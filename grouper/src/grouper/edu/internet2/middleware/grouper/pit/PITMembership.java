package edu.internet2.middleware.grouper.pit;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
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
      FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB);

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_CONTEXT_ID, FIELD_END_TIME_DB, FIELD_FIELD_ID, 
      FIELD_ID, FIELD_MEMBER_ID, FIELD_OWNER_ATTR_DEF_ID, FIELD_OWNER_GROUP_ID, 
      FIELD_OWNER_ID, FIELD_OWNER_STEM_ID, FIELD_START_TIME_DB);


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
    // if the id already exists for an inactive membership, let's rename the id to avoid a conflict.
    PITMembership existing = GrouperDAOFactory.getFactory().getPITMembership().findById(this.getId());
    if (existing != null && !existing.isActive() && this.isActive()) {
      
      // first make a copy of the membership with a new id
      PITMembership existingCopy = existing.clone();
      existingCopy.setId(GrouperUuid.getUuid());
      existingCopy.setHibernateVersionNumber(-1L);
      existingCopy.save();
      
      // next update attribute assignments where this is the owner
      GrouperDAOFactory.getFactory().getPITAttributeAssign().updateOwnerMembershipId(existing.getId(), existingCopy.getId());
      
      // delete the old membership now and add the new one
      existing.delete();
      GrouperDAOFactory.getFactory().getPITMembership().saveOrUpdate(this);
      
      // next add new assignments and end dates to existing ones.
      Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findActiveByOwnerMembershipId(existingCopy.getId());
      Iterator<PITAttributeAssign> assignmentIter = assignments.iterator();
      while (assignmentIter.hasNext()) {
        PITAttributeAssign assignment = assignmentIter.next();
        PITAttributeAssign assignmentCopy = assignment.clone();
        
        assignmentCopy.setId(GrouperUuid.getUuid());
        assignmentCopy.setEndTimeDb(this.getStartTimeDb());
        assignmentCopy.setActiveDb("F");
        assignmentCopy.setContextId(this.getContextId());
        assignmentCopy.setHibernateVersionNumber(-1L);
        assignmentCopy.save();
        
        assignment.setOwnerMembershipId(this.getId());
        assignment.setStartTimeDb(this.getStartTimeDb());
        assignment.setContextId(this.getContextId());
        assignment.update();
        
        // well this assignment might have values too...
        GrouperDAOFactory.getFactory().getPITAttributeAssignValue().updateAttributeAssignId(assignment.getId(), assignmentCopy.getId());
        Set<PITAttributeAssignValue> values = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findActiveByAttributeAssignId(assignmentCopy.getId());
        Iterator<PITAttributeAssignValue> valueIter = values.iterator();
        while (valueIter.hasNext()) {
          PITAttributeAssignValue value = valueIter.next();
          PITAttributeAssignValue valueCopy = value.clone();
          
          valueCopy.setId(GrouperUuid.getUuid());
          valueCopy.setEndTimeDb(this.getStartTimeDb());
          valueCopy.setActiveDb("F");
          valueCopy.setContextId(this.getContextId());
          valueCopy.setHibernateVersionNumber(-1L);
          valueCopy.save();
          
          value.setAttributeAssignId(assignment.getId());
          value.setStartTimeDb(this.getStartTimeDb());
          value.setContextId(this.getContextId());
          value.update();
        }
        
        // to complicate things a bit more, this attribute assignment might be the owner of another attribute assignment
        GrouperDAOFactory.getFactory().getPITAttributeAssign().updateOwnerAttributeAssignId(assignment.getId(), assignmentCopy.getId());
        Set<PITAttributeAssign> assignments2 = GrouperDAOFactory.getFactory().getPITAttributeAssign().findActiveByOwnerAttributeAssignId(assignmentCopy.getId());
        Iterator<PITAttributeAssign> assignmentIter2 = assignments2.iterator();
        while (assignmentIter2.hasNext()) {
          PITAttributeAssign assignment2 = assignmentIter2.next();
          PITAttributeAssign assignmentCopy2 = assignment2.clone();
          
          assignmentCopy2.setId(GrouperUuid.getUuid());
          assignmentCopy2.setEndTimeDb(this.getStartTimeDb());
          assignmentCopy2.setActiveDb("F");
          assignmentCopy2.setContextId(this.getContextId());
          assignmentCopy2.setHibernateVersionNumber(-1L);
          assignmentCopy2.save();
          
          assignment2.setOwnerAttributeAssignId(assignment.getId());
          assignment2.setStartTimeDb(this.getStartTimeDb());
          assignment2.setContextId(this.getContextId());
          assignment2.update();
          
          // well this assignment might have values too...
          GrouperDAOFactory.getFactory().getPITAttributeAssignValue().updateAttributeAssignId(assignment2.getId(), assignmentCopy2.getId());
          Set<PITAttributeAssignValue> values2 = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findActiveByAttributeAssignId(assignmentCopy2.getId());
          Iterator<PITAttributeAssignValue> valueIter2 = values2.iterator();
          while (valueIter2.hasNext()) {
            PITAttributeAssignValue value2 = valueIter2.next();
            PITAttributeAssignValue valueCopy2 = value2.clone();
            
            valueCopy2.setId(GrouperUuid.getUuid());
            valueCopy2.setEndTimeDb(this.getStartTimeDb());
            valueCopy2.setActiveDb("F");
            valueCopy2.setContextId(this.getContextId());
            valueCopy2.setHibernateVersionNumber(-1L);
            valueCopy2.save();
            
            value2.setAttributeAssignId(assignment2.getId());
            value2.setStartTimeDb(this.getStartTimeDb());
            value2.setContextId(this.getContextId());
            value2.update();
          }
        }
      }
      
      return;
    }
    
    GrouperDAOFactory.getFactory().getPITMembership().saveOrUpdate(this);
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
    
    this.member = GrouperDAOFactory.getFactory().getPITMember().findById(this.memberId);
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
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(pitGroupSet.getFieldId());
        String ownerId = null;
        String ownerName = null;
        String privilegeName = null;
        String privilegeType = null;
        String ownerType = null;
        boolean isMembership = false;
        
        if (pitGroupSet.getOwnerGroupId() != null) {
          PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(pitGroupSet.getOwnerId());
          ownerId = pitGroup.getId();
          ownerName = pitGroup.getName();
          if (pitField.getType().equals(FieldType.LIST.getType())) {
            isMembership = true;
          } else {
            privilegeType = FieldType.ACCESS.getType();
            privilegeName = AccessPrivilege.listToPriv(pitField.getName()).getName();
            ownerType = Membership.OWNER_TYPE_GROUP;
          }
        } else if (pitGroupSet.getOwnerStemId() != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(pitGroupSet.getOwnerId());
          ownerId = pitStem.getId();
          ownerName = pitStem.getName();
          privilegeType = FieldType.NAMING.getType();
          privilegeName = NamingPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_STEM;
        } else if (pitGroupSet.getOwnerAttrDefId() != null) {
          PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(pitGroupSet.getOwnerId());
          ownerId = pitAttributeDef.getId();
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
              ChangeLogLabels.MEMBERSHIP_ADD.id.name(), this.getId(),
              ChangeLogLabels.MEMBERSHIP_ADD.fieldName.name(), pitField.getName(),
              ChangeLogLabels.MEMBERSHIP_ADD.fieldId.name(), pitField.getId(),
              ChangeLogLabels.MEMBERSHIP_ADD.memberId.name(), this.getMemberId(),
              ChangeLogLabels.MEMBERSHIP_ADD.subjectId.name(), this.getMember().getSubjectId(),
              ChangeLogLabels.MEMBERSHIP_ADD.sourceId.name(), this.getMember().getSubjectSourceId(),
              ChangeLogLabels.MEMBERSHIP_ADD.groupId.name(), ownerId,
              ChangeLogLabels.MEMBERSHIP_ADD.membershipType.name(), "flattened",
              ChangeLogLabels.MEMBERSHIP_ADD.groupName.name(), ownerName);
        } else if (!isMembership && this.getFlatPrivilegeNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PRIVILEGE_ADD,
              ChangeLogLabels.PRIVILEGE_ADD.id.name(), this.getId(),
              ChangeLogLabels.PRIVILEGE_ADD.privilegeName.name(), privilegeName,
              ChangeLogLabels.PRIVILEGE_ADD.fieldId.name(), pitField.getId(),
              ChangeLogLabels.PRIVILEGE_ADD.memberId.name(), this.getMemberId(),
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
            ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId.name(), role.getId(),
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

      Set<PITGroupSet> pitGroupSets = GrouperDAOFactory.getFactory().getPITMembershipView().findPITGroupSetsJoinedWithOldPITMembership(this);
      Iterator<PITGroupSet> iter = pitGroupSets.iterator();
      while (iter.hasNext()) {
        PITGroupSet pitGroupSet = iter.next();
        PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(pitGroupSet.getFieldId());
        String ownerId = null;
        String ownerName = null;
        String privilegeName = null;
        String privilegeType = null;
        String ownerType = null;
        boolean isMembership = false;
        
        if (pitGroupSet.getOwnerGroupId() != null) {
          PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(pitGroupSet.getOwnerId());
          ownerId = pitGroup.getId();
          ownerName = pitGroup.getName();
          if (pitField.getType().equals(FieldType.LIST.getType())) {
            isMembership = true;
          } else {
            privilegeType = FieldType.ACCESS.getType();
            privilegeName = AccessPrivilege.listToPriv(pitField.getName()).getName();
            ownerType = Membership.OWNER_TYPE_GROUP;
          }
        } else if (pitGroupSet.getOwnerStemId() != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(pitGroupSet.getOwnerId());
          ownerId = pitStem.getId();
          ownerName = pitStem.getName();
          privilegeType = FieldType.NAMING.getType();
          privilegeName = NamingPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_STEM;
        } else if (pitGroupSet.getOwnerAttrDefId() != null) {
          PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(pitGroupSet.getOwnerId());
          ownerId = pitAttributeDef.getId();
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
              ChangeLogLabels.MEMBERSHIP_DELETE.id.name(), this.getId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldName.name(), pitField.getName(),
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldId.name(), pitField.getId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.memberId.name(), this.getMemberId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.subjectId.name(), this.getMember().getSubjectId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.sourceId.name(), this.getMember().getSubjectSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.groupId.name(), ownerId,
              ChangeLogLabels.MEMBERSHIP_DELETE.membershipType.name(), "flattened",
              ChangeLogLabels.MEMBERSHIP_DELETE.groupName.name(), ownerName);
        } else if (!isMembership && this.getFlatPrivilegeNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PRIVILEGE_DELETE,
              ChangeLogLabels.PRIVILEGE_DELETE.id.name(), this.getId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), privilegeName,
              ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), pitField.getId(),
              ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), this.getMemberId(),
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
            ChangeLogLabels.PERMISSION_CHANGE_ON_ROLE.roleId.name(), role.getId(),
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
      Field field = FieldFinder.findById(this.getFieldId(), false);
      if (field == null) {
        // if the field was deleted, then there's nothing to do
        super.onPostSave(hibernateSession);
        return;
      }
      
      PITGroup memberGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(this.getMember().getSubjectId());

      GroupSet immediateGroupSet = null;
      PITGroupSet pitImmediateGroupSet = new PITGroupSet();

      if (this.getOwnerGroupId() != null) {
        pitImmediateGroupSet.setOwnerGroupId(this.getOwnerGroupId());
        immediateGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(this.getOwnerGroupId(), memberGroup.getId(), field);
      } else if (this.getOwnerStemId() != null) {
        pitImmediateGroupSet.setOwnerStemId(this.getOwnerStemId());
        immediateGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerStemAndMemberGroupAndField(this.getOwnerStemId(), memberGroup.getId(), field);
      } else if (this.getOwnerAttrDefId() != null) {
        pitImmediateGroupSet.setOwnerAttrDefId(this.getOwnerAttrDefId());
        immediateGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerAttrDefAndMemberGroupAndField(this.getOwnerAttrDefId(), memberGroup.getId(), field);
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
      
      pitImmediateGroupSet.setId(immediateGroupSet.getId());
      pitImmediateGroupSet.setFieldId(this.getFieldId());
      pitImmediateGroupSet.setMemberFieldId(Group.getDefaultList().getUuid());
      pitImmediateGroupSet.setMemberGroupId(memberGroup.getId());
      pitImmediateGroupSet.setDepth(1);
      pitImmediateGroupSet.setParentId(immediateGroupSet.getParentId());
      pitImmediateGroupSet.setActiveDb("T");
      pitImmediateGroupSet.setStartTimeDb(this.getStartTimeDb());
      pitImmediateGroupSet.setContextId(this.getContextId());
      pitImmediateGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(this.getFlatMembershipNotificationsOnSaveOrUpdate());
      pitImmediateGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(this.getFlatPrivilegeNotificationsOnSaveOrUpdate());
      
      // see if group set with this id already exists.  if it does, delete it..
      PITGroupSet existing = GrouperDAOFactory.getFactory().getPITGroupSet().findById(pitImmediateGroupSet.getId());
      if (existing != null && !existing.isActive()) {
        existing.delete();
      }
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

      PITGroup memberGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(this.getMember().getSubjectId());
      
      // get the PIT immediate group set
      PITGroupSet pitImmediateGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findActiveImmediateByOwnerAndMemberAndField(
          this.getOwnerId(), memberGroup.getId(), this.getFieldId());
      
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
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByOwnerMembershipId(this.getId());
    
    for (PITAttributeAssign assignment : assignments) {
      GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(assignment);
    }
  }
}
