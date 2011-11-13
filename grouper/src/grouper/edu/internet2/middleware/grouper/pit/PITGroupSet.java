package edu.internet2.middleware.grouper.pit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
public class PITGroupSet extends GrouperPIT implements Hib3GrouperVersioned {

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

  /** member_attr_def_id */
  public static final String COLUMN_MEMBER_ATTR_DEF_ID = "member_attr_def_id";
  
  /** member_group_id */
  public static final String COLUMN_MEMBER_GROUP_ID = "member_group_id";
  
  /** member_stem_id */
  public static final String COLUMN_MEMBER_STEM_ID = "member_stem_id";
  
  /** field_id */
  public static final String COLUMN_FIELD_ID = "field_id";

  /** member_field_id */
  public static final String COLUMN_MEMBER_FIELD_ID = "member_field_id";
  
  /** depth */
  public static final String COLUMN_DEPTH = "depth";

  /** parent_id */
  public static final String COLUMN_PARENT_ID = "parent_id";
  
  
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

  /** constant for field name for: memberAttrDefId */
  public static final String FIELD_MEMBER_ATTR_DEF_ID = "memberAttrDefId";
  
  /** constant for field name for: memberGroupId */
  public static final String FIELD_MEMBER_GROUP_ID = "memberGroupId";
  
  /** constant for field name for: memberStemId */
  public static final String FIELD_MEMBER_STEM_ID = "memberStemId";
  
  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

  /** constant for field name for: memberFieldId */
  public static final String FIELD_MEMBER_FIELD_ID = "memberFieldId";
  
  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";
  
