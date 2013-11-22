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
package edu.internet2.middleware.grouper.pit;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITMembership extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** owner_id */
  public static final String COLUMN_OWNER_ID = "owner_id";

  /** owner_attr_def_id */
  public static final String COLUMN_OWNER_ATTR_DEF_ID= "owner_attr_def_id";
  
  /** owner_group_id */
  public static final String COLUMN_OWNER_GROUP_ID = "owner_group_id";
  
  /** owner_stem_id */
  public static final String COLUMN_OWNER_STEM_ID = "owner_stem_id";
  
  /** member_id */
  public static final String COLUMN_MEMBER_ID = "member_id";
  
  /** field_id */
  public static final String COLUMN_FIELD_ID = "field_id";

  /** column */
  public static final String COLUMN_SOURCE_ID = "source_id";
  
  
  /** constant for field name for: sourceId */
  public static final String FIELD_SOURCE_ID = "sourceId";
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ownerId */
  public static final String FIELD_OWNER_ID = "ownerId";
  
  /** constant for field name for: ownerAttrDefId */
  public static final String FIELD_OWNER_ATTR_DEF_ID = "ownerAttrDefId";
  
  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";
  
  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";
  
  /** constant for field name for: memberId */
  public static final String FIELD_MEMBER_ID = "memberId";
  
  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";
  

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_OWNER_ID, FIELD_OWNER_ATTR_DEF_ID, FIELD_OWNER_GROUP_ID,
      FIELD_OWNER_STEM_ID, FIELD_MEMBER_ID, FIELD_FIELD_ID,
      FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB, FIELD_SOURCE_ID);

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_CONTEXT_ID, FIELD_END_TIME_DB, FIELD_FIELD_ID, 
      FIELD_ID, FIELD_MEMBER_ID, FIELD_OWNER_ATTR_DEF_ID, FIELD_OWNER_GROUP_ID, 
      FIELD_OWNER_ID, FIELD_OWNER_STEM_ID, FIELD_START_TIME_DB, FIELD_SOURCE_ID);


  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_MEMBERSHIPS = "grouper_pit_memberships";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** ownerId */
  private String ownerId;
  
  /** ownerAttrDefId */
  private String ownerAttrDefId;
  
  /** ownerGroupId */
  private String ownerGroupId;
  
  /** ownerStemId */
  private String ownerStemId;
  
  /** memberId */
  private String memberId;
  
  /** fieldId */
  private String fieldId;
  
  /** member */
  private PITMember member;
  
  /** sourceId */
  private String sourceId;
  
  /**
   * @return source id
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * set source id
   * @param sourceId
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }
  
  /** whether there will be flat membership notifications when this object is saved or updated */ 
  private boolean flatMembershipNotificationsOnSaveOrUpdate = false;
  
  /** whether there will be flat privilege notifications when this object is saved or updated */ 
  private boolean flatPrivilegeNotificationsOnSaveOrUpdate = false;
  
  /** whether there will be notifications for roles with permission changes when this object is saved or updated */ 
  private boolean notificationsForRolesWithPermissionChangesOnSaveOrUpdate = false;
  
  /**
   * @return boolean
   */
  public boolean getNotificationsForRolesWithPermissionChangesOnSaveOrUpdate() {
    return notificationsForRolesWithPermissionChangesOnSaveOrUpdate;
  }
  
  /**
   * @param notificationsForRolesWithPermissionChangesOnSaveOrUpdate
   */
  public void setNotificationsForRolesWithPermissionChangesOnSaveOrUpdate(boolean notificationsForRolesWithPermissionChangesOnSaveOrUpdate) {
    this.notificationsForRolesWithPermissionChangesOnSaveOrUpdate = notificationsForRolesWithPermissionChangesOnSaveOrUpdate;
  }
  
  /**
   * @return boolean
   */
  public boolean getFlatMembershipNotificationsOnSaveOrUpdate() {
    return flatMembershipNotificationsOnSaveOrUpdate;
  }
  
  /**
   * @param flatMembershipNotificationsOnSaveOrUpdate
   */
  public void setFlatMembershipNotificationsOnSaveOrUpdate(boolean flatMembershipNotificationsOnSaveOrUpdate) {
    this.flatMembershipNotificationsOnSaveOrUpdate = flatMembershipNotificationsOnSaveOrUpdate;
  }
  
  /**
   * @return boolean
   */
  public boolean getFlatPrivilegeNotificationsOnSaveOrUpdate() {
    return flatPrivilegeNotificationsOnSaveOrUpdate;
  }
  
  /**
   * @param flatPrivilegeNotificationsOnSaveOrUpdate
   */
  public void setFlatPrivilegeNotificationsOnSaveOrUpdate(boolean flatPrivilegeNotificationsOnSaveOrUpdate) {
    this.flatPrivilegeNotificationsOnSaveOrUpdate = flatPrivilegeNotificationsOnSaveOrUpdate;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public PITMembership clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }

  /**
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * save this object
   */
  public void save() {

    // may need to create new child objects if this object is being re-enabled..
    Set<PITMembership> existingAll = GrouperDAOFactory.getFactory().getPITMembership().findBySourceId(this.getSourceId(), false);
    GrouperDAOFactory.getFactory().getPITMembership().saveOrUpdate(this);
    if (!this.isActive()) {
      return;
    }
    
    for (PITMembership existing : existingAll) {

      // add new assignments and end dates to existing ones.
      Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findActiveByOwnerPITMembershipId(existing.getId());
      for (PITAttributeAssign assignment : assignments) {
        PITAttributeAssign assignmentCopy = assignment.clone();

        assignment.setEndTimeDb(this.getStartTimeDb());
        assignment.setActiveDb("F");
        assignment.setContextId(this.getContextId());
        assignment.update();
        
        assignmentCopy.setId(GrouperUuid.getUuid());
        assignmentCopy.setOwnerMembershipId(this.getId());
        assignmentCopy.setStartTimeDb(this.getStartTimeDb());
        assignmentCopy.setContextId(this.getContextId());
        assignmentCopy.setHibernateVersionNumber(-1L);
        assignmentCopy.save();
      }
    }
  }
  
  /**
   * update this object
   */
  public void update() {
    GrouperDAOFactory.getFactory().getPITMembership().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITMembership().delete(this);
  }

  /**
   * @return ownerId
   */
  public String getOwnerId() {
    return ownerId;
  }

  /**
   * @param ownerId
   */
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * @return ownerAttrDefId
   */
  public String getOwnerAttrDefId() {
    return ownerAttrDefId;
  }

  /**
   * @param ownerAttrDefId
   */
  public void setOwnerAttrDefId(String ownerAttrDefId) {
    this.ownerAttrDefId = ownerAttrDefId;
    if (ownerAttrDefId != null) {
      setOwnerId(ownerAttrDefId);
    }
  }

  /**
   * @return ownerGroupId
   */
  public String getOwnerGroupId() {
    return ownerGroupId;
  }

  /**
   * @param ownerGroupId
   */
  public void setOwnerGroupId(String ownerGroupId) {
    this.ownerGroupId = ownerGroupId;
    if (ownerGroupId != null) {
      setOwnerId(ownerGroupId);
    }
  }

  /**
   * @return ownerStemId
   */
  public String getOwnerStemId() {
    return ownerStemId;
  }

  /**
   * @param ownerStemId
   */
  public void setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
    if (ownerStemId != null) {
      setOwnerId(ownerStemId);
    }
  }

  /**
   * @return memberId
   */
  public String getMemberId() {
    return memberId;
  }

  /**
   * @param memberId
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }
  
  /**
   * @param member
   */
  public void setMember(PITMember member) {
    this.member = member;
  }
  
  /**
   * @return member
   */
  public PITMember getMember() {
    if (this.member != null) {
      return this.member;
    }
    
    this.member = GrouperDAOFactory.getFactory().getPITMember().findById(this.memberId, true);
    return this.member;
  }

  /**
   * @return fieldId
   */
  public String getFieldId() {
    return fieldId;
  }

  /**
   * @param fieldId
   */
  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    // add change log entry for flat memberships
    if (this.isActive() && (this.getFlatMembershipNotificationsOnSaveOrUpdate() || this.getFlatPrivilegeNotificationsOnSaveOrUpdate())) {
      Set<ChangeLogEntry> changeLogEntryBatch = new LinkedHashSet<ChangeLogEntry>();
      int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
      if (batchSize <= 0) {
        batchSize = 1;
      }

      Set<PITGroupSet> pitGroupSets = GrouperDAOFactory.getFactory().getPITMembershipView().findPITGroupSetsJoinedWithNewPITMembership(this);
      Iterator<PITGroupSet> iter = pitGroupSets.iterator();
      while (iter.hasNext()) {
        PITGroupSet pitGroupSet = iter.next();
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(pitGroupSet.getFieldId(), true);
        String ownerId = null;
        String ownerName = null;
        String privilegeName = null;
        String privilegeType = null;
        String ownerType = null;
        boolean isMembership = false;
        
        if (pitGroupSet.getOwnerGroupId() != null) {
          PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(pitGroupSet.getOwnerId(), true);
          ownerId = pitGroup.getSourceId();
          ownerName = pitGroup.getName();
          if (pitField.getType().equals(FieldType.LIST.getType())) {
            isMembership = true;
          } else {
            privilegeType = FieldType.ACCESS.getType();
            privilegeName = AccessPrivilege.listToPriv(pitField.getName()).getName();
            ownerType = Membership.OWNER_TYPE_GROUP;
          }
        } else if (pitGroupSet.getOwnerStemId() != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(pitGroupSet.getOwnerId(), true);
          ownerId = pitStem.getSourceId();
          ownerName = pitStem.getName();
          privilegeType = FieldType.NAMING.getType();
          privilegeName = NamingPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_STEM;
        } else if (pitGroupSet.getOwnerAttrDefId() != null) {
          PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(pitGroupSet.getOwnerId(), true);
          ownerId = pitAttributeDef.getSourceId();
          ownerName = pitAttributeDef.getName();
          privilegeType = FieldType.ATTRIBUTE_DEF.getType();
          privilegeName = AttributeDefPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_ATTRIBUTE_DEF;
        } else {
          throw new RuntimeException("Unable to determine owner of PIT Group Set: " + pitGroupSet.getId());
        }
        
        ChangeLogEntry changeLogEntry = null;
        if (isMembership && this.getFlatMembershipNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.MEMBERSHIP_ADD,
              ChangeLogLabels.MEMBERSHIP_ADD.id.name(), this.getSourceId(),
              ChangeLogLabels.MEMBERSHIP_ADD.fieldName.name(), pitField.getName(),
              ChangeLogLabels.MEMBERSHIP_ADD.fieldId.name(), pitField.getSourceId(),
              ChangeLogLabels.MEMBERSHIP_ADD.memberId.name(), this.getMember().getSourceId(),
              ChangeLogLabels.MEMBERSHIP_ADD.subjectId.name(), this.getMember().getSubjectId(),
              ChangeLogLabels.MEMBERSHIP_ADD.sourceId.name(), this.getMember().getSubjectSourceId(),
              ChangeLogLabels.MEMBERSHIP_ADD.groupId.name(), ownerId,
              ChangeLogLabels.MEMBERSHIP_ADD.membershipType.name(), "flattened",
              ChangeLogLabels.MEMBERSHIP_ADD.groupName.name(), ownerName);
        } else if (!isMembership && this.getFlatPrivilegeNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PRIVILEGE_ADD,
              ChangeLogLabels.PRIVILEGE_ADD.id.name(), this.getSourceId(),
              ChangeLogLabels.PRIVILEGE_ADD.privilegeName.name(), privilegeName,
              ChangeLogLabels.PRIVILEGE_ADD.fieldId.name(), pitField.getSourceId(),
              ChangeLogLabels.PRIVILEGE_ADD.memberId.name(), this.getMember().getSourceId(),
              ChangeLogLabels.PRIVILEGE_ADD.subjectId.name(), this.getMember().getSubjectId(),
              ChangeLogLabels.PRIVILEGE_ADD.sourceId.name(), this.getMember().getSubjectSourceId(),
              ChangeLogLabels.PRIVILEGE_ADD.privilegeType.name(), privilegeType,
              ChangeLogLabels.PRIVILEGE_ADD.ownerType.name(), ownerType,
              ChangeLogLabels.PRIVILEGE_ADD.ownerId.name(), ownerId,
              ChangeLogLabels.PRIVILEGE_ADD.membershipType.name(), "flattened",
              ChangeLogLabels.PRIVILEGE_ADD.ownerName.name(), ownerName);
        }

        if (changeLogEntry != null) {
          changeLogEntry.setContextId(this.getContextId());
          changeLogEntry.setCreatedOnDb(this.getStartTimeDb());
          changeLogEntryBatch.add(changeLogEntry);
          if (changeLogEntryBatch.size() % batchSize == 0) {
            GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, false);
            changeLogEntryBatch.clear();
          }
        }
      }
      
      // make sure all changes get made      
      if (changeLogEntryBatch.size() > 0) {
        GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, false);
        changeLogEntryBatch.clear();
      }
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    // add change log entry for permissions
    if (!this.isActive() && this.dbVersion().isActive() && this.getNotificationsForRolesWithPermissionChangesOnSaveOrUpdate()) {
      Set<ChangeLogEntry> changeLogEntryBatch = new LinkedHashSet<ChangeLogEntry>();
      int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
      if (batchSize <= 0) {
        batchSize = 1;
      }

      Set<PITGroup> roles = GrouperDAOFactory.getFactory().getPITGroup().findRolesWithPermissionsContainingObject(this);

      for (PITGroup role : roles) {
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE,
            ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId.name(), role.getSourceId(),
            ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName.name(), role.getName());
            
        changeLogEntry.setContextId(this.getContextId());
        changeLogEntry.setCreatedOnDb(this.getStartTimeDb());
        changeLogEntryBatch.add(changeLogEntry);
        if (changeLogEntryBatch.size() % batchSize == 0) {
          GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, false);
          changeLogEntryBatch.clear();
        }
      }
      
      // make sure all changes get made      
      if (changeLogEntryBatch.size() > 0) {
        GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, false);
        changeLogEntryBatch.clear();
      }
    }
    
    // add change log entry for flat memberships
    if (!this.isActive() && this.dbVersion().isActive() && (this.getFlatMembershipNotificationsOnSaveOrUpdate() || this.getFlatPrivilegeNotificationsOnSaveOrUpdate())) {
      Set<ChangeLogEntry> changeLogEntryBatch = new LinkedHashSet<ChangeLogEntry>();
      int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
      if (batchSize <= 0) {
        batchSize = 1;
      }
      
      String subjectName = null;
      
      // get the subject name if the subject is a group
      if (this.getMember().getSubjectTypeId().equals("group")) {
        PITGroup memberGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(this.getMember().getSubjectId(), false);
        
        if (memberGroup != null) {
          subjectName = memberGroup.getName();
        }
      }        

      Set<PITGroupSet> pitGroupSets = GrouperDAOFactory.getFactory().getPITMembershipView().findPITGroupSetsJoinedWithOldPITMembership(this);
      Iterator<PITGroupSet> iter = pitGroupSets.iterator();
      while (iter.hasNext()) {
        PITGroupSet pitGroupSet = iter.next();
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(pitGroupSet.getFieldId(), true);
        String ownerId = null;
        String ownerName = null;
        String privilegeName = null;
        String privilegeType = null;
        String ownerType = null;
        boolean isMembership = false;
        
        if (pitGroupSet.getOwnerGroupId() != null) {
          PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(pitGroupSet.getOwnerId(), true);
          ownerId = pitGroup.getSourceId();
          ownerName = pitGroup.getName();
          if (pitField.getType().equals(FieldType.LIST.getType())) {
            isMembership = true;
          } else {
            privilegeType = FieldType.ACCESS.getType();
            privilegeName = AccessPrivilege.listToPriv(pitField.getName()).getName();
            ownerType = Membership.OWNER_TYPE_GROUP;
          }
        } else if (pitGroupSet.getOwnerStemId() != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(pitGroupSet.getOwnerId(), true);
          ownerId = pitStem.getSourceId();
          ownerName = pitStem.getName();
          privilegeType = FieldType.NAMING.getType();
          privilegeName = NamingPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_STEM;
        } else if (pitGroupSet.getOwnerAttrDefId() != null) {
          PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(pitGroupSet.getOwnerId(), true);
          ownerId = pitAttributeDef.getSourceId();
          ownerName = pitAttributeDef.getName();
          privilegeType = FieldType.ATTRIBUTE_DEF.getType();
          privilegeName = AttributeDefPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_ATTRIBUTE_DEF;
        } else {
          throw new RuntimeException("Unable to determine owner of PIT Group Set: " + pitGroupSet.getId());
        }
        
        ChangeLogEntry changeLogEntry = null;
        
        if (isMembership && this.getFlatMembershipNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.MEMBERSHIP_DELETE,
              ChangeLogLabels.MEMBERSHIP_DELETE.id.name(), this.getSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldName.name(), pitField.getName(),
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldId.name(), pitField.getSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.memberId.name(), this.getMember().getSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.subjectId.name(), this.getMember().getSubjectId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.sourceId.name(), this.getMember().getSubjectSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.groupId.name(), ownerId,
              ChangeLogLabels.MEMBERSHIP_DELETE.membershipType.name(), "flattened",
              ChangeLogLabels.MEMBERSHIP_DELETE.subjectName.name(), subjectName,
              ChangeLogLabels.MEMBERSHIP_DELETE.groupName.name(), ownerName);
        } else if (!isMembership && this.getFlatPrivilegeNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PRIVILEGE_DELETE,
              ChangeLogLabels.PRIVILEGE_DELETE.id.name(), this.getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), privilegeName,
              ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), pitField.getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), this.getMember().getSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), this.getMember().getSubjectId(),
              ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), this.getMember().getSubjectSourceId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeType.name(), privilegeType,
              ChangeLogLabels.PRIVILEGE_DELETE.ownerType.name(), ownerType,
              ChangeLogLabels.PRIVILEGE_DELETE.ownerId.name(), ownerId,
              ChangeLogLabels.PRIVILEGE_DELETE.membershipType.name(), "flattened",
              ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), ownerName);
        }

        if (changeLogEntry != null) {
          changeLogEntry.setContextId(this.getContextId());
          changeLogEntry.setCreatedOnDb(this.getEndTimeDb());
          changeLogEntryBatch.add(changeLogEntry);
          
          if (changeLogEntryBatch.size() % batchSize == 0) {
            GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, false);
            changeLogEntryBatch.clear();
          }
        }
      }
      
      // make sure all changes get made      
      if (changeLogEntryBatch.size() > 0) {
        GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, false);
        changeLogEntryBatch.clear();
      }
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {

    // add change log entry for permissions
    if (this.isActive() && this.getNotificationsForRolesWithPermissionChangesOnSaveOrUpdate()) {
      Set<ChangeLogEntry> changeLogEntryBatch = new LinkedHashSet<ChangeLogEntry>();
      int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
      if (batchSize <= 0) {
        batchSize = 1;
      }

      Set<PITGroup> roles = GrouperDAOFactory.getFactory().getPITGroup().findRolesWithPermissionsContainingObject(this);

      for (PITGroup role : roles) {
        ChangeLogEntry changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PERMISSION_CHANGE_ON_ROLE,
            ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId.name(), role.getSourceId(),
            ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleName.name(), role.getName());
            
        changeLogEntry.setContextId(this.getContextId());
        changeLogEntry.setCreatedOnDb(this.getStartTimeDb());
        changeLogEntryBatch.add(changeLogEntry);
        if (changeLogEntryBatch.size() % batchSize == 0) {
          GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, false);
          changeLogEntryBatch.clear();
        }
      }
      
      // make sure all changes get made      
      if (changeLogEntryBatch.size() > 0) {
        GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntryBatch, false);
        changeLogEntryBatch.clear();
      }
    }
    
    // if the member is a group, add the PIT immediate group set.
    if (this.isActive() && this.getMember().getSubjectTypeId().equals("group")) {
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(this.getFieldId(), true);
      Field field = FieldFinder.findById(pitField.getSourceId(), false);
      if (field == null) {
        // if the field was deleted, then there's nothing to do
        super.onPostSave(hibernateSession);
        return;
      }
      
      PITGroup memberGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(this.getMember().getSubjectId(), true);

      GroupSet immediateGroupSet = null;
      PITGroupSet pitImmediateGroupSet = new PITGroupSet();

      if (this.getOwnerGroupId() != null) {
        PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findById(this.getOwnerGroupId(), true);
        pitImmediateGroupSet.setOwnerGroupId(this.getOwnerGroupId());
        immediateGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(pitOwner.getSourceId(), memberGroup.getSourceId(), field);
      } else if (this.getOwnerStemId() != null) {
        PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findById(this.getOwnerStemId(), true);
        pitImmediateGroupSet.setOwnerStemId(this.getOwnerStemId());
        immediateGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerStemAndMemberGroupAndField(pitOwner.getSourceId(), memberGroup.getSourceId(), field);
      } else if (this.getOwnerAttrDefId() != null) {
        PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(this.getOwnerAttrDefId(), true);
        pitImmediateGroupSet.setOwnerAttrDefId(this.getOwnerAttrDefId());
        immediateGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerAttrDefAndMemberGroupAndField(pitOwner.getSourceId(), memberGroup.getSourceId(), field);
      } else {
        throw new RuntimeException("Not expecting");
      }
      
      if (immediateGroupSet == null) {
        // if the immediate group set was deleted, then there's nothing to do
        super.onPostSave(hibernateSession);
        return;
      }
      
      if (!GrouperUtil.isEmpty(this.getContextId()) && !GrouperUtil.isEmpty(immediateGroupSet.getContextId()) &&
          !this.getContextId().equals(immediateGroupSet.getContextId())) {
        // this group set must have been added, deleted and then readded.  Don't add to point in time
        super.onPostSave(hibernateSession);
        return;
      }
      
      PITField pitMemberField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(immediateGroupSet.getMemberFieldId(), true);
      PITGroupSet pitParent = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(immediateGroupSet.getParentId(), true);
      
      pitImmediateGroupSet.setId(GrouperUuid.getUuid());
      pitImmediateGroupSet.setSourceId(immediateGroupSet.getId());
      pitImmediateGroupSet.setFieldId(this.getFieldId());
      pitImmediateGroupSet.setMemberFieldId(pitMemberField.getId());
      pitImmediateGroupSet.setMemberGroupId(memberGroup.getId());
      pitImmediateGroupSet.setDepth(1);
      pitImmediateGroupSet.setParentId(pitParent.getId());
      pitImmediateGroupSet.setActiveDb("T");
      pitImmediateGroupSet.setStartTimeDb(this.getStartTimeDb());
      pitImmediateGroupSet.setContextId(this.getContextId());
      pitImmediateGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(this.getFlatMembershipNotificationsOnSaveOrUpdate());
      pitImmediateGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(this.getFlatPrivilegeNotificationsOnSaveOrUpdate());
      pitImmediateGroupSet.saveOrUpdate();
    }
    
    super.onPostSave(hibernateSession);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {

    // if the member is a group and the membership is ending, add an end time to the PIT immediate group set.
    if (!this.isActive() && this.dbVersion().isActive() && this.getMember().getSubjectTypeId().equals("group")) {

      PITGroupSet pitImmediateGroupSet = null;
      PITGroup memberGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(this.getMember().getSubjectId(), false);
      if (memberGroup == null) {
        // probably a bad membership being deleted, but let's make sure no prior version of the group has group sets
        Set<PITGroup> memberGroups = GrouperDAOFactory.getFactory().getPITGroup().findBySourceId(this.getMember().getSubjectId(), false);
        for (PITGroup currMemberGroup : memberGroups) {
          pitImmediateGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findActiveImmediateByPITOwnerAndPITMemberAndPITField(
              this.getOwnerId(), currMemberGroup.getId(), this.getFieldId());
          if (pitImmediateGroupSet != null) {
            break;
          }
        }
      }
      
      if (memberGroup == null && pitImmediateGroupSet == null) {
        // ok nothing to do then
        super.onPostUpdate(hibernateSession);
        return;
      }
      
      // get the PIT immediate group set
      if (pitImmediateGroupSet == null) {
        pitImmediateGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findActiveImmediateByPITOwnerAndPITMemberAndPITField(
            this.getOwnerId(), memberGroup.getId(), this.getFieldId());
      }
      
      if (pitImmediateGroupSet == null) {
        // this must have already been deleted...
        super.onPostUpdate(hibernateSession);
        return;
      }

      pitImmediateGroupSet.setActiveDb("F");
      pitImmediateGroupSet.setEndTimeDb(this.getEndTimeDb());
      pitImmediateGroupSet.setContextId(this.getContextId());
      pitImmediateGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(this.getFlatMembershipNotificationsOnSaveOrUpdate());
      pitImmediateGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(this.getFlatPrivilegeNotificationsOnSaveOrUpdate());
      pitImmediateGroupSet.saveOrUpdate();
    }
    
    super.onPostUpdate(hibernateSession);
  }
  
  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public PITMembership dbVersion() {
    return (PITMembership)this.dbVersion;
  }
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }


  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    if (this.isActive()) {
      throw new RuntimeException("Cannot delete active point in time membership object with id=" + this.getId());
    }
    
    // Note that we're not deleting group sets from here since there's no foreign key to the grouper_group_set table.
    // We're assuming that memberships only get deleted when all the memberships for the group, stem, or attr def
    // are getting deleted.  So the group sets get deleted separately.
    
    // delete attribute assignments
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByOwnerPITMembershipId(this.getId());
    
    for (PITAttributeAssign assignment : assignments) {
      GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(assignment);
    }
  }
  
  private PITGroup pitOwnerGroup = null;
  private PITAttributeDef pitOwnerAttributeDef = null;
  private PITStem pitOwnerStem = null;
  private PITField pitField = null;
  private PITMember pitMember = null;
  
  /**
   * @return pitMember
   */
  public PITMember getPITMember() {
    if (pitMember == null) {
      pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(memberId, true);
    }
    
    return pitMember;
  }

  
  /**
   * @return pitField
   */
  public PITField getPITField() {
    if (pitField == null) {
      pitField = GrouperDAOFactory.getFactory().getPITField().findById(fieldId, true);
    }
    
    return pitField;
  }

  /**
   * @return pitOwnerGroup
   */
  public PITGroup getOwnerPITGroup() {
    if (pitOwnerGroup == null && ownerGroupId != null) {
      pitOwnerGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(ownerGroupId, true);
    }
    
    return pitOwnerGroup;
  }
  
  /**
   * @return pitOwnerAttributeDef
   */
  public PITAttributeDef getOwnerPITAttributeDef() {
    if (pitOwnerAttributeDef == null && ownerAttrDefId != null) {
      pitOwnerAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(ownerAttrDefId, true);
    }
    
    return pitOwnerAttributeDef;
  }
  
  /**
   * @return pitOwnerStem
   */
  public PITStem getOwnerPITStem() {
    if (pitOwnerStem == null && ownerStemId != null) {
      pitOwnerStem = GrouperDAOFactory.getFactory().getPITStem().findById(ownerStemId, true);
    }
    
    return pitOwnerStem;
  }
}
