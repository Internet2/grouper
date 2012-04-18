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
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.rules.RuleCheckType;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.rules.beans.RulesMembershipBean;
import edu.internet2.middleware.grouper.rules.beans.RulesPermissionBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Class to dispatch individual events for rules
 * @author mchyzer
 */
public class RuleConsumer extends ChangeLogConsumerBase {

  /**
   * process events based on event type.  This is the category__action
   *
   */
  private static enum RuleEventType {

    /** add membership event */
    membership__addMembership {

      /**
       * @see RuleEventType#processEvent(ChangeLogType, ChangeLogEntry, RulesBean)
       */
      @Override
      public void processEvent(ChangeLogType changeLogType,
          ChangeLogEntry changeLogEntry, RulesBean rulesBean) {
        
        //fire rules directly connected to this membership flat delete
        RuleEngine.fireRule(RuleCheckType.flattenedMembershipAdd, rulesBean);

        //fire rules related to add in stem
        RuleEngine.fireRule(RuleCheckType.flattenedMembershipAddInFolder, rulesBean);

      }

      /**
       * @see RuleEventType#setupRulesBean(ChangeLogType, ChangeLogEntry, GrouperSession)
       */
      @Override
      public RulesBean setupRulesBean(ChangeLogType changeLogType,
          ChangeLogEntry changeLogEntry, GrouperSession grouperSession) {
        return setupRulesBeanMembership(changeLogType, changeLogEntry, grouperSession);
      }

      /**
       * @see RuleEventType#shouldProcess(ChangeLogType, ChangeLogEntry)
       */
      @Override
      public boolean shouldProcess(ChangeLogType changeLogType,
          ChangeLogEntry changeLogEntry) {
        return shouldProcessMembership(changeLogType, changeLogEntry);
      }
    },
    
    /** delete membership event */
    membership__deleteMembership {

      /**
       * @see RuleEventType#processEvent(ChangeLogType, ChangeLogEntry, RulesBean)
       */
      @Override
      public void processEvent(ChangeLogType changeLogType,
          ChangeLogEntry changeLogEntry, RulesBean rulesBean) {
        
        //fire rules directly connected to this membership flat delete
        RuleEngine.fireRule(RuleCheckType.flattenedMembershipRemove, rulesBean);

        //fire rules related to membership flat delete in folder
        RuleEngine.fireRule(RuleCheckType.flattenedMembershipRemoveInFolder, rulesBean);
        
      }

      /**
       * @see RuleEventType#setupRulesBean(ChangeLogType, ChangeLogEntry, GrouperSession)
       */
      @Override
      public RulesBean setupRulesBean(ChangeLogType changeLogType,
          ChangeLogEntry changeLogEntry, GrouperSession grouperSession) {
        return setupRulesBeanMembership(changeLogType, changeLogEntry, grouperSession);
      }

      /**
       * @see RuleEventType#shouldProcess(ChangeLogType, ChangeLogEntry)
       */
      @Override
      public boolean shouldProcess(ChangeLogType changeLogType,
          ChangeLogEntry changeLogEntry) {
        return shouldProcessMembership(changeLogType, changeLogEntry);
      }
    };


    /** 
     * if this record should be processed
     * @param changeLogType
     * @param changeLogEntry
     * @return true if the record should be processed
     */
    public abstract boolean shouldProcess(ChangeLogType changeLogType, ChangeLogEntry changeLogEntry);
    
    /**
     * setup a rules bean, this will be called in the context of a root session
     * @param changeLogType
     * @param changeLogEntry
     * @param grouperSession
     * @return the rules bean
     */
    public abstract RulesBean setupRulesBean(ChangeLogType changeLogType, ChangeLogEntry changeLogEntry, GrouperSession grouperSession);
    
    /**
     * process an event which matches the category and type
     * @param changeLogType
     * @param changeLogEntry
     * @param rulesBean 
     */
    public abstract void processEvent(ChangeLogType changeLogType, ChangeLogEntry changeLogEntry, RulesBean rulesBean);
    
    /**
     * do a case-insensitive matching
     * 
     * @param string
     * @param exceptionOnNull will not allow null or blank entries
     * @param exceptionIfInvalid true if there should be an exception if invalid
     * @return the enum or null or exception if not found
     */
    public static RuleConsumer.RuleEventType valueOfIgnoreCase(String string, boolean exceptionOnNull, boolean exceptionIfInvalid) {
      return GrouperUtil.enumValueOfIgnoreCase(RuleConsumer.RuleEventType.class, 
          string, exceptionOnNull, exceptionIfInvalid);
    }
  };
  
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

        final ChangeLogType changeLogType = changeLogEntry.getChangeLogType();

