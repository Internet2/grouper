/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import java.util.ArrayList;
import java.util.Collection;
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
      
      if (GrouperClientUtils.equalsIgnoreCase(wsSubject.getSuccess(), "T")){
        
        GrouperAtlassianSourceConfig grouperAtlassianSourceConfig = sourceConfigMap.get(wsSubject.getSourceId());
        //we dont have a config for this source
        if (grouperAtlassianSourceConfig == null) {
          continue;
        }
        
        String subjectIdName = grouperAtlassianSourceConfig.getIdOrAttribute();
        String atlassianId = null;
        if (GrouperClientUtils.equalsIgnoreCase("id", subjectIdName)) {
          atlassianId = wsSubject.getId();
        } else {
          atlassianId = subjectAttributeValue(wsSubject, subjectAttributeNames, subjectIdName);
        }
        //if it didnt have that attribute, then skip
        if (GrouperClientUtils.isBlank(atlassianId)) {
          continue;
        }
        result.add(atlassianId);

      }
      
    }
    
    return result;
  }
  
  /**
   * get subject attribute names per config, or empty list if none
   * @return subject attribute names
   */
  public static List<String> subjectAttributeNames() {
    
    Set<String> result = new LinkedHashSet<String>();
    
    for (GrouperAtlassianSourceConfig grouperAtlassianSourceConfig : GrouperAtlassianConfig.grouperAtlassianConfig().getSourceConfigs().values()) {
      
      String idOrAttribute = grouperAtlassianSourceConfig.getIdOrAttribute();
      
      //if it is id, then that means use subjectId
      if (!GrouperClientUtils.equalsIgnoreCase("id", idOrAttribute)) {
        result.add(idOrAttribute);
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
    String folderRoot = GrouperAtlassianConfig.grouperAtlassianConfig().getRootFolder();
    for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGroups, WsGroup.class)) {
      String groupName = wsGroup.getName();
      if (!groupName.startsWith(folderRoot)) {
        throw new RuntimeException("Why does group name: " + groupName + " not start with folderRoot? " + folderRoot);
      }
      //add one for a colon...
      String atlassianName = groupName.substring(folderRoot.length()+1);
      atlassianGroups.add(atlassianName);
    }
    return atlassianGroups;
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
  
}
