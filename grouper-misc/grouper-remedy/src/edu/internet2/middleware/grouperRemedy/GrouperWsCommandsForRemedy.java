/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouperClient.api.GcFindGroups;
import edu.internet2.middleware.grouperClient.api.GcGetMembers;
import edu.internet2.middleware.grouperClient.api.GcGroupSave;
import edu.internet2.middleware.grouperClient.api.GcMessageAcknowledge;
import edu.internet2.middleware.grouperClient.api.GcMessageReceive;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsMessage;
import edu.internet2.middleware.grouperClient.ws.beans.WsMessageResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;


/**
 *
 */
public class GrouperWsCommandsForRemedy {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    createGrouperGroup("abc", "abc_123");
  }
  
  /**
   * 
   */
  public GrouperWsCommandsForRemedy() {
  }

  /**
   * 
   * @param groupName
   * @return the list of users
   */
  public static Set<String> retrieveGrouperMembershipsForGroup(String groupName) {

    
    //users that are supposed to be in remedy (do this at top so it doesnt affect the timing here
    Map<String, String[]> usersAllowedToBeInRemedy = GrouperWsCommandsForRemedy.retrieveGrouperUsers();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveGrouperMembershipsForGroup");
    debugMap.put("groupName", groupName);
    long startTime = System.nanoTime();
    try {
    
      String subjectAttributeForRemedyUsername = GrouperRemedyUtils.configSubjectAttributeForRemedyUsername();
      
      Set<String> grouperUsernamesInGroup = new TreeSet<String>();

      GcGetMembers gcGetMembers = new GcGetMembers().addGroupName(groupName);
        
      // request extra attributes in WS call
      if (!GrouperClientUtils.equals("id", subjectAttributeForRemedyUsername)) {
        gcGetMembers.addSubjectAttributeName(subjectAttributeForRemedyUsername);
      }

      for (String sourceId : GrouperRemedyUtils.configSourcesForSubjects()) {
        gcGetMembers.addSourceId(sourceId);
      }
      
      WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();

      WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];
        
      WsSubject[] wsSubjects = wsGetMembersResult.getWsSubjects();
      
      debugMap.put("originalMemberCount", GrouperClientUtils.length(wsSubjects));

      String[] attributeNames = wsGetMembersResults.getSubjectAttributeNames();

      int unresolvableCount = 0;
      int notAllowedInRemedyCount = 0;
      
      //get usernames from grouper
      for (WsSubject wsSubject : GrouperClientUtils.nonNull(wsSubjects, WsSubject.class)) {
        String subjectPrefix = null;
        if (GrouperClientUtils.equals("id", subjectAttributeForRemedyUsername)) {
          subjectPrefix = wsSubject.getId();
        } else {
          subjectPrefix = GrouperClientUtils.subjectAttributeValue(wsSubject, attributeNames, subjectAttributeForRemedyUsername);
        }
        if (GrouperClientUtils.isBlank(subjectPrefix)) {
          //i guess this is ok
          debugMap.put("subjectBlankAttribute_" + wsSubject.getSourceId() + "_" + wsSubject.getId(), subjectAttributeForRemedyUsername);
          unresolvableCount++;
        } else {
          String remedyUserName = subjectPrefix
              + GrouperClientUtils.defaultIfBlank(GrouperClientConfig.retrieveConfig().propertyValueString("grouperRemedy.subjectIdSuffix"), "");
          if (usersAllowedToBeInRemedy == null || usersAllowedToBeInRemedy.containsKey(remedyUserName)) {
            grouperUsernamesInGroup.add(remedyUserName);
          } else {
            notAllowedInRemedyCount++;
          }
        }
      }

      debugMap.put("finalMemberCount", GrouperClientUtils.length(grouperUsernamesInGroup));
      debugMap.put("unresolvableCount", unresolvableCount);
      debugMap.put("notAllowedInRemedyCount", notAllowedInRemedyCount);
      return grouperUsernamesInGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }

  }
  
  /**
   * @return list of groups never null
   */
  public static List<WsGroup> retrieveGrouperGroups() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveGrouperGroups");
    long startTime = System.nanoTime();
    try {
    
      //# put groups in here which go to remedy, the name in remedy will be the extension here
      String grouperRemedyFolderName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperRemedy.folder.name.withRemedyGroups");

      debugMap.put("grouperRemedyFolderName", grouperRemedyFolderName);

      WsQueryFilter wsQueryFilter = new WsQueryFilter();
      wsQueryFilter.setQueryFilterType("FIND_BY_STEM_NAME");
      wsQueryFilter.setStemName(grouperRemedyFolderName);
      wsQueryFilter.setStemNameScope("ONE_LEVEL");
      
      WsFindGroupsResults wsFindGroupsResults = new GcFindGroups().assignQueryFilter(wsQueryFilter).execute();
    
      WsGroup[] wsGroupsArray = wsFindGroupsResults.getGroupResults();

      debugMap.put("numberOfGroups", GrouperClientUtils.length(wsGroupsArray));

      @SuppressWarnings("unchecked")
      List<WsGroup> grouperGroups = wsGroupsArray == null ? new ArrayList<WsGroup>() : (List<WsGroup>)GrouperClientUtils.toList(wsGroupsArray);
      return grouperGroups;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }

    
  }

  /**
   * cache of users in grouper
   */
  private static ExpirableCache<Boolean, Map<String, String[]>> retrieveGrouperUsersCache = null;
  
  /**
   * lazy load return the cache of users
   * @return the cache
   */
  private static ExpirableCache<Boolean, Map<String, String[]>> retrieveGrouperUsersCache() {
    if (retrieveGrouperUsersCache == null) {
      int expireMinutes = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperRemedy.cacheGrouperUsersForMinutes", 60);
      retrieveGrouperUsersCache = new ExpirableCache(expireMinutes);
    }
    return retrieveGrouperUsersCache;
  }
  
  /**
   * return null if not configured to have such a group, otherwise the cached members of the group.  note, this is not a copy of the
   * cache, it can be edited
   * @return a map of remedyUserName to array of [subjectId, sourceId, remedySubjectAttributeValue, remedyUserName]
   */
  public static Map<String, String[]> retrieveGrouperUsers() {
    
    Map<String, String[]> result = retrieveGrouperUsersCache().get(Boolean.TRUE);
    if (result != null) {
      return result;
    }

    String remedyGrouperRequireGroupName = GrouperClientConfig.retrieveConfig().propertyValueString("grouperRemedy.requireGroup");
    if (GrouperClientUtils.isBlank(remedyGrouperRequireGroupName)) {
      return null;
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "retrieveGrouperUsers");

    try {
      result = new HashMap<String, String[]>();
      
      GcGetMembers gcGetMembers = new GcGetMembers().addGroupName(remedyGrouperRequireGroupName);
      
      for (String sourceId : GrouperRemedyUtils.configSourcesForSubjects()) {
        gcGetMembers.addSourceId(sourceId);
      }
  
      String configSubjectAttributeForRemedyUsername = GrouperRemedyUtils.configSubjectAttributeForRemedyUsername();
      
      debugMap.put("configSubjectAttributeForRemedyUsername", configSubjectAttributeForRemedyUsername);
      
      if (GrouperClientUtils.equals("id", configSubjectAttributeForRemedyUsername)) {
  
        // in the utils method below this is what it expects
        configSubjectAttributeForRemedyUsername = "subject__id"; 
      } else {
        gcGetMembers.addSubjectAttributeName(configSubjectAttributeForRemedyUsername);
      }
      WsGetMembersResults wsGetMembersResults = gcGetMembers.execute();
      WsGetMembersResult wsGetMembersResult = wsGetMembersResults.getResults()[0];
      
      debugMap.put("resultSize", GrouperClientUtils.length(wsGetMembersResult.getWsSubjects()));

      String subjectSuffix = GrouperClientUtils.defaultIfBlank(GrouperClientConfig.retrieveConfig().propertyValueString("grouperRemedy.subjectIdSuffix"), "");

      debugMap.put("subjectSuffix", subjectSuffix);

      for (WsSubject wsSubject: GrouperClientUtils.nonNull(wsGetMembersResult.getWsSubjects(), WsSubject.class)) {
        String usernameValueWithoutSuffix = GrouperClientUtils.subjectAttributeValue(wsSubject, wsGetMembersResults.getSubjectAttributeNames(), configSubjectAttributeForRemedyUsername);
        String remedyUsername = usernameValueWithoutSuffix + subjectSuffix;
        String[] userArray = new String[]{wsSubject.getId(), wsSubject.getSourceId(), usernameValueWithoutSuffix, remedyUsername};
        result.put(remedyUsername, userArray);
      }
      
      retrieveGrouperUsersCache().put(Boolean.TRUE, result);
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTimeNanos);
    }
    
    return result;
  }

  /**
   * 
   * @return the messages
   */
  public static WsMessage[] grouperReceiveMessages() {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "grouperReceiveMessages");

    try {
      String messageSystemName = GrouperClientConfig.retrieveConfig()
          .propertyValueStringRequired("grouperRemedy.messaging.systemName");

      debugMap.put("messageSystemName", messageSystemName);
      
      String messageQueueName = GrouperClientConfig.retrieveConfig()
          .propertyValueStringRequired("grouperRemedy.messaging.queueName");

      debugMap.put("messageQueueName", messageQueueName);

      WsMessageResults wsMessageResults = new GcMessageReceive()
        .assignMessageSystemName(messageSystemName).assignQueueOrTopicName(messageQueueName).execute();

      debugMap.put("checkMessagesWsResultCode", wsMessageResults.getResultMetadata().getResultCode());
      debugMap.put("messageCount", GrouperClientUtils.length(wsMessageResults.getMessages()));

      return wsMessageResults.getMessages();
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTimeNanos);
    }

  }
  
  /**
   * @param ids
   * @param acknowledgeType mark_as_processed, return_to_queue, return_to_end_of_queue,  send_to_another_queue
   */
  public static void grouperAcknowledgeMessages(Set<String> ids, String acknowledgeType) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "grouperAcknowledgeMessages");
    debugMap.put("numberOfIds", GrouperClientUtils.length(ids));
    debugMap.put("acknowledgeType", acknowledgeType);

    try {
      String messageSystemName = GrouperClientConfig.retrieveConfig()
          .propertyValueStringRequired("grouperRemedy.messaging.systemName");

      debugMap.put("messageSystemName", messageSystemName);
      
      String messageQueueName = GrouperClientConfig.retrieveConfig()
          .propertyValueStringRequired("grouperRemedy.messaging.queueName");

      debugMap.put("messageQueueName", messageQueueName);


      GcMessageAcknowledge successMessageAcknowledge = null;
      successMessageAcknowledge = new GcMessageAcknowledge()
        .assignMessageSystemName(messageSystemName).assignQueueOrTopicName(messageQueueName).assignAcknowledgeType(acknowledgeType);
      
      for (String id : ids) {
        //mark message as processed
        successMessageAcknowledge.addMessageId(id);
      }
      
      successMessageAcknowledge.execute();
      
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTimeNanos);
    }

  }

  /**
   * massage name to get special shars out
   * @param name
   * @return the new name
   */
  private static String massageName(String name) {
    if (GrouperClientUtils.isBlank(name)) {
      return name;
    }
    StringBuilder result = new StringBuilder();
    for (int i=0;i<name.length();i++) {
      char theChar = name.charAt(i);
      if (('a' <= theChar && theChar <= 'z')
          || ('A' <= theChar && theChar <= 'Z')
          || ('0' <= theChar && theChar <= '9')) {
        result.append(theChar);
      } else {
        result.append("_");
      }
    }
    return result.toString();
  }
  
  /**
   * @param extension 
   * @param displayExtension 
   */
  public static void createGrouperGroup(String extension, String displayExtension) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "createGrouperGroup");
    long startTime = System.nanoTime();
    try {
    
      //# put groups in here which go to remedy, the name in remedy will be the extension here
      String grouperRemedyFolderName = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperRemedy.folder.name.withRemedyGroups");
  
      debugMap.put("grouperRemedyFolderName", grouperRemedyFolderName);

      WsGroupToSave wsGroupToSave = new WsGroupToSave();
      WsGroup wsGroup = new WsGroup();
      String groupName = grouperRemedyFolderName + ":" + massageName(extension);
      wsGroup.setName(groupName);
      debugMap.put("groupName", groupName);
      wsGroup.setDisplayExtension(massageName(displayExtension));
      debugMap.put("displayExtension", displayExtension);
      wsGroupToSave.setWsGroup(wsGroup);
      wsGroupToSave.setWsGroupLookup(new WsGroupLookup(groupName, null));
      
      @SuppressWarnings("unused")
      WsGroupSaveResults wsGroupSaveResults = new GcGroupSave().addGroupToSave(wsGroupToSave).execute();
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperRemedyLog.remedyLog(debugMap, startTime);
    }
  
    
  }

}
