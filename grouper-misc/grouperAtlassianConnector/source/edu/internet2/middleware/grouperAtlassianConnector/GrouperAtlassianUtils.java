/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperAtlassianConnector.GrouperAtlassianConfig.GrouperAtlassianSourceConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * 
 */
public class GrouperAtlassianUtils {

  /**
   * in property set map, this is name
   */
  public static final String PROPERTY_SET_NAME = "fullName";
  
  /**
   * in property set map, this is email
   */
  public static final String PROPERTY_SET_EMAIL = "email";

  /**
   * assign how long this method took in nanos
   * @param debugMap
   * @param startNanos
   */
  public static void assignTimingGate(Map<String, Object> debugMap, long startNanos) {
    long totalNanos = System.nanoTime() - startNanos;
    long totalMillis = totalNanos / 1000000;
    debugMap.put("timeMillis", totalMillis);
  }
  
  /**
   * get the attribute value of an attribute name of a subject
   * @param wsSubject subject
   * @param attributeNames list of attribute names in the subject
   * @param attributeName to query
   * @return the value or null
   */
  public static String subjectAttributeValue(WsSubject wsSubject, String[] attributeNames, String attributeName) {
    for (int i=0;i<GrouperClientUtils.length(attributeNames);i++) {
      
      if (GrouperClientUtils.equalsIgnoreCase(attributeName, attributeNames[i])
          && GrouperClientUtils.length(wsSubject.getAttributeValues()) > i) {
        //got it
        return wsSubject.getAttributeValue(i);
      }
    }
    return null;
  }

