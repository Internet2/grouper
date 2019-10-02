/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.changeLog.*;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class GrouperDuoChangeLogConsumer extends ChangeLogConsumerBase {

  /**
   * 
   */
  public GrouperDuoChangeLogConsumer() {
    //schedule with job in grouper-loader.properties
    //otherJob.duo.class = edu.internet2.middleware.grouperDuo.GrouperDuoFullRefresh
    //otherJob.duo.quartzCron = 0 0 5 * * ?
//    GrouperDuoDaemon.scheduleJobsOnce();
  }

  /**
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase#processChangeLogEntries(java.util.List, edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {

    long currentId = -1;

    boolean startedGrouperSession = false;
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    if (grouperSession == null) {
      grouperSession = GrouperSession.startRootSession();
      startedGrouperSession = true;
    } else {
      grouperSession = grouperSession.internal_getRootSession();
    }

    HashMap<String, Object> debugMap = new HashMap<String, Object>();
    long startTime = System.nanoTime();
    
    //try catch so we can track that we made some progress
    try {
      for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
        currentId = changeLogEntry.getSequenceNumber();
        debugMap.put("currentEntryId", currentId);
 
        //if this is a group add action and category
        if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_ADD)) {
 
          String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.name);
          if (GrouperDuoUtils.validDuoGroupName(groupName)) {
            String groupExtension = GrouperUtil.extensionFromName(groupName);
            //get the group in grouper
            String groupDescription = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_ADD.description);
            //shouldnt be the case but check anyways
            if (!GrouperDuoCommands.retrieveGroups().containsKey(groupExtension)) {
              GrouperDuoCommands.createDuoGroup(groupExtension, groupDescription, true);
            }
          }
        } else if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_DELETE)) {
          String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_DELETE.name);
          if (GrouperDuoUtils.validDuoGroupName(groupName)) {
            String groupExtension = GrouperUtil.extensionFromName(groupName);
            //shouldnt be the case but check anyways
            GrouperDuoGroup grouperDuoGroup = GrouperDuoCommands.retrieveGroups().get(groupExtension);
            if (grouperDuoGroup != null) {
              GrouperDuoCommands.deleteDuoGroup(grouperDuoGroup.getId(), true);
            }
          }
 
        } if (changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.GROUP_UPDATE)) {
          String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.GROUP_UPDATE.name);
          if (GrouperDuoUtils.validDuoGroupName(groupName)) {
            String groupExtension = GrouperUtil.extensionFromName(groupName);
            //get the group in grouper
            
            Group group = GroupFinder.findByName(grouperSession, groupName, false);

            if (group != null) {
              
              //shouldnt be the case but check anyways
              Map<String, GrouperDuoGroup> groupNameToDuoGroupMap = GrouperDuoCommands.retrieveGroups();
              GrouperDuoGroup grouperDuoGroup = groupNameToDuoGroupMap.get(groupExtension);
              if (grouperDuoGroup != null) {
                GrouperDuoCommands.updateDuoGroup(grouperDuoGroup.getId(), group.getDescription(), true);
              }
            }
          }
        } 
        
        boolean isMembershipAdd = changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD);
        boolean isMembershipDelete = changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE);
        boolean isMembershipUpdate = changeLogEntry.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_UPDATE);

        debugMap.put("isMembershipAdd", isMembershipAdd);
        debugMap.put("isMembershipDelete", isMembershipDelete);
        debugMap.put("isMembershipUpdate", isMembershipUpdate);

        if (isMembershipAdd || isMembershipDelete || isMembershipUpdate) {
          String groupName;
          if (isMembershipAdd) {
            groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);
          }else if (isMembershipDelete) {
            groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName);
          }else {
            groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.groupName);
          }

          debugMap.put("groupName", groupName);
          debugMap.put("isAdminSyncEnabled", GrouperDuoUtils.isDuoAdminSyncEnabled());
          debugMap.put("isGroupSyncEnabled", GrouperDuoUtils.isDuoGroupSyncEnabled());
          debugMap.put("isValidGroup", GrouperDuoUtils.validDuoGroupName(groupName));
          debugMap.put("isValidAdminGroup", GrouperDuoUtils.isValidDuoAdminGroup(grouperSession, groupName));

          if (GrouperDuoUtils.isDuoGroupSyncEnabled() && GrouperDuoUtils.validDuoGroupName(groupName)) {
            String sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId);

            boolean inCorrectSubjectSource = GrouperDuoUtils.configSourcesForSubjects().contains(sourceId);
            
            if (inCorrectSubjectSource) {
              String groupExtension = GrouperUtil.extensionFromName(groupName);
              Group group = GroupFinder.findByName(grouperSession, groupName, false);
              Map<String, GrouperDuoGroup> groupNameToDuoGroupMap = GrouperDuoCommands.retrieveGroups();
              GrouperDuoGroup grouperDuoGroup = groupNameToDuoGroupMap.get(groupExtension);
              String subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
              
              String subjectAttributeForDuoUsername = GrouperDuoUtils.configSubjectAttributeForDuoUsername();
                
              String username = null;
              Subject subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
              
              if (StringUtils.equals("id", subjectAttributeForDuoUsername)) {
                username = subjectId;
              } else {
                
                if (subject != null) {
                  String attributeValue = subject.getAttributeValue(subjectAttributeForDuoUsername);
                  if (!StringUtils.isBlank(attributeValue)) {
                    username = attributeValue;
                  }                    
                }
              }
              
              String duoGroupId = grouperDuoGroup != null ? grouperDuoGroup.getId() : null;
              String duoUserId = !StringUtils.isBlank(username) ? GrouperDuoCommands.retrieveUserIdFromUsername(username) : null;
              
              //cant do anything if missing these things
              if (!StringUtils.isBlank(duoGroupId) && !StringUtils.isBlank(duoUserId)) {

                boolean userInDuoGroup = GrouperDuoCommands.userInGroup(duoUserId, duoGroupId, true);
                
                boolean addUserToGroup = isMembershipAdd;
                
                //if update it could have unexpired
                if (isMembershipUpdate && group != null && subject != null && group.hasMember(subject)) {
                  addUserToGroup = true;
                }
                
                //see if any update is needed
                if (addUserToGroup != userInDuoGroup) {
                  if (addUserToGroup) {
                    GrouperDuoCommands.assignUserToGroup(duoUserId, duoGroupId, true);
                  } else {
                    GrouperDuoCommands.removeUserFromGroup(duoUserId, duoGroupId, true);
                  }
                }
              }
            }
          }
          else if (GrouperDuoUtils.isDuoAdminSyncEnabled() && GrouperDuoUtils.isValidDuoAdminGroup(grouperSession, groupName)) {
            debugMap.put("method", "processChangeLogEntries - duoAdminSync");

            String sourceId;
            String subjectId;

            if (isMembershipAdd) {
              sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId);
              subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId);
            }else if (isMembershipDelete) {
              sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId);
              subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId);
            }else { // isMembershipUpdate
              sourceId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.sourceId);
              subjectId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_UPDATE.subjectId);
            }

            debugMap.put("subjectId", subjectId);
            debugMap.put("sourceId", sourceId);

            if (!GrouperDuoUtils.configSourcesForSubjects().contains(sourceId))
              continue;

        	Subject subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
            
            Member member = MemberFinder.findBySubject(grouperSession, subject, false);
            debugMap.put("memberId", member.getId());

            GrouperDuoAdministrator administrator = null;
            try {
	            administrator = GrouperDuoUtils.fetchOrCreateGrouperDuoAdministrator(
	                    member,
	                    !isMembershipDelete,
	                    GrouperDuoCommands.retrieveAdminAccounts()
	            );
            }catch (Exception e) {
            	GrouperDuoLog.logError(String.format("Failed to create administrator for subject: %s (%s) from %s, removing member from group.", subject.getName(), subject.getId(), subject.getSource()));
            	GrouperDuoUtils.removeSubjectFromDuoAdminGroups(grouperSession, subject);
            	
            	if (GrouperDuoUtils.configEmailRecipientsGroupName().length() > 0) {
                	GrouperDuoUtils.sendEmailToGroupMembers(
            			GroupFinder.findByName(grouperSession, GrouperDuoUtils.configEmailRecipientsGroupName(), false), 
            			"Failed to add member to Administrator Group.", 
            			String.format(
        					"There was an error while adding a member to an Administrative role. -- %s. Check the logs for more details.\n\nSubject Information:\nSubject Id: %s\nSubject Source: %s", 	
        					e.getMessage(),
        					subject.getId(),
        					subject.getSource()
    					)
        			);
            	}
            }

            if (administrator != null)
            	GrouperDuoUtils.synchronizeMemberAndDuoAdministrator(grouperSession, member, administrator);
          }
        }
 
        //we successfully processed this record
      }
    } catch (Exception e) {
      changeLogProcessorMetadata.registerProblem(e, "Error processing record", currentId);
      //we made it to this -1
      return currentId-1;
    } finally {
      if (startedGrouperSession) {
        GrouperSession.stopQuietly(grouperSession);
      }

      GrouperDuoLog.duoLog(debugMap, startTime);
    }
    if (currentId == -1) {
      throw new RuntimeException("Couldn't process any records");
    }
 
    return currentId;

  }

}