        currentId = changeLogEntry.getSequenceNumber();
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Processing event number " + currentId + ", " 
              + changeLogType.getChangeLogCategory() + ", " + changeLogType.getActionName());
        }

        String enumKey = changeLogType.getChangeLogCategory() + "__" + changeLogType.getActionName();

        final RuleEventType ruleEventType = RuleEventType.valueOfIgnoreCase(enumKey, false, false);
        
        if (ruleEventType != null) {
          
          if (!ruleEventType.shouldProcess(changeLogType, changeLogEntry)) {
            continue;
          }

          GrouperSession theGrouperSession = GrouperSession.startRootSession(false);
          
          RulesBean rulesBean = null;
          
          try {
            rulesBean = (RulesBean)GrouperSession.callbackGrouperSession(theGrouperSession, new GrouperSessionHandler() {
              
              public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
                return ruleEventType.setupRulesBean(changeLogType, changeLogEntry, grouperSession);
              }
            });
          } finally {
            GrouperSession.stopQuietly(theGrouperSession);
          }

          if (rulesBean != null) {
            ruleEventType.processEvent(changeLogType, changeLogEntry, rulesBean);
          }
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

  /**
   * 
   * @param changeLogType
   * @param changeLogEntry
   * @return true if should process, false if not
   */
  private static boolean shouldProcessMembership(ChangeLogType changeLogType, ChangeLogEntry changeLogEntry) {
    String fieldId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId);
    
    //lets only do members list for now
    if (!StringUtils.equals(fieldId, Group.getDefaultList().getUuid())) {
      return false;
    }

    //must be flattened
    String membershipType = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.membershipType);
    
    if (!StringUtils.equals("flattened", membershipType)) {
      return false;
    }
    
    return true;
  }
  
  /**
   * 
   * @param changeLogType
   * @param changeLogEntry
   * @return true if should process, false if not
   */
  private static boolean shouldProcessPermission(ChangeLogType changeLogType, ChangeLogEntry changeLogEntry) {
    
//    There is no more flattened...
//    //must be flattened
//    String permissionType = changeLogEntry.retrieveValueForLabel("permissionType");
//    
//    if (!StringUtils.equals("flattened", permissionType)) {
//      return false;
//    }
    
    String memberId = changeLogEntry.retrieveValueForLabel("memberId");
    
    //if this is assigned to a role, and not a user, skip it
    if (StringUtils.isBlank(memberId)) {
      return false;
    }
    
    return true;
  }
  
  /**
   * 
   * @param changeLogType
   * @param changeLogEntry
   * @param grouperSession
   * @return the rules bean
   */
  private static RulesBean setupRulesBeanMembership(ChangeLogType changeLogType, ChangeLogEntry changeLogEntry, GrouperSession grouperSession) {
    
    String memberId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.memberId);
    Member member = MemberFinder.findByUuid(grouperSession, memberId, true);

    if (member == null) {
      return null;
    }
    
    Subject subject = member.getSubject();
    String groupId = changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId);
    Group group = GroupFinder.findByUuid(grouperSession, groupId, false);
    
    if (group == null) {
      return null;
    }
    
    RulesMembershipBean rulesMembershipBean = new RulesMembershipBean(member, group, subject);

    return rulesMembershipBean;

  }
  
  /**
   * 
   * @param changeLogType
   * @param changeLogEntry
   * @param grouperSession
   * @return the rules bean
   */
  private static RulesBean setupRulesBeanPermission(ChangeLogType changeLogType, ChangeLogEntry changeLogEntry, GrouperSession grouperSession) {

    AttributeAssign attributeAssign = null;
// CH this went away, not in change log anymore...
//    String attributeAssignId = changeLogEntry.retrieveValueForLabel("attributeAssignId");
//
//    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, false);
//    if (attributeAssign == null) {
//      return null;
//    }

    String memberId = changeLogEntry.retrieveValueForLabel("memberId");

    Member member = MemberFinder.findByUuid(grouperSession, memberId, false);
    
    if (member == null) {
      return null;
    }
    Role role = null;
// CH this went away, not in change log anymore...
//    String roleId = changeLogEntry.retrieveValueForLabel("roleId");
//
//    Role role = GroupFinder.findByUuid(grouperSession, roleId, false);
//    
//    if (role == null) {
//      return null;
//    }
    
    String attributeDefNameId = changeLogEntry.retrieveValueForLabel("attributeDefNameId");

    AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
    
    if (attributeDefName == null) {
      return null;
    }

    AttributeDef attributeDef = attributeDefName.getAttributeDef();

    String action = changeLogEntry.retrieveValueForLabel("action");

    RulesPermissionBean rulesPermissionBean = new RulesPermissionBean(attributeAssign, role, member, attributeDefName, attributeDef, action);

    return rulesPermissionBean;

  }
  
}