  /**
   * convert WS users to atlassian users
   * @param subjectAttributeNames 
   * @param wsSubjects 
   * @return the list of users
   */
  public static List<String> convertToAtlassianUsers(String[] subjectAttributeNames, WsSubject[] wsSubjects) {
    Map<String, GrouperAtlassianSourceConfig> sourceConfigMap = GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs();
    
    List<String> result = new ArrayList<String>();  
    
    for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsSubjects, WsSubject.class)) {

      String atlassianId = convertToAtlassianUser(subjectAttributeNames, wsSubject, sourceConfigMap);
      if (!GrouperClientUtils.isBlank(atlassianId)) {
        result.add(atlassianId);
      }
      
    }
    
    return result;
  }
  
  /**
   * convert WS user to atlassian user
   * @param subjectAttributeNames
   * @param wsSubject
   * @param sourceConfigMap e.g. GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs()
   * @return the user string or null if not found
   */
  public static String convertToAtlassianUser(String[] subjectAttributeNames, WsSubject wsSubject, Map<String, GrouperAtlassianSourceConfig> sourceConfigMap) {

    String result = null;  

    if (GrouperClientUtils.equalsIgnoreCase(wsSubject.getSuccess(), "T")){
      
      GrouperAtlassianSourceConfig grouperAtlassianSourceConfig = sourceConfigMap.get(wsSubject.getSourceId());
      //we dont have a config for this source
      if (grouperAtlassianSourceConfig != null) {
        String subjectIdName = grouperAtlassianSourceConfig.getIdOrAttribute();
        String atlassianId = null;
        if (GrouperClientUtils.equalsIgnoreCase("id", subjectIdName)) {
          atlassianId = wsSubject.getId();
        } else {
          atlassianId = subjectAttributeValue(wsSubject, subjectAttributeNames, subjectIdName);
        }
        //if it didnt have that attribute, then skip
        if (!GrouperClientUtils.isBlank(atlassianId)) {
          result = atlassianId;
        }
      }
    }
    return result;
  }
  
  /**
   * convert WS users to atlassian propertySets
   * @param subjectAttributeNames 
   * @param wsSubjects 
   * @return the list of users, by user id
   */
  public static Map<String,Map<String, String>> convertToAtlassianPropertySets(String[] subjectAttributeNames, WsSubject[] wsSubjects) {
    Map<String, GrouperAtlassianSourceConfig> sourceConfigMap = GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs();
    
    Map<String,Map<String, String>> propertySetMap = new HashMap<String, Map<String, String>>();
    
    for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsSubjects, WsSubject.class)) {
      
      String userId = convertToAtlassianUser(subjectAttributeNames, wsSubject, sourceConfigMap);
      if (GrouperClientUtils.isBlank(userId)) {
        continue;
      }
      Map<String, String> propertySet = convertToAtlassianPropertySet(userId, subjectAttributeNames, wsSubject, sourceConfigMap);
      if (propertySet == null) {
        propertySet = new HashMap<String, String>();
      }
      propertySetMap.put(userId, propertySet);
    }
    
    return propertySetMap;
  }
  
  /**
   * convert WS user to atlassian propertySet
   * @param userId
   * @param subjectAttributeNames 
   * @param wsSubject
   * @param sourceConfigMap e.g. GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs()
   * @return the propertySet or null if not found or a problem
   */
  public static Map<String, String> convertToAtlassianPropertySet(String userId, String[] subjectAttributeNames, WsSubject wsSubject, Map<String, GrouperAtlassianSourceConfig> sourceConfigMap) {

    String name = null;
    String email = null;
    
    if (GrouperClientUtils.equalsIgnoreCase(wsSubject.getSuccess(), "T")){
      
      GrouperAtlassianSourceConfig grouperAtlassianSourceConfig = sourceConfigMap.get(wsSubject.getSourceId());
      //we dont have a config for this source
      if (grouperAtlassianSourceConfig == null) {
        return null;
      }
      
      {
        String nameAttribute = grouperAtlassianSourceConfig.getNameAttribute();
        if (GrouperClientUtils.equalsIgnoreCase("id", nameAttribute)) {
          name = wsSubject.getId();
        } else if (GrouperClientUtils.equalsIgnoreCase("name", nameAttribute)) {
          name = wsSubject.getName();
        } else {
          //note: description is not in the API
          name = subjectAttributeValue(wsSubject, subjectAttributeNames, nameAttribute);
        }
      }
      
      {
        String emailAttribute = grouperAtlassianSourceConfig.getEmailAttribute();
        if (GrouperClientUtils.equalsIgnoreCase("id", emailAttribute)) {
          email = wsSubject.getId();
        } else {
          //note: description is not in the API
          email = subjectAttributeValue(wsSubject, subjectAttributeNames, emailAttribute);
        }
      }
      

    } else {
      return null;
    }
    
    return propertySet(userId, name, email);
  }
  
  /**
   * get subject attribute names per config, or empty list if none
   * @param retrieveProfileAttributes 
   * @return subject attribute names
   */
  public static List<String> subjectAttributeNames(boolean retrieveProfileAttributes) {
    
    Set<String> result = new LinkedHashSet<String>();
    
    for (GrouperAtlassianSourceConfig grouperAtlassianSourceConfig : GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs().values()) {
      
      String idOrAttribute = grouperAtlassianSourceConfig.getIdOrAttribute();
      
      //if it is id, then that means use subjectId
      if (!GrouperClientUtils.equalsIgnoreCase("id", idOrAttribute)) {
        result.add(idOrAttribute);
      }
      
      if (retrieveProfileAttributes) {
        {
          String emailAttribute = grouperAtlassianSourceConfig.getEmailAttribute();
          if (!GrouperClientUtils.isBlank(emailAttribute)) {
            result.add(emailAttribute);
          }
        }
        {
          String nameAttribute = grouperAtlassianSourceConfig.getNameAttribute();
          if (!GrouperClientUtils.isBlank(nameAttribute)) {
            if (!GrouperClientUtils.equalsIgnoreCase("name", nameAttribute) && 
                !GrouperClientUtils.equalsIgnoreCase("id", nameAttribute) &&
                !GrouperClientUtils.equalsIgnoreCase("description", nameAttribute)) {
              result.add(nameAttribute);
            }
          }
        }
      }
      
    }
    
    return new ArrayList<String>(result);
    
  }
  
  /**
   * get source ids to search in
   * @return the source ids
   */
  public static List<String> sourceIdsToSearch() {
    
    Set<String> result = new LinkedHashSet<String>();
    
    for (GrouperAtlassianSourceConfig grouperAtlassianSourceConfig : GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs().values()) {
      
      result.add(grouperAtlassianSourceConfig.getSourceId());
      
    }
    
    return new ArrayList<String>(result);
    
  }
  
  /**
   * 
   * @param wsGroups 
   * @return the atlassian groups non null
   */
  public static List<String> convertToAtlassianGroups(WsGroup[] wsGroups) {
    List<String> atlassianGroups = new ArrayList<String>();
    for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGroups, WsGroup.class)) {
      String groupName = wsGroup.getName();
      String atlassianName = atlassianGroupName(groupName);
      atlassianGroups.add(atlassianName);
    }
    return atlassianGroups;
  }
  
  /**
   * 
   * @param grouperGroupName
   * @return atlassian group name
   */
  public static String atlassianGroupName(String grouperGroupName) {
    String folderRoot = GrouperAtlassianConfig.grouperAtlassianConfig().getRootFolder();
    if (!grouperGroupName.startsWith(folderRoot)) {
      throw new RuntimeException("Why does group name: " + grouperGroupName + " not start with folderRoot? " + folderRoot);
    }
    String atlassianName = grouperGroupName.substring(folderRoot.length()+1);
    return atlassianName;
  }
  
  /**
   * 
   * @param wsResultMeta
   * @param debugMap 
   * @param includeMessage 
   */
  public static void addToDebugMap(WsResultMeta wsResultMeta, Map<String, Object> debugMap, boolean includeMessage) {
    String resultCode = wsResultMeta.getResultCode();
    String success = wsResultMeta.getSuccess();
    
    debugMap.put("success", success);
    debugMap.put("resultCode", resultCode);
    
    if (includeMessage) {
      debugMap.put("resultMessage", wsResultMeta.getResultMessage());
    }
  }
  
  /**
   * convert atlassian user to grouper ws subject lookup
   * @param username 
   * @param debugMap
   * @return the ws subject lookup
   */
  public static WsSubjectLookup wsSubjectLookup(String username, Map<String, Object> debugMap) {
    
    if (GrouperAtlassianConfig.grouperAtlassianConfig()
        .getWsUsersToIgnore().contains(username)) {
      
      String error = "User " + username 
              + " is restricted from Grouper via config file";
      debugMap.put("exception", error);
      throw new RuntimeException(error);
    }

    WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
    
    String sourceId = GrouperAtlassianConfig.grouperAtlassianConfig().getSubjectSearchSourceId();
    if (!GrouperClientUtils.isBlank(sourceId)) {
      debugMap.put("sourceId", sourceId);
      wsSubjectLookup.setSubjectSourceId(sourceId);
    }
    
    String subjectIdMethod = GrouperAtlassianConfig.grouperAtlassianConfig().getSubjectSearchSubjectId();
    
    debugMap.put("subjectIdMethod", subjectIdMethod);
    
    if (GrouperClientUtils.equalsIgnoreCase("id", subjectIdMethod)) {
      wsSubjectLookup.setSubjectId(username);
    } else if (GrouperClientUtils.equalsIgnoreCase("identifier", subjectIdMethod)) {
      wsSubjectLookup.setSubjectIdentifier(username);
    } else if (GrouperClientUtils.equalsIgnoreCase("idOrIdentifier", subjectIdMethod)) {
      wsSubjectLookup.setSubjectIdentifier(username);
      wsSubjectLookup.setSubjectId(username);
    }
    
    return wsSubjectLookup;
  }
  
  /**
   * 
   * @param debugMap
   * @return the string
   */
  public static String mapForLog(Map<String, Object> debugMap) {
    if (GrouperClientUtils.length(debugMap) == 0) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (String key : debugMap.keySet()) {
      Object valueObject = debugMap.get(key);
      String value = valueObject == null ? null : valueObject.toString();
      result.append(key).append(": ").append(GrouperClientUtils.abbreviate(value, 100)).append(", ");
    }
    //take off the last two chars
    result.delete(result.length()-2, result.length());
    return result.toString();
  }

  /**
   * 
   * @param debugMap
   * @param collection 
   * @param logLabel 
   */
  public static void addToDebugMap(Map<String, Object> debugMap, Collection<?> collection, String logLabel) {

    StringBuilder result = new StringBuilder();
    result.append("Size ").append(GrouperClientUtils.length(collection));
    if (GrouperClientUtils.length(collection) > 0) {
      result.append(": ");
      int index = 0;
      for (Object object : collection) {
        //dont put more than 20 in there
        if (index > 20) {
          result.append("..., ");
          break;
        }
        String string = object == null ? "null" : object.toString();
        //abbreviate
        result.append(GrouperClientUtils.abbreviate(string, 30)).append(", ");
        index++;
      }
      //take off the last two chars
      result.delete(result.length()-2, result.length());
    }
    debugMap.put(logLabel, result.toString());
  }

  /**
   * convert an atlassian group name to a grouper group name
   * @param atlassianGroupName
   * @param debugMap 
   * @return the grouper group name
   */
  public static String grouperGroupName(String atlassianGroupName, Map<String, Object> debugMap) {
    
    String grouperGroupName = GrouperAtlassianConfig.grouperAtlassianConfig().getRootFolder() + ":" + atlassianGroupName;
    debugMap.put("grouperGroupName", grouperGroupName);
    return grouperGroupName;
    
  }
  
  /**
   * property set of name and email
   * @param userId 
   * @param name
   * @param email
   * @return property set
   */
  public static Map<String, String> propertySet(String userId, String name, String email) {
    Map<String, String> propertySet = new HashMap<String, String>();

    if (!GrouperClientUtils.isBlank(email)) {
      propertySet.put(PROPERTY_SET_EMAIL, email);
    }
    propertySet.put(PROPERTY_SET_NAME, GrouperClientUtils.defaultIfBlank(name, userId));
    return propertySet;
  }
  
}
