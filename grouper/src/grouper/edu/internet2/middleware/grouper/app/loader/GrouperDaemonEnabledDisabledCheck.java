/**
 * Copyright 2022 Internet2
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
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 */
public class GrouperDaemonEnabledDisabledCheck {

  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonEnabledDisabledCheck.class);
   
  private static long lastQuery = -1;
  
  private static Set<String> cachedMembershipIds = new HashSet<String>();
  private static Set<String> cachedAttributeAssignIds = new HashSet<String>();
  private static Set<String> cachedGroupIds = new HashSet<String>();
  
  /**
   * @param hib3GrouploaderLog
   * @return number of updates
   */
  public synchronized static int fixEnabledDisabled() {
    int records = 0;
    
    if (System.currentTimeMillis() > lastQuery) {
      long queryTime = System.currentTimeMillis() + (1000L * GrouperLoaderConfig.retrieveConfig().propertyValueInt("changeLog.enabledDisabled.queryIntervalInSeconds", 3600));
      
      records += internal_groupsFixEnabledDisabled(queryTime);
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      records += internal_membershipsFixEnabledDisabled(queryTime);
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      records += internal_attributeAssignsFixEnabledDisabled(queryTime);
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      lastQuery = queryTime;
    } else {
      records += internal_groupsFixEnabledDisabledUsingCache();
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      records += internal_membershipsFixEnabledDisabledUsingCache();
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      records += internal_attributeAssignsFixEnabledDisabledUsingCache();
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }

    // hopefully this is always quick anyways, if not need to adjust it
    records += ExternalSubject.internal_fixDisabled();

    return records;
  }
  
  /**
   * for testing
   */
  public static void internal_clearCache() {
    lastQuery = -1;
  }
  
  private static int internal_groupsFixEnabledDisabledUsingCache() {
    int updates = 0;
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Checking " + cachedGroupIds.size() + " cached groups.");
    }
    
    if (cachedGroupIds.size() > 0) {
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findByUuids(cachedGroupIds, false);
      for (Group group : groups) {
        boolean isEnabledUsingTimestamps = group.internal_isEnabledUsingTimestamps();
        
        if (isEnabledUsingTimestamps != group.isEnabled()) {
          group.setEnabled(isEnabledUsingTimestamps);
          group.store();
          updates++;
          cachedGroupIds.remove(group.getId());
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Updated group " + group.getId() + " with enabled=" + isEnabledUsingTimestamps);
          }
        }
      }
    }
    
    return updates;
  }
  
  private static int internal_attributeAssignsFixEnabledDisabledUsingCache() {
    int updates = 0;
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Checking " + cachedAttributeAssignIds.size() + " cached attribute assigns.");
    }
    
    if (cachedAttributeAssignIds.size() > 0) {
      Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findByIds(cachedAttributeAssignIds, null, false);
      for (AttributeAssign attributeAssign : attributeAssigns) {
        boolean isEnabledUsingTimestamps = attributeAssign.internal_isEnabledUsingTimestamps();
        
        if (isEnabledUsingTimestamps != attributeAssign.isEnabled()) {
          attributeAssign.setEnabled(isEnabledUsingTimestamps);
          GrouperDAOFactory.getFactory().getAttributeAssign().saveOrUpdate(attributeAssign);
          updates++;
          cachedAttributeAssignIds.remove(attributeAssign.getId());
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Updated attribute assign " + attributeAssign.getId() + " with enabled=" + isEnabledUsingTimestamps);
          }
        }
      }
    }
    
    return updates;
  }
  
  private static int internal_membershipsFixEnabledDisabledUsingCache() {
    int updates = 0;
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Checking " + cachedMembershipIds.size() + " cached memberships.");
    }
    
    if (cachedMembershipIds.size() > 0) {
      Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership().findByImmediateUuids(cachedMembershipIds);
      for (Membership membership : memberships) {
        boolean isEnabledUsingTimestamps = membership.internal_isEnabledUsingTimestamps();
        
        if (isEnabledUsingTimestamps != membership.isEnabled()) {
          membership.setEnabled(isEnabledUsingTimestamps);
          updateMembershipWithAuditing(membership);
          updates++;
          cachedMembershipIds.remove(membership.getImmediateMembershipId());
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Updated membership " + membership.getImmediateMembershipId() + " with enabled=" + isEnabledUsingTimestamps);
          }
        }
      }
    }
    
    return updates;
  }
  
  /**
   * fix enabled and disabled groups, and return the count of how many were fixed
   * @param queryTime
   * @return the number of records affected
   */
  public static int internal_groupsFixEnabledDisabled(long queryTime) {

    cachedGroupIds = new HashSet<String>();
    
    int updates = 0;
    Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findAllEnabledDisabledMismatch(queryTime);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Found " + groups.size() + " groups with mismatch using query time: " + queryTime);
    }
    
    for (Group group : groups) {
      boolean isEnabledUsingTimestamps = group.internal_isEnabledUsingTimestamps();
      
      if (isEnabledUsingTimestamps != group.isEnabled()) {
        group.setEnabled(isEnabledUsingTimestamps);
        group.store();
        updates++;
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Updated group " + group.getId() + " with enabled=" + isEnabledUsingTimestamps);
        }
      } else {
        // cache to check later
        cachedGroupIds.add(group.getId());
      }
    }

    return updates;
  }
  

  /**
   * fix enabled and disabled attribute assigns, and return the count of how many were fixed
   * @param queryTime
   * @return the number of records affected
   */
  public static int internal_attributeAssignsFixEnabledDisabled(long queryTime) {
    cachedAttributeAssignIds = new HashSet<String>();
    
    int updates = 0;
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAllEnabledDisabledMismatch(queryTime);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Found " + attributeAssigns.size() + " attribute assigns with mismatch using query time: " + queryTime);
    }
    
    for (AttributeAssign attributeAssign : attributeAssigns) {
      boolean isEnabledUsingTimestamps = attributeAssign.internal_isEnabledUsingTimestamps();

      if (isEnabledUsingTimestamps != attributeAssign.isEnabled()) {
        attributeAssign.setEnabled(isEnabledUsingTimestamps);
        GrouperDAOFactory.getFactory().getAttributeAssign().saveOrUpdate(attributeAssign);
        updates++;
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Updated attribute assign " + attributeAssign.getId() + " with enabled=" + isEnabledUsingTimestamps);
        }
      } else {
        // cache to check later
        cachedAttributeAssignIds.add(attributeAssign.getId());
      }
    }
    
    return updates;
  }
  
  /**
   * fix enabled and disabled memberships, and return the count of how many were fixed
   * @param queryTime
   * @return the number of records affected
   */
  public static int internal_membershipsFixEnabledDisabled(long queryTime) {
    cachedMembershipIds = new HashSet<String>();
    
    int updates = 0;
    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership().findAllEnabledDisabledMismatch(queryTime);
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Found " + memberships.size() + " memberships with mismatch using query time: " + queryTime);
    }
    
    for (Membership membership : memberships) {
      boolean isEnabledUsingTimestamps = membership.internal_isEnabledUsingTimestamps();
      
      if (isEnabledUsingTimestamps != membership.isEnabled()) {
        membership.setEnabled(isEnabledUsingTimestamps);
        updateMembershipWithAuditing(membership);
        updates++;
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Updated membership " + membership.getImmediateMembershipId() + " with enabled=" + isEnabledUsingTimestamps);
        }
      } else {
        // cache to check later
        cachedMembershipIds.add(membership.getImmediateMembershipId());
      }
    }

    return updates;
  }
  
  private static void updateMembershipWithAuditing(Membership membership) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_AUDIT, new HibernateHandler() {
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
        GrouperDAOFactory.getFactory().getMembership().update(membership);
        
        if (membership.getField().isGroupListField() || membership.getField().isEntityListField()) {

          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.MEMBERSHIP_GROUP_DELETE, 
              "id", membership.getUuid(), "fieldId", membership.getFieldId(),
                  "fieldName", membership.getField().getName(), "memberId",  membership.getMemberUuid(),
                  "membershipType", membership.getType(), 
                  "groupId", membership.getOwnerGroupId(), "groupName", membership.getOwnerGroup().getName());
          auditEntry.setDescription("Expired membership: group: " + membership.getGroupName()
              + ", subject: " + membership.getMember().getSubjectSourceId() + "." + membership.getMemberSubjectId() + ", field: "
              + membership.getField().getName());
          auditEntry.saveOrUpdate(true);

        } else if (membership.getField().isGroupAccessField()) {
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_GROUP_DELETE, 
              "privilegeType", "access",
                  "privilegeName", AccessPrivilege.listToPriv(membership.getField().getName()).getName(), "memberId",  membership.getMemberUuid(),
                  "groupId", membership.getOwnerGroupId(), "groupName", membership.getOwnerGroup().getName());
          
          auditEntry.setDescription("Expired privilege: group: " + membership.getGroupName()
              + ", subject: " +  membership.getMember().getSubjectSourceId() + "." + membership.getMemberSubjectId() + ", privilege: "
              + AccessPrivilege.listToPriv(membership.getField().getName()).getName());

          auditEntry.saveOrUpdate(true);

        } else if (membership.getField().isStemListField()) {
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_STEM_DELETE, 
              "privilegeType", "naming",
                  "privilegeName", NamingPrivilege.listToPriv(membership.getField().getName()).getName(), "memberId",  membership.getMemberUuid(),
                  "stemId", membership.getOwnerStemId(), "stemName", membership.getOwnerStem().getName());
          
          auditEntry.setDescription("Expired privilege: stem: " + membership.getOwnerStem().getName()
              + ", subject: " +  membership.getMember().getSubjectSourceId() + "." + membership.getMemberSubjectId() + ", privilege: "
              + NamingPrivilege.listToPriv(membership.getField().getName()).getName());

          auditEntry.saveOrUpdate(true);
        } else if (membership.getField().isAttributeDefListField()) {
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.PRIVILEGE_ATTRIBUTE_DEF_DELETE, 
              "privilegeType", "attributeDef",
                  "privilegeName", AttributeDefPrivilege.listToPriv(membership.getField().getName()).getName(), "memberId",  membership.getMemberUuid(),
                  "attributeDefId", membership.getOwnerAttrDefId(), "attributeDefName", membership.getOwnerAttributeDef().getName());
          
          auditEntry.setDescription("Expired privilege: attributeDef: " + membership.getOwnerAttributeDef().getName()
              + ", subject: " +  membership.getMember().getSubjectSourceId() + "." + membership.getMemberSubjectId() + ", privilege: "
              + AttributeDefPrivilege.listToPriv(membership.getField().getName()).getName());

          auditEntry.saveOrUpdate(true);
        } else {
          LOG.error("Not expecting privilege: " + membership.getField());
        } 
        return null;
      }
    });
  }
}
