package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
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
public class PITMembership extends GrouperAPI implements Hib3GrouperVersioned {

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
  
  /** active */
  public static final String COLUMN_ACTIVE = "active";
  
  /** start_time */
  public static final String COLUMN_START_TIME = "start_time";
  
  /** end_time */
  public static final String COLUMN_END_TIME = "end_time";
  
  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  
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
  
  /** constant for field name for: activeDb */
  public static final String FIELD_ACTIVE_DB = "activeDb";
  
  /** constant for field name for: startTimeDb */
  public static final String FIELD_START_TIME_DB = "startTimeDb";
  
  /** constant for field name for: endTimeDb */
  public static final String FIELD_END_TIME_DB = "endTimeDb";
  

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_OWNER_ID, FIELD_OWNER_ATTR_DEF_ID, FIELD_OWNER_GROUP_ID,
      FIELD_OWNER_STEM_ID, FIELD_MEMBER_ID, FIELD_FIELD_ID,
      FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB);



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
  
  /** activeDb */
  private String activeDb;
  
  /** startTimeDb */
  private Long startTimeDb;
  
  /** endTimeDb */
  private Long endTimeDb;
  
  /** member */
  private PITMember member;
  
  /** whether there will flat notifications when this object is saved or updated */ 
  private boolean flatNotificationsOnSaveOrUpdate = false;
  
  /**
   * @return boolean
   */
  public boolean getFlatNotificationsOnSaveOrUpdate() {
    return flatNotificationsOnSaveOrUpdate;
  }
  
  /**
   * @param flatNotificationsOnSaveOrUpdate
   */
  public void setFlatNotificationsOnSaveOrUpdate(boolean flatNotificationsOnSaveOrUpdate) {
    this.flatNotificationsOnSaveOrUpdate = flatNotificationsOnSaveOrUpdate;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
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
    if (existing != null && !existing.isActive()) {
      GrouperDAOFactory.getFactory().getPITMembership().updateId(existing.getId(), GrouperUuid.getUuid());
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
   * @return activeDb
   */
  public String getActiveDb() {
    return activeDb;
  }

  /**
   * @param activeDb
   */
  public void setActiveDb(String activeDb) {
    this.activeDb = activeDb;
  }

  /**
   * @return startTimeDb
   */
  public Long getStartTimeDb() {
    return startTimeDb;
  }

  /**
   * @param startTimeDb
   */
  public void setStartTimeDb(Long startTimeDb) {
    this.startTimeDb = startTimeDb;
  }

  /**
   * @return endTimeDb
   */
  public Long getEndTimeDb() {
    return endTimeDb;
  }

  /**
   * @param endTimeDb
   */
  public void setEndTimeDb(Long endTimeDb) {
    this.endTimeDb = endTimeDb;
  }

  /**
   * @return true if active
   */
  public boolean isActive() {
    if (activeDb == null) {
      throw new RuntimeException("activeDb should not be null.");
    }
    
    if (activeDb.equals("T")) {
      return true;
    }
    
    return false;
  }
  
  /**
   * @return start time
   */
  public Timestamp getStartTime() {
    if (startTimeDb == null) {
      throw new RuntimeException("startTimeDb should not be null.");
    }
    
    return new Timestamp(startTimeDb / 1000);
  }
  
  /**
   * @return end time
   */
  public Timestamp getEndTime() {
    if (endTimeDb != null) {
      return new Timestamp(endTimeDb / 1000);
    }
    
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    // add change log entry for flat memberships
    if (this.isActive() && this.getFlatNotificationsOnSaveOrUpdate()) {
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
        if (isMembership) {
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
        } else {
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
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    // add change log entry for flat memberships
    if (!this.isActive() && this.getFlatNotificationsOnSaveOrUpdate()) {
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
        
        if (isMembership) {
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
        } else {
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

        changeLogEntry.setContextId(this.getContextId());
        changeLogEntry.setCreatedOnDb(this.getEndTimeDb());
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
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    super.onPostSave(hibernateSession);

    // if the member is a group, add the PIT immediate group set.
    if (this.getMember().getSubjectTypeId().equals("group")) {
      Field field = FieldFinder.findById(this.getFieldId(), false);
      if (field == null) {
        // if the field was deleted, then there's nothing to do
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
      pitImmediateGroupSet.setFlatNotificationsOnSaveOrUpdate(this.getFlatNotificationsOnSaveOrUpdate());
      pitImmediateGroupSet.saveOrUpdate();
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {
    super.onPostUpdate(hibernateSession);

    // if the member is a group and the membership is ending, add an end time to the PIT immediate group set.
    if (!this.isActive() && this.getMember().getSubjectTypeId().equals("group")) {

      PITGroup memberGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(this.getMember().getSubjectId());
      
      // get the PIT immediate group set
      PITGroupSet pitImmediateGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findActiveImmediateByOwnerAndMemberAndField(
          this.getOwnerId(), memberGroup.getId(), this.getFieldId());
      
      if (pitImmediateGroupSet == null) {
        // this must have already been deleted...
        return;
      }

      pitImmediateGroupSet.setActiveDb("F");
      pitImmediateGroupSet.setEndTimeDb(this.getEndTimeDb());
      pitImmediateGroupSet.setContextId(this.getContextId());
      pitImmediateGroupSet.setFlatNotificationsOnSaveOrUpdate(this.getFlatNotificationsOnSaveOrUpdate());
      pitImmediateGroupSet.saveOrUpdate();
    }
  }
}
