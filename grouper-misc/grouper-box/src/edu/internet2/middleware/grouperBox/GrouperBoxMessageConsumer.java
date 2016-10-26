/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;

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

import com.box.sdk.BoxGroupMembership.Info;

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
public class GrouperBoxMessageConsumer implements Job {

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperBoxFullRefresh.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(System.currentTimeMillis());
    //incrementalSync();
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
      GrouperBoxFullRefresh.waitForFullRefreshToEnd();

      WsMessage[] wsMessages = GrouperWsCommandsForBox.grouperReceiveMessages();

      if (GrouperClientUtils.length(wsMessages) == 0) {
        boolean logIfNoMessages = GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperBox.logIfNoMessages", false);
        if (!logIfNoMessages) {
          logDebugMap = false;
        }
        return;
      }
      
      boolean fullSyncOnMessage = GrouperClientConfig.retrieveConfig().propertyValueBoolean(
          "grouperBox.fullSyncOnMessage", false);

      if (fullSyncOnMessage) {
        debugMap.put("fullSyncOnMessage", true);
      }
      
      int fullSyncOnMessageWaitSeconds = GrouperClientConfig.retrieveConfig().propertyValueInt(
          "grouperBox.fullSyncOnMessageWaitSeconds", 30);

      //short circuit to let full go
      if (GrouperBoxFullRefresh.isFullRefreshInProgress()) {
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
        //        "groupName":"box:groups:someGroup",
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

          if (GrouperBoxFullRefresh.getLastFullRefreshStart() > (esbEvent.getCreatedOnMicros() / 1000L)) {
            
            continue;
            
          }

          if (fullSyncOnMessage) {
            if (fullSyncOnMessageWaitSeconds < 5) {
              fullSyncOnMessageWaitSeconds = 5;
            }
            GrouperClientUtils.sleep(fullSyncOnMessageWaitSeconds * 1000L);
            
            try {
              incrementalRefreshInProgress = false;
              GrouperBoxFullRefresh.fullRefreshLogic();
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
          GrouperWsCommandsForBox.grouperAcknowledgeMessages(successMessageIds, "mark_as_processed");
        }
      } catch (Exception e) {
        debugMap.put("successAcknowledgeException", GrouperClientUtils.getFullStackTrace(e));
      }

      try {
        //mark messages as return to queue
        if (GrouperClientUtils.length(waitMessageIds) > 0) {
          GrouperWsCommandsForBox.grouperAcknowledgeMessages(waitMessageIds, "return_to_queue");
        }
      } catch (Exception e) {
        debugMap.put("waitAcknowledgeException", GrouperClientUtils.getFullStackTrace(e));
      }

      incrementalRefreshInProgress = false;

      if (logDebugMap) {
        GrouperBoxLog.boxLog(debugMap, startTimeNanos);
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
    String subjectAttributeForBoxUsername = GrouperBoxUtils.configSubjectAttributeForBoxUsername();
    String subjectAttributeValue = null;
    if (!GrouperClientUtils.equals("id", subjectAttributeForBoxUsername)) {
      // note make sure the loader is configured to send this attribute
      subjectAttributeValue = esbEvent.subjectAttribute(subjectAttributeForBoxUsername);
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
      //        "groupName":"box:groups:someGroup",
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
      
      boolean boxGroupWhichHasAllowedUsers = GrouperClientUtils.equals(groupName, GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.requireGroup"));
      
      if (!boxGroupWhichHasAllowedUsers && !GrouperBoxUtils.validBoxGroupName(groupName)) {
        debugMap.put("invalidGroupName", true);
        return;
      }
      String groupExtension = GrouperClientUtils.extensionFromName(groupName);

      boolean isMembershipAdd = GrouperClientUtils.equals(eventType, "MEMBERSHIP_ADD");
      boolean isMembershipUpdate = GrouperClientUtils.equals(eventType, "MEMBERSHIP_UPDATE");
      boolean isMembershipDelete = GrouperClientUtils.equals(eventType, "MEMBERSHIP_DELETE");

      //get groups from box
      Map<String, GrouperBoxGroup> boxGroupNameToGroupMap = GrouperBoxCommands.retrieveBoxGroups();

      GrouperBoxGroup groupInBox = boxGroupNameToGroupMap.get(groupExtension);

      if (GrouperClientUtils.equals(eventType, "GROUP_ADD")) {
        if (!boxGroupWhichHasAllowedUsers) {
          //create box group
          GrouperBoxCommands.createBoxGroup(groupExtension, true);
        }

      } else if (GrouperClientUtils.equals(eventType, "GROUP_DELETE")) {
        
        if (!boxGroupWhichHasAllowedUsers) {
          //create box group
          GrouperBoxCommands.deleteBoxGroup(groupInBox, true);
        }
      } else if (GrouperClientUtils.equals(eventType, "GROUP_UPDATE")) {
        
        if (!boxGroupWhichHasAllowedUsers) {
          if (groupInBox == null) {
            //hmmm, rename, do a full refresh?  need to delete old, create new, and add/remove memberships
            GrouperBoxFullRefresh.fullRefreshLogic();
          }
        }        
        
      } else if (isMembershipAdd || isMembershipDelete || isMembershipUpdate) {

        debugMap.put("sourceId", sourceId);

        boolean inCorrectSubjectSource = GrouperBoxUtils.configSourcesForSubjects().contains(sourceId);

        String username = null;

        if (inCorrectSubjectSource) {

          String subjectAttributeForBoxUsername = GrouperBoxUtils.configSubjectAttributeForBoxUsername();
          debugMap.put("subjectAttributeBoxUsername", subjectAttributeForBoxUsername);
         
          
          if (GrouperClientUtils.equals("id", subjectAttributeForBoxUsername)) {
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
        
        username += GrouperClientUtils.defaultIfBlank(GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.subjectIdSuffix"), "");
        
        debugMap.put("boxUsername", username);
        
        //lets get the user from box
        GrouperBoxUser grouperBoxUser = GrouperBoxUser.retrieveUsers().get(username);

        debugMap.put("boxUserExists", grouperBoxUser != null);

        if (grouperBoxUser == null) {
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
          if (boxGroupWhichHasAllowedUsers) {
            GrouperWsCommandsForBox.retrieveGrouperUsers().put(username, new String[]{subjectId, sourceId, usernamePrefix});
            GrouperBoxCommands.deprovisionOrUndeprovision(grouperBoxUser, debugMap);
          } else {
            GrouperBoxCommands.assignUserToBoxGroup(grouperBoxUser, groupInBox, true);
          }
        }
        
        if (isMembershipDelete) {
          if (boxGroupWhichHasAllowedUsers) {
            //remove memberships in box
            for (Info info : grouperBoxUser.getBoxUser().getMemberships()) {
              //check role?
              GrouperBoxGroup grouperBoxGroup = boxGroupNameToGroupMap.get(info.getGroup().getName());
              GrouperBoxCommands.removeUserFromBoxGroup(grouperBoxUser, grouperBoxGroup, true);
            }
            GrouperWsCommandsForBox.retrieveGrouperUsers().remove(username);
            GrouperBoxCommands.deprovisionOrUndeprovision(grouperBoxUser, debugMap);
          } else {
            GrouperBoxCommands.removeUserFromBoxGroup(grouperBoxUser, groupInBox, false);
          }
        }
        

      } else {
        debugMap.put("invalidEventType", true);
      }
      
      
      
    } finally {
            
      incrementalRefreshInProgress = false;

      GrouperBoxLog.boxLog(debugMap, startTimeNanos);
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
  public GrouperBoxMessageConsumer() {
  }


}
