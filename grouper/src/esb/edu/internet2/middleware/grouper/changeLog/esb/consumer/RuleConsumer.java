/*
 * @author Chris Hyzer
 */
package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.beans.RulesMembershipBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Class to dispatch individual events for rules
 */
public class RuleConsumer extends ChangeLogConsumerBase {

  /** */
  private static final Log LOG = GrouperUtil.getLog(RuleConsumer.class);

  /**
   * @see ChangeLogConsumerBase#processChangeLogEntries(List, ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(
      List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {

    long currentId = -1;

    //try catch so we can track that we made some progress
    try {
      for (final ChangeLogEntry changeLogEntry : changeLogEntryList) {

        ChangeLogType changeLogType = changeLogEntry.getChangeLogType();

        currentId = changeLogEntry.getSequenceNumber();
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Processing event number " + currentId + ", " 
              + changeLogType.getChangeLogCategory() + ", " + changeLogType.getActionName());
        }
        
        //if this is a group type add action and category
        if (changeLogType.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_DELETE)) {
          
          String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId);
          
          //lets only do members list for now
          if (!StringUtils.equals(fieldId, Group.getDefaultList().getUuid())) {
            continue;
          }

          //must be flattened
          String membershipType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType);
          
          if (!StringUtils.equals("flattened", membershipType)) {
            continue;
          }
          
          GrouperSession grouperSession = GrouperSession.startRootSession(false);
          
          RulesMembershipBean rulesMembershipBean = null;
          
          try {
            rulesMembershipBean = (RulesMembershipBean)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
              
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

                String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.memberId);
                Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
                Subject subject = member.getSubject();
                String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId);
                Group group = GroupFinder.findByUuid(grouperSession, groupId, true);
                
                RulesMembershipBean rulesMembershipBean = new RulesMembershipBean(member, group, subject);

                return rulesMembershipBean;
              }
            });
          } finally {
            GrouperSession.stopQuietly(grouperSession);
          }
          
          //fire rules directly connected to this membership flat delete
          RuleEngine.fireRule(RuleCheckType.flattenedMembershipRemove, rulesMembershipBean);

          //fire rules related to add in stem
          RuleEngine.fireRule(RuleCheckType.flattenedMembershipRemoveInFolder, rulesMembershipBean);

        } else if (changeLogType.equalsCategoryAndAction(ChangeLogTypeBuiltin.MEMBERSHIP_ADD)) {
            
            String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId);
            
            //lets only do members list for now
            if (!StringUtils.equals(fieldId, Group.getDefaultList().getUuid())) {
              continue;
            }

            //must be flattened
            String membershipType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType);
            
            if (!StringUtils.equals("flattened", membershipType)) {
              continue;
            }
            
            GrouperSession grouperSession = GrouperSession.startRootSession(false);
            
            RulesMembershipBean rulesMembershipBean = null;
            
            try {
              rulesMembershipBean = (RulesMembershipBean)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {
                
                public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

                  String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.memberId);
                  Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
                  Subject subject = member.getSubject();
                  String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId);
                  Group group = GroupFinder.findByUuid(grouperSession, groupId, true);
                  
                  RulesMembershipBean rulesMembershipBean = new RulesMembershipBean(member, group, subject);

                  return rulesMembershipBean;
                }
              });
            } finally {
              GrouperSession.stopQuietly(grouperSession);
            }
            
            //fire rules directly connected to this membership flat delete
            RuleEngine.fireRule(RuleCheckType.flattenedMembershipAdd, rulesMembershipBean);

            //fire rules related to add in stem
            RuleEngine.fireRule(RuleCheckType.flattenedMembershipAddInFolder, rulesMembershipBean);



        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Unsupported event " + changeLogType.getChangeLogCategory() + ", " 
                + changeLogType.getActionName() + ", " + changeLogEntry.getSequenceNumber());
          }
        }

      }
      //we successfully processed this record

    } catch (Exception e) {
      LOG.error("problem", e);
      changeLogProcessorMetadata.registerProblem(e, "Error processing record " + currentId, currentId);
      //we made it to this -1
      return currentId - 1;
    }
    if (currentId == -1) {
      throw new RuntimeException("Couldn't process any records");
    }
    return currentId;
  }

}
