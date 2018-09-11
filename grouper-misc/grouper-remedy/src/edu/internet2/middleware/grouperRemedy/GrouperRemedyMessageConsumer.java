/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import edu.internet2.middleware.grouperClient.api.GcHasMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsMessage;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.grouperClientExt.xmpp.EsbEvent;
import edu.internet2.middleware.grouperClientExt.xmpp.EsbEvents;
import edu.internet2.middleware.grouperClientExt.xmpp.GcDecodeEsbEvents;

/**
 *
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GrouperRemedyMessageConsumer implements Job {

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperRemedyFullRefresh.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    incrementalSync();
  }
  
  /**
   * if incremental refresh is in progress
   */
  static boolean incrementalRefreshInProgress = false;
  
  
  /**
   * if incremental refresh is in progress
   * @return the fullRefreshInProgress
   */
  public static boolean isIncrementalRefreshInProgress() {
    return incrementalRefreshInProgress;
  }

  /**
   * wait for full refresh to end
   */
  public static void waitForIncrementalRefreshToEnd() {
    while (isIncrementalRefreshInProgress()) {
      GrouperClientUtils.sleep(100);
    }
  }
  
  /**
   * do an incrementalsync
   */
  public static void incrementalSync() {
    
    incrementalRefreshInProgress = true;

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "incrementalSync");

    boolean logDebugMap = true;

    Set<String> successMessageIds = new HashSet<String>();
    Set<String> waitMessageIds = new HashSet<String>();

    try {
      GrouperRemedyFullRefresh.waitForFullRefreshToEnd();

      WsMessage[] wsMessages = GrouperWsCommandsForRemedy.grouperReceiveMessages();

      if (GrouperClientUtils.length(wsMessages) == 0) {
        boolean logIfNoMessages = GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperRemedy.logIfNoMessages", false);
        if (!logIfNoMessages) {
          logDebugMap = false;
        }
        return;
      }
      
      boolean fullSyncOnMessage = GrouperClientConfig.retrieveConfig().propertyValueBoolean(
          "grouperRemedy.fullSyncOnMessage", false);

      if (fullSyncOnMessage) {
        debugMap.put("fullSyncOnMessage", true);
      }
      
      int fullSyncOnMessageWaitSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt(
          "grouperRemedy.fullSyncOnMessageWaitSeconds", 30);

      //short circuit to let full go
      if (GrouperRemedyFullRefresh.isFullRefreshInProgress()) {
        return;
      }

      //wait message ids
      for (WsMessage wsMessage : GrouperClientUtils.nonNull(wsMessages, WsMessage.class)) {
        waitMessageIds.add(wsMessage.getId());
      }

      //process messages
      for (WsMessage wsMessage : GrouperClientUtils.nonNull(wsMessages, WsMessage.class)) {

        String jsonString = wsMessage.getMessageBody();  
        
        EsbEvents esbEvents = GcDecodeEsbEvents.decodeEsbEvents(jsonString);
        esbEvents = GcDecodeEsbEvents.unencryptEsbEvents(esbEvents);

        //  {  
        //  "encrypted":false,
        //  "esbEvent":[  
        //     {  
        //        "changeOccurred":false,
        //        "createdOnMicros":1476889916578000,
        //        "eventType":"MEMBERSHIP_DELETE",
        //        "fieldName":"members",
        //        "groupId":"89dd656be8c743e79b2ef24fde6dab36",
        //        "groupName":"remedy:groups:someGroup",
        //        "id":"c2641b287f964bb28b2f0ddcd05f9fd3",
        //        "membershipType":"flattened",
        //        "sequenceNumber":"618",
        //        "sourceId":"g:isa",
        //        "subjectId":"GrouperSystem"
        //     }
        //  ]
        //}
        
        //not sure why there would be no events in there
        for (EsbEvent esbEvent : GrouperClientUtils.nonNull(esbEvents.getEsbEvent(), EsbEvent.class)) {

          if (GrouperRemedyFullRefresh.getLastFullRefreshStart() > (esbEvent.getCreatedOnMicros() / 1000L)) {
            
            continue;
            
          }

          if (fullSyncOnMessage) {
            if (fullSyncOnMessageWaitSeconds < 5) {
              fullSyncOnMessageWaitSeconds = 5;
            }
            GrouperClientUtils.sleep(fullSyncOnMessageWaitSeconds * 1000L);
            
            try {
              incrementalRefreshInProgress = false;
              GrouperRemedyFullRefresh.fullRefreshLogic();
            } finally {
              incrementalRefreshInProgress = true;
            }
          } else {
            processMessage(esbEvent);
          }

        }
        
        //mark message as processed
        successMessageIds.add(wsMessage.getId());
        waitMessageIds.remove(wsMessage.getId());
        
      }

    } finally {
      debugMap.put("successMessageCount", GrouperClientUtils.length(successMessageIds));
      debugMap.put("waitMessageCount", GrouperClientUtils.length(waitMessageIds));
      
      try {
        //mark messages as processed
        if (GrouperClientUtils.length(successMessageIds) > 0) {
          GrouperWsCommandsForRemedy.grouperAcknowledgeMessages(successMessageIds, "mark_as_processed");
        }
      } catch (Exception e) {
        debugMap.put("successAcknowledgeException", GrouperClientUtils.getFullStackTrace(e));
      }

      try {
        //mark messages as return to queue
        if (GrouperClientUtils.length(waitMessageIds) > 0) {
          GrouperWsCommandsForRemedy.grouperAcknowledgeMessages(waitMessageIds, "return_to_queue");
        }
      } catch (Exception e) {
        debugMap.put("waitAcknowledgeException", GrouperClientUtils.getFullStackTrace(e));
      }

      incrementalRefreshInProgress = false;

      if (logDebugMap) {
        GrouperRemedyLog.remedyLog(debugMap, startTimeNanos);
      }
    }

    //if there were errors, wait a minute
    if (GrouperClientUtils.length(waitMessageIds) > 0) {
      GrouperClientUtils.sleep(60000);
    }

    //if there were messages successfully processed, then try again, might be more on queue
    if (GrouperClientUtils.length(successMessageIds) > 0) {
      incrementalSync();
    }
    
  }

  /**
   * process message
   * @param esbEvent
   */
  public static void processMessage(EsbEvent esbEvent) {
    String subjectAttributeForRemedyUsername = GrouperRemedyUtils.configSubjectAttributeForRemedyUsername();
    String subjectAttributeValue = null;
    if (!GrouperClientUtils.equals("id", subjectAttributeForRemedyUsername)) {
      // note make sure the loader is configured to send this attribute
      subjectAttributeValue = esbEvent.subjectAttribute(subjectAttributeForRemedyUsername);
    }
    processMessage(esbEvent.getEventType(), GrouperClientUtils.defaultIfBlank(esbEvent.getName(),esbEvent.getGroupName()), esbEvent.getSourceId(), esbEvent.getSubjectId(), subjectAttributeValue);
  }
  
  /**
   * process message
   * @param eventType 
   * @param groupName 
   * @param sourceId 
   * @param subjectId 
   * @param subjectAttributeValue 
   */
  public static void processMessage(String eventType, String groupName, String sourceId, String subjectId, String subjectAttributeValue) {
    

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "processMessage");

    try {

      //  {  
      //  "encrypted":false,
      //  "esbEvent":[  
      //     {  
      //        "changeOccurred":false,
      //        "createdOnMicros":1476889916578000,
      //        "eventType":"MEMBERSHIP_DELETE",
      //        "fieldName":"members",
      //        "groupId":"89dd656be8c743e79b2ef24fde6dab36",
      //        "groupName":"remedy:groups:someGroup",
      //        "id":"c2641b287f964bb28b2f0ddcd05f9fd3",
      //        "membershipType":"flattened",
      //        "sequenceNumber":"618",
      //        "sourceId":"g:isa",
      //        "subjectId":"GrouperSystem"
      //     }
      //  ]
      //}

      debugMap.put("eventType", eventType);
      debugMap.put("groupName", groupName);
      
      boolean remedyGroupWhichHasAllowedUsers = GrouperClientUtils.equals(groupName, GrouperClientConfig.retrieveConfig().propertyValueString("grouperRemedy.requireGroup"));
      
      if (!remedyGroupWhichHasAllowedUsers && !GrouperRemedyUtils.validRemedyGroupName(groupName)) {
        debugMap.put("invalidGroupName", true);
        return;
      }
      String groupExtension = GrouperClientUtils.extensionFromName(groupName);

      boolean isMembershipAdd = GrouperClientUtils.equals(eventType, "MEMBERSHIP_ADD");
      boolean isMembershipUpdate = GrouperClientUtils.equals(eventType, "MEMBERSHIP_UPDATE");
      boolean isMembershipDelete = GrouperClientUtils.equals(eventType, "MEMBERSHIP_DELETE");

      //get groups from remedy
      Map<Long, GrouperRemedyGroup> remedyGroupNameToGroupMap = GrouperRemedyCommands.retrieveRemedyGroups();

      GrouperRemedyGroup groupInRemedy = null;
      try {
        Long theLong = GrouperClientUtils.longObjectValue(groupExtension, false);
        groupInRemedy = remedyGroupNameToGroupMap.get(theLong);
      } catch (Exception e) {
        LOG.debug("Cant convert extension: '" + groupExtension + "'", e);
        //ignore
      }

      debugMap.put("groupInRemedy", groupInRemedy != null);

      if (groupInRemedy != null) {
        
        if (GrouperClientUtils.equals(eventType, "GROUP_ADD")) {
          if (!remedyGroupWhichHasAllowedUsers) {
            //create remedy group
  //          GrouperRemedyCommands.createRemedyGroup(groupExtension, true);
          }
  
        } else if (GrouperClientUtils.equals(eventType, "GROUP_DELETE")) {
          
          if (!remedyGroupWhichHasAllowedUsers) {
            //create remedy group
  //          GrouperRemedyCommands.deleteRemedyGroup(groupInRemedy, true);
          }
        } else if (GrouperClientUtils.equals(eventType, "GROUP_UPDATE")) {
          
          if (!remedyGroupWhichHasAllowedUsers) {
            if (groupInRemedy == null) {
              //hmmm, rename, do a full refresh?  need to delete old, create new, and add/remove memberships
  //            GrouperRemedyFullRefresh.fullRefreshLogic();
            }
          }        
          
        } else if (isMembershipAdd || isMembershipDelete || isMembershipUpdate) {
  
          debugMap.put("sourceId", sourceId);
  
          boolean inCorrectSubjectSource = GrouperRemedyUtils.configSourcesForSubjects().contains(sourceId);
  
          String username = null;
  
          if (inCorrectSubjectSource) {
  
            String subjectAttributeForRemedyUsername = GrouperRemedyUtils.configSubjectAttributeForRemedyUsername();
            debugMap.put("subjectAttributeRemedyUsername", subjectAttributeForRemedyUsername);
           
            
            if (GrouperClientUtils.equals("id", subjectAttributeForRemedyUsername)) {
              username = subjectId;
            } else {
              // note make sure the loader is configured to send this attribute
              username = subjectAttributeValue;
            }
            debugMap.put("username", username);
          } else {
            debugMap.put("invalidSource", true);
            
          }
  
          if (GrouperClientUtils.isBlank(username)) {
            //this isnt good
            return;
          }
  
          String usernamePrefix = username;
          
          username += GrouperClientUtils.defaultIfBlank(GrouperClientConfig.retrieveConfig().propertyValueString("grouperRemedy.subjectIdSuffix"), "");
          
          debugMap.put("remedyUsername", username);
          
          //lets get the user from remedy
          GrouperRemedyUser grouperRemedyUser = GrouperRemedyUser.retrieveUsers().get(username);
  
          debugMap.put("remedyUserExists", grouperRemedyUser != null);
  
          if (grouperRemedyUser == null) {
            //doesnt currently create users
            return;
          }
  
          //translate update to add or remove
          if (isMembershipUpdate) {
            //see if add or remove
            WsHasMemberResults wsHasMemberResults = new GcHasMember().assignGroupName(groupName)
              .addSubjectLookup(new WsSubjectLookup(subjectId, sourceId, null)).execute();
  
            if (GrouperClientUtils.equals("IS_MEMBER", wsHasMemberResults.getResults()[0]
                .getResultMetadata().getResultCode())) {
              isMembershipAdd = true;
            } else {
              isMembershipDelete = true;
            }
          }
  
          if (isMembershipAdd) {
            if (remedyGroupWhichHasAllowedUsers) {
              GrouperWsCommandsForRemedy.retrieveGrouperUsers().put(username, new String[]{subjectId, sourceId, usernamePrefix});
  //            GrouperRemedyCommands.deprovisionOrUndeprovision(grouperRemedyUser, debugMap);
            } else {
              GrouperRemedyCommands.assignUserToRemedyGroup(grouperRemedyUser, groupInRemedy, true);
            }
          }
          
          if (isMembershipDelete) {
            if (remedyGroupWhichHasAllowedUsers) {
              //remove memberships in remedy
  //            for (Info info : grouperRemedyUser.getRemedyUser().getMemberships()) {
  //              //check role?
  //              GrouperRemedyGroup grouperRemedyGroup = remedyGroupNameToGroupMap.get(info.getGroup().getName());
  //              GrouperRemedyCommands.removeUserFromRemedyGroup(grouperRemedyUser, grouperRemedyGroup, true);
  //            }
  //            GrouperWsCommandsForRemedy.retrieveGrouperUsers().remove(username);
  //            GrouperRemedyCommands.deprovisionOrUndeprovision(grouperRemedyUser, debugMap);
            } else {
              GrouperRemedyCommands.removeUserFromRemedyGroup(grouperRemedyUser, groupInRemedy, false);
            }
          }
          
  
        } else {
          debugMap.put("invalidEventType", true);
        }
        
      }      
      
    } finally {
            
      incrementalRefreshInProgress = false;

      GrouperRemedyLog.remedyLog(debugMap, startTimeNanos);
    }

  }
  
  
  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext arg0) throws JobExecutionException {
    incrementalSync();
  }

  /**
   * 
   */
  public GrouperRemedyMessageConsumer() {
  }


}