  /** constant for field name for: parentId */
  public static final String FIELD_PARENT_ID = "parentId";
  

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_OWNER_ID, FIELD_OWNER_ATTR_DEF_ID, FIELD_OWNER_GROUP_ID,
      FIELD_OWNER_STEM_ID, FIELD_MEMBER_ID, FIELD_FIELD_ID,
      FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB,
      FIELD_MEMBER_ATTR_DEF_ID, FIELD_MEMBER_GROUP_ID, FIELD_MEMBER_STEM_ID,
      FIELD_MEMBER_FIELD_ID, FIELD_DEPTH, FIELD_PARENT_ID);


  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_CONTEXT_ID, FIELD_DEPTH, FIELD_END_TIME_DB, 
      FIELD_FIELD_ID, 
      FIELD_ID, FIELD_MEMBER_ATTR_DEF_ID, FIELD_MEMBER_FIELD_ID, FIELD_MEMBER_GROUP_ID, 
      FIELD_MEMBER_ID, FIELD_MEMBER_STEM_ID, FIELD_OWNER_ATTR_DEF_ID, FIELD_OWNER_GROUP_ID, 
      FIELD_OWNER_ID, FIELD_OWNER_STEM_ID, FIELD_PARENT_ID, FIELD_START_TIME_DB);


  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_GROUP_SET = "grouper_pit_group_set";

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
  
  /** memberAttrDefId */
  private String memberAttrDefId;
  
  /** memberGroupId */
  private String memberGroupId;
  
  /** memberStemId */
  private String memberStemId;
  
  /** fieldId */
  private String fieldId;
  
  /** memberFieldId */
  private String memberFieldId;
  
  /** depth */
  private int depth;
  
  /** parentId */
  private String parentId;
  
  /** whether there will be flat membership notifications when this object is saved or updated */ 
  private boolean flatMembershipNotificationsOnSaveOrUpdate = false;

  /** whether there will be flat privilege notifications when this object is saved or updated */ 
  private boolean flatPrivilegeNotificationsOnSaveOrUpdate = false;
  
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
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITGroupSet().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITGroupSet().delete(this);
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
   * @return memberAttrDefId
   */
  public String getMemberAttrDefId() {
    return memberAttrDefId;
  }

  /**
   * @param memberAttrDefId
   */
  public void setMemberAttrDefId(String memberAttrDefId) {
    this.memberAttrDefId = memberAttrDefId;
    if (memberAttrDefId != null) {
      setMemberId(memberAttrDefId);
    }
  }

  /**
   * @return memberGroupId
   */
  public String getMemberGroupId() {
    return memberGroupId;
  }

  /**
   * @param memberGroupId
   */
  public void setMemberGroupId(String memberGroupId) {
    this.memberGroupId = memberGroupId;
    if (memberGroupId != null) {
      setMemberId(memberGroupId);
    }
  }

  /**
   * @return memberStemId
   */
  public String getMemberStemId() {
    return memberStemId;
  }

  /**
   * @param memberStemId
   */
  public void setMemberStemId(String memberStemId) {
    this.memberStemId = memberStemId;
    if (memberStemId != null) {
      setMemberId(memberStemId);
    }
  }

  /**
   * @return memberFieldId
   */
  public String getMemberFieldId() {
    return memberFieldId;
  }

  /**
   * @param memberFieldId
   */
  public void setMemberFieldId(String memberFieldId) {
    this.memberFieldId = memberFieldId;
  }

  /**
   * @return parentId
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * @param parentId
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }
  
  /**
   * @return depth
   */
  public int getDepth() {
    return depth;
  }
  
  /**
   * @param depth
   */
  public void setDepth(int depth) {
    this.depth = depth;
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

      PITField pitField = null;
      String ownerId = null;
      String ownerName = null;
      String privilegeName = null;
      String privilegeType = null;
      String ownerType = null;
      boolean isMembership = false;
      Set<PITMembership> pitMemberships = GrouperDAOFactory.getFactory().getPITMembershipView().findPITMembershipsJoinedWithNewPITGroupSet(this);
      
      if (pitMemberships.size() > 0) {
        pitField = GrouperDAOFactory.getFactory().getPITField().findById(this.getFieldId());
        if (this.getOwnerGroupId() != null) {
          PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(this.getOwnerId());
          ownerId = pitGroup.getId();
          ownerName = pitGroup.getName();
          if (pitField.getType().equals(FieldType.LIST.getType())) {
            isMembership = true;
          } else {
            privilegeType = FieldType.ACCESS.getType();
            privilegeName = AccessPrivilege.listToPriv(pitField.getName()).getName();
            ownerType = Membership.OWNER_TYPE_GROUP;
          }
        } else if (this.getOwnerStemId() != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(this.getOwnerId());
          ownerId = pitStem.getId();
          ownerName = pitStem.getName();
          privilegeType = FieldType.NAMING.getType();
          privilegeName = NamingPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_STEM;
        } else if (this.getOwnerAttrDefId() != null) {
          PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(this.getOwnerId());
          ownerId = pitAttributeDef.getId();
          ownerName = pitAttributeDef.getName();
          privilegeType = FieldType.ATTRIBUTE_DEF.getType();
          privilegeName = AttributeDefPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_ATTRIBUTE_DEF;
        } else {
          throw new RuntimeException("Unable to determine owner of PIT Group Set: " + this.getId());
        }
      }
      
      Iterator<PITMembership> iter = pitMemberships.iterator();
      while (iter.hasNext()) {
        PITMembership pitMembership = iter.next();
        
        ChangeLogEntry changeLogEntry = null;
        
        if (isMembership && this.getFlatMembershipNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.MEMBERSHIP_ADD,
              ChangeLogLabels.MEMBERSHIP_ADD.id.name(), pitMembership.getId(),
              ChangeLogLabels.MEMBERSHIP_ADD.fieldName.name(), pitField.getName(),
              ChangeLogLabels.MEMBERSHIP_ADD.fieldId.name(), pitField.getId(),
              ChangeLogLabels.MEMBERSHIP_ADD.memberId.name(), pitMembership.getMemberId(),
              ChangeLogLabels.MEMBERSHIP_ADD.subjectId.name(), pitMembership.getMember().getSubjectId(),
              ChangeLogLabels.MEMBERSHIP_ADD.sourceId.name(), pitMembership.getMember().getSubjectSourceId(),
              ChangeLogLabels.MEMBERSHIP_ADD.groupId.name(), ownerId,
              ChangeLogLabels.MEMBERSHIP_ADD.membershipType.name(), "flattened",
              ChangeLogLabels.MEMBERSHIP_ADD.groupName.name(), ownerName);
        } else if (!isMembership && this.getFlatPrivilegeNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PRIVILEGE_ADD,
              ChangeLogLabels.PRIVILEGE_ADD.id.name(), pitMembership.getId(),
              ChangeLogLabels.PRIVILEGE_ADD.privilegeName.name(), privilegeName,
              ChangeLogLabels.PRIVILEGE_ADD.fieldId.name(), pitField.getId(),
              ChangeLogLabels.PRIVILEGE_ADD.memberId.name(), pitMembership.getMemberId(),
              ChangeLogLabels.PRIVILEGE_ADD.subjectId.name(), pitMembership.getMember().getSubjectId(),
              ChangeLogLabels.PRIVILEGE_ADD.sourceId.name(), pitMembership.getMember().getSubjectSourceId(),
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
    
    // add change log entry for flat memberships
    if (!this.isActive() && this.dbVersion().isActive() && (this.getFlatMembershipNotificationsOnSaveOrUpdate() || this.getFlatPrivilegeNotificationsOnSaveOrUpdate())) {
      Set<ChangeLogEntry> changeLogEntryBatch = new LinkedHashSet<ChangeLogEntry>();
      int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
      if (batchSize <= 0) {
        batchSize = 1;
      }

      PITField pitField = null;
      String ownerId = null;
      String ownerName = null;
      String privilegeName = null;
      String privilegeType = null;
      String ownerType = null;
      boolean isMembership = false;
      Set<PITMembership> pitMemberships = GrouperDAOFactory.getFactory().getPITMembershipView().findPITMembershipsJoinedWithOldPITGroupSet(this);
      
      if (pitMemberships.size() > 0) {
        pitField = GrouperDAOFactory.getFactory().getPITField().findById(this.getFieldId());
        if (this.getOwnerGroupId() != null) {
          PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(this.getOwnerId());
          ownerId = pitGroup.getId();
          ownerName = pitGroup.getName();
          if (pitField.getType().equals(FieldType.LIST.getType())) {
            isMembership = true;
          } else {
            privilegeType = FieldType.ACCESS.getType();
            privilegeName = AccessPrivilege.listToPriv(pitField.getName()).getName();
            ownerType = Membership.OWNER_TYPE_GROUP;
          }
        } else if (this.getOwnerStemId() != null) {
          PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(this.getOwnerId());
          ownerId = pitStem.getId();
          ownerName = pitStem.getName();
          privilegeType = FieldType.NAMING.getType();
          privilegeName = NamingPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_STEM;
        } else if (this.getOwnerAttrDefId() != null) {
          PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(this.getOwnerId());
          ownerId = pitAttributeDef.getId();
          ownerName = pitAttributeDef.getName();
          privilegeType = FieldType.ATTRIBUTE_DEF.getType();
          privilegeName = AttributeDefPrivilege.listToPriv(pitField.getName()).getName();
          ownerType = Membership.OWNER_TYPE_ATTRIBUTE_DEF;
        } else {
          throw new RuntimeException("Unable to determine owner of PIT Group Set: " + this.getId());
        }
      }
      
      Iterator<PITMembership> iter = pitMemberships.iterator();
      while (iter.hasNext()) {
        PITMembership pitMembership = iter.next();
        
        ChangeLogEntry changeLogEntry = null;
        
        if (isMembership && this.getFlatMembershipNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.MEMBERSHIP_DELETE,
              ChangeLogLabels.MEMBERSHIP_DELETE.id.name(), pitMembership.getId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldName.name(), pitField.getName(),
              ChangeLogLabels.MEMBERSHIP_DELETE.fieldId.name(), pitField.getId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.memberId.name(), pitMembership.getMemberId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.subjectId.name(), pitMembership.getMember().getSubjectId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.sourceId.name(), pitMembership.getMember().getSubjectSourceId(),
              ChangeLogLabels.MEMBERSHIP_DELETE.groupId.name(), ownerId,
              ChangeLogLabels.MEMBERSHIP_DELETE.membershipType.name(), "flattened",
              ChangeLogLabels.MEMBERSHIP_DELETE.groupName.name(), ownerName);
        } else if (!isMembership && this.getFlatPrivilegeNotificationsOnSaveOrUpdate()) {
          changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PRIVILEGE_DELETE,
              ChangeLogLabels.PRIVILEGE_DELETE.id.name(), pitMembership.getId(),
              ChangeLogLabels.PRIVILEGE_DELETE.privilegeName.name(), privilegeName,
              ChangeLogLabels.PRIVILEGE_DELETE.fieldId.name(), pitField.getId(),
              ChangeLogLabels.PRIVILEGE_DELETE.memberId.name(), pitMembership.getMemberId(),
              ChangeLogLabels.PRIVILEGE_DELETE.subjectId.name(), pitMembership.getMember().getSubjectId(),
              ChangeLogLabels.PRIVILEGE_DELETE.sourceId.name(), pitMembership.getMember().getSubjectSourceId(),
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
    
    // take care of effective PIT group sets
    if (this.getDepth() == 1 && this.getMemberGroupId() != null) {
      Set<PITGroupSet> results = new LinkedHashSet<PITGroupSet>();
      Set<PITGroupSet> pitGroupSetHasMembers = GrouperDAOFactory.getFactory().getPITGroupSet().findAllActiveByGroupOwnerAndField(
          this.getMemberGroupId(), Group.getDefaultList());
      
      // Add members of member to owner PIT group set
      results.addAll(addHasMembersToOwner(this, pitGroupSetHasMembers));
  
      // If we are working on a group, where is it a member and field is the default list
      if (this.getOwnerGroupId() != null && this.getFieldId().equals(Group.getDefaultList().getUuid())) {
        Set<PITGroupSet> pitGroupSetIsMember = GrouperDAOFactory.getFactory().getPITGroupSet().findAllActiveByMemberGroup(this.getOwnerGroupId());
  
        // Add member and members of member to where owner is member
        results.addAll(addHasMembersToWhereGroupIsMember(this.getMemberGroupId(), pitGroupSetIsMember, pitGroupSetHasMembers));
      }
      
      Iterator<PITGroupSet> iter = results.iterator();
      while (iter.hasNext()) {
        PITGroupSet pitGroupSet = iter.next();
        pitGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(this.getFlatMembershipNotificationsOnSaveOrUpdate());
        pitGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(this.getFlatPrivilegeNotificationsOnSaveOrUpdate());

        // see if group set with this id already exists.  if it does, delete it..
        PITGroupSet existing = GrouperDAOFactory.getFactory().getPITGroupSet().findById(pitGroupSet.getId());
        if (existing != null && !existing.isActive()) {
          existing.delete();
        }

        pitGroupSet.saveOrUpdate();
      }
    }
    
    super.onPostSave(hibernateSession);
  }
  
  /**
   * Given a set of PIT group sets, return a map that will allow retrieval of 
   * the children of a parent PIT group set.
   * @param members
   * @return the map
   */
  private Map<String, Set<PITGroupSet>> getParentToChildrenMap(Set<PITGroupSet> members) {
    Map<String, Set<PITGroupSet>> parentToChildrenMap = new HashMap<String, Set<PITGroupSet>>();

    Iterator<PITGroupSet> iterator = members.iterator();
    while (iterator.hasNext()) {
      PITGroupSet pitGroupSet = iterator.next();
      String parentId = pitGroupSet.getParentId();

      if (parentId != null && !parentId.equals("")) {
        Set<PITGroupSet> children = parentToChildrenMap.get(parentId);
        if (children == null) {
          children = new LinkedHashSet<PITGroupSet>();
        }

        children.add(pitGroupSet);
        parentToChildrenMap.put(parentId, children);
      }
    }

    return parentToChildrenMap;
  }
  
  /**
   * @param pitImmediateGroupSet
   * @param hasMembers
   * @return set
   */
  private Set<PITGroupSet> addHasMembersToOwner(PITGroupSet pitImmediateGroupSet, Set<PITGroupSet> hasMembers) {
    Set<PITGroupSet> pitGroupSets = new LinkedHashSet<PITGroupSet>();
  
    Map<String, Set<PITGroupSet>> parentToChildrenMap = getParentToChildrenMap(hasMembers);

    Iterator<PITGroupSet> it = hasMembers.iterator();
    while (it.hasNext()) {
      PITGroupSet pitGroupSet = it.next();
      if (pitGroupSet.getDepth() == 1) {
        Set<PITGroupSet> newAdditions = addHasMembersRecursively(pitGroupSet, pitImmediateGroupSet, parentToChildrenMap);
        pitGroupSets.addAll(newAdditions);
      }
    }
  
    return pitGroupSets;
  }
  
  /**
   * @param pitGroupSet
   * @param parentPITGroupSet
   * @param parentToChildrenMap
   * @return set
   */
  private Set<PITGroupSet> addHasMembersRecursively(PITGroupSet pitGroupSet, PITGroupSet parentPITGroupSet, Map<String, Set<PITGroupSet>> parentToChildrenMap) {

    PITGroupSet newPITGroupSet = new PITGroupSet();
    newPITGroupSet.setFieldId(parentPITGroupSet.getFieldId());
    newPITGroupSet.setMemberFieldId(Group.getDefaultList().getUuid());
    newPITGroupSet.setOwnerAttrDefId(parentPITGroupSet.getOwnerAttrDefId());
    newPITGroupSet.setOwnerGroupId(parentPITGroupSet.getOwnerGroupId());
    newPITGroupSet.setOwnerStemId(parentPITGroupSet.getOwnerStemId());
    newPITGroupSet.setMemberGroupId(pitGroupSet.getMemberGroupId());
    newPITGroupSet.setDepth(parentPITGroupSet.getDepth() + 1);
    newPITGroupSet.setParentId(parentPITGroupSet.getId());
    newPITGroupSet.setActiveDb("T");
    newPITGroupSet.setStartTimeDb(this.getStartTimeDb());
    newPITGroupSet.setContextId(this.getContextId());

    // now get the existing group set so we have the id.
    GroupSet gs = GrouperDAOFactory.getFactory().getGroupSet().findByOwnerMemberFieldParentAndType(
        newPITGroupSet.getOwnerId(), newPITGroupSet.getMemberId(), newPITGroupSet.getFieldId(), 
        newPITGroupSet.getParentId(), "effective", false);
    
    if (gs == null) {
      // either the group was deleted or it was never added (because it formed a circular path for instance)
      return new LinkedHashSet<PITGroupSet>();
    }
    
    if (!GrouperUtil.isEmpty(this.getContextId()) && !GrouperUtil.isEmpty(gs.getContextId()) &&
        !this.getContextId().equals(gs.getContextId())) {
      // this group set must have been added, deleted and then readded.  Don't add to point in time
      return new LinkedHashSet<PITGroupSet>();
    }
    
    newPITGroupSet.setId(gs.getId());

    Set<PITGroupSet> newPITGroupSets = new LinkedHashSet<PITGroupSet>();
    newPITGroupSets.add(newPITGroupSet);

    Set<PITGroupSet> children = parentToChildrenMap.get(pitGroupSet.getId());
    if (children != null) {
      Iterator<PITGroupSet> it = children.iterator();
      while (it.hasNext()) {
        PITGroupSet nextPITGroupSet = it.next();
        Set<PITGroupSet> newAdditions = addHasMembersRecursively(nextPITGroupSet, newPITGroupSet, parentToChildrenMap);
        newPITGroupSets.addAll(newAdditions);
      }
    }

    return newPITGroupSets;
  }
  
  /**
   * @param memberGroupId
   * @param pitGroupSetIsMember
   * @param pitGroupSetHasMembers
   * @return set
   */
  private Set<PITGroupSet> addHasMembersToWhereGroupIsMember(String memberGroupId,
      Set<PITGroupSet> pitGroupSetIsMember, Set<PITGroupSet> pitGroupSetHasMembers) {
    
    Set<PITGroupSet> pitGroupSets = new LinkedHashSet<PITGroupSet>();

    Iterator<PITGroupSet> isMembersIter = pitGroupSetIsMember.iterator();
    Map<String, Set<PITGroupSet>> parentToChildrenMap = getParentToChildrenMap(pitGroupSetHasMembers);

    // lets get all the hasMembers with a depth of 1 before the while loop
    Set<PITGroupSet> hasMembersOneDepth = new LinkedHashSet<PITGroupSet>();
    Iterator<PITGroupSet> hasMembersIter = pitGroupSetHasMembers.iterator();
    while (hasMembersIter.hasNext()) {
      PITGroupSet pitGroupSet = hasMembersIter.next();
      if (pitGroupSet.getDepth() == 1) {
        hasMembersOneDepth.add(pitGroupSet);
      }
    }

    while (isMembersIter.hasNext()) {
      PITGroupSet isPITGS = isMembersIter.next();

      String ownerGroupId = isPITGS.getOwnerGroupId();
      String ownerStemId = isPITGS.getOwnerStemId();
      String ownerAttrDefId = isPITGS.getOwnerAttrDefId();
      String fieldId = isPITGS.getFieldId();
      String id = isPITGS.getId();
      int depth = isPITGS.getDepth();

      PITGroupSet pitGroupSet = new PITGroupSet();
      pitGroupSet.setDepth(depth + 1);
      pitGroupSet.setParentId(id);
      pitGroupSet.setFieldId(fieldId);
      pitGroupSet.setMemberFieldId(Group.getDefaultList().getUuid());
      pitGroupSet.setMemberGroupId(memberGroupId);
      pitGroupSet.setOwnerGroupId(ownerGroupId);
      pitGroupSet.setOwnerAttrDefId(ownerAttrDefId);
      pitGroupSet.setOwnerStemId(ownerStemId);
      pitGroupSet.setActiveDb("T");
      pitGroupSet.setStartTimeDb(this.getStartTimeDb());
      pitGroupSet.setContextId(this.getContextId());

      // now get the existing group set so we have the id.
      GroupSet gs = GrouperDAOFactory.getFactory().getGroupSet().findByOwnerMemberFieldParentAndType(
          pitGroupSet.getOwnerId(), pitGroupSet.getMemberId(), pitGroupSet.getFieldId(), 
          pitGroupSet.getParentId(), "effective", false);
      
      if (gs == null) {
        // either the group was deleted or it was never added (because it formed a circular path for instance)
        continue;
      }
      
      if (!GrouperUtil.isEmpty(this.getContextId()) && !GrouperUtil.isEmpty(gs.getContextId()) &&
          !this.getContextId().equals(gs.getContextId())) {
        // this group set must have been added, deleted and then readded.  Don't add to point in time
        continue;
      }

      pitGroupSet.setId(gs.getId());
      pitGroupSets.add(pitGroupSet);

      Iterator<PITGroupSet> itHM = hasMembersOneDepth.iterator();
      while (itHM.hasNext()) {
        PITGroupSet hasPITGS = itHM.next();
        Set<PITGroupSet> newAdditions = addHasMembersRecursively(hasPITGS, pitGroupSet, parentToChildrenMap);
        pitGroupSets.addAll(newAdditions);
      }
    }

    return pitGroupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {

    // take care of effective group sets
    if (!this.isActive() && this.dbVersion().isActive() && this.getDepth() == 1) {
      Set<PITGroupSet> pitGroupSetsToEnd = new LinkedHashSet<PITGroupSet>();
      
      // Get all children of this PIT group set
      Set<PITGroupSet> childResults = GrouperDAOFactory.getFactory().getPITGroupSet().findAllActiveChildren(this);
  
      pitGroupSetsToEnd.addAll(childResults);
  
      // Find all effective PIT group sets that need to be ended
      if (this.getOwnerGroupId() != null && this.getFieldId().equals(Group.getDefaultList().getUuid())) {
        Set<PITGroupSet> pitGroupSetIsMember = GrouperDAOFactory.getFactory().getPITGroupSet().findAllActiveByMemberGroup(this.getOwnerGroupId());
        
        Iterator<PITGroupSet> pitGroupSetIsMemberIter = pitGroupSetIsMember.iterator();
        while (pitGroupSetIsMemberIter.hasNext()) {
          PITGroupSet currPITGroupSet = pitGroupSetIsMemberIter.next();
          PITGroupSet childToUpdate = GrouperDAOFactory.getFactory().getPITGroupSet().findActiveImmediateChildByParentAndMemberGroup(currPITGroupSet, this.getMemberGroupId());
  
          if (childToUpdate != null) {
            Set<PITGroupSet> childrenOfChildResults = GrouperDAOFactory.getFactory().getPITGroupSet().findAllActiveChildren(childToUpdate);
    
            pitGroupSetsToEnd.addAll(childrenOfChildResults);
            pitGroupSetsToEnd.add(childToUpdate);
          }
        }
      }
      
      Iterator<PITGroupSet> iter = pitGroupSetsToEnd.iterator();
      while (iter.hasNext()) {
        PITGroupSet pitGroupSet = iter.next();
        pitGroupSet.setActiveDb("F");
        pitGroupSet.setEndTimeDb(this.getEndTimeDb());
        pitGroupSet.setContextId(this.getContextId());
        pitGroupSet.setFlatMembershipNotificationsOnSaveOrUpdate(this.getFlatMembershipNotificationsOnSaveOrUpdate());
        pitGroupSet.setFlatPrivilegeNotificationsOnSaveOrUpdate(this.getFlatPrivilegeNotificationsOnSaveOrUpdate());
        pitGroupSet.saveOrUpdate();
      }
    }
    
    super.onPostUpdate(hibernateSession);
  }

  
  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public PITGroupSet dbVersion() {
    return (PITGroupSet)this.dbVersion;
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
      throw new RuntimeException("Cannot delete active point in time group set object with id=" + this.getId());
    }
    
    // Note that not all group sets that exist because of this group set are deleted by this.
    // We're assuming that group sets only get deleted when all the memberships for the group, stem, or attr def
    // are getting deleted.  So some group sets get deleted separately.

    Set<PITGroupSet> childResults = GrouperDAOFactory.getFactory().getPITGroupSet().findImmediateChildren(this);

    for (PITGroupSet child : childResults) {
      GrouperDAOFactory.getFactory().getPITGroupSet().delete(child);
    }
  }

}
