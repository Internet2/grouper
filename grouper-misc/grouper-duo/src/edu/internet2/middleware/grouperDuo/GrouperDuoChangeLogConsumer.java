/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperDuo;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperDuoChangeLogConsumer extends ChangeLogConsumerBase {

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
    
    //try catch so we can track that we made some progress
    try {
      for (ChangeLogEntry changeLogEntry : changeLogEntryList) {
        currentId = changeLogEntry.getSequenceNumber();
 
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
        if (isMembershipAdd || isMembershipDelete || isMembershipUpdate) {
          String groupName = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName);

          if (GrouperDuoUtils.validDuoGroupName(groupName)) {
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
    }
    if (currentId == -1) {
      throw new RuntimeException("Couldnt process any records");
    }
 
    return currentId;

  }

}
