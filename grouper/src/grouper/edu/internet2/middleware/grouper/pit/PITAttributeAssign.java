package edu.internet2.middleware.grouper.pit;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITAttributeAssign extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";
  
  /** column */
  public static final String COLUMN_OWNER_GROUP_ID = "owner_group_id";

  /** column */
  public static final String COLUMN_DISALLOWED = "disallowed";

  /** column */
  public static final String COLUMN_OWNER_STEM_ID = "owner_stem_id";

  /** column */
  public static final String COLUMN_OWNER_MEMBER_ID = "owner_member_id";

  /** column */
  public static final String COLUMN_OWNER_MEMBERSHIP_ID = "owner_membership_id";

  /** column */
  public static final String COLUMN_OWNER_ATTRIBUTE_ASSIGN_ID = "owner_attribute_assign_id";

  /** column */
  public static final String COLUMN_OWNER_ATTRIBUTE_DEF_ID = "owner_attribute_def_id";
  
  /** column */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_ACTION_ID = "attribute_assign_action_id";
  
  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_NAME_ID = "attribute_def_name_id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_TYPE = "attribute_assign_type";
  
  /** column */
  public static final String COLUMN_SOURCE_ID = "source_id";
  
  
  /** constant for field name for: sourceId */
  public static final String FIELD_SOURCE_ID = "sourceId";
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: disallowed */
  public static final String FIELD_DISALLOWED = "disallowed";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";
  
  /** constant for field name for: ownerAttributeAssignId */
  public static final String FIELD_OWNER_ATTRIBUTE_ASSIGN_ID = "ownerAttributeAssignId";

  /** constant for field name for: ownerAttributeDefId */
  public static final String FIELD_OWNER_ATTRIBUTE_DEF_ID = "ownerAttributeDefId";

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerMemberId */
  public static final String FIELD_OWNER_MEMBER_ID = "ownerMemberId";

  /** constant for field name for: ownerMembershipId */
  public static final String FIELD_OWNER_MEMBERSHIP_ID = "ownerMembershipId";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";
  
  /** constant for field name for: attributeAssignActionId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ACTION_ID = "attributeAssignActionId";

  /** constant for field name for: attributeAssignType */
  public static final String FIELD_ATTRIBUTE_ASSIGN_TYPE = "attributeAssignType";

  /** constant for field name for: attributeDefNameId */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_ID = "attributeDefNameId";
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_DISALLOWED, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB,
      FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, FIELD_ATTRIBUTE_ASSIGN_TYPE, FIELD_ATTRIBUTE_DEF_NAME_ID,
      FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, FIELD_OWNER_ATTRIBUTE_DEF_ID, FIELD_OWNER_GROUP_ID,
      FIELD_OWNER_MEMBER_ID, FIELD_OWNER_MEMBERSHIP_ID, FIELD_OWNER_STEM_ID, FIELD_SOURCE_ID);


  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, FIELD_ATTRIBUTE_ASSIGN_TYPE, FIELD_ATTRIBUTE_DEF_NAME_ID, 
      FIELD_CONTEXT_ID, FIELD_DISALLOWED, FIELD_END_TIME_DB, FIELD_ID, 
      FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, FIELD_OWNER_ATTRIBUTE_DEF_ID, FIELD_OWNER_GROUP_ID, FIELD_OWNER_MEMBER_ID, 
      FIELD_OWNER_MEMBERSHIP_ID, FIELD_OWNER_STEM_ID, FIELD_START_TIME_DB, FIELD_SOURCE_ID);


  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ATTRIBUTE_ASSIGN = "grouper_pit_attribute_assign";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;
  
  /** disallowed */
  private boolean disallowed;
  
  /** if this is an attribute assign attribute, this is the foreign key */
  private String ownerAttributeAssignId;
  
  /** if this is an attribute def attribute, this is the foreign key */
  private String ownerAttributeDefId;
  
  /** if this is a group attribute, this is the foreign key */
  private String ownerGroupId;
  
  /** if this is a member attribute, this is the foreign key */
  private String ownerMemberId;
  
  /** if this is a membership attribute, this is the foreign key */
  private String ownerMembershipId;
  
  /** if this is a stem attribute, this is the foreign key */
  private String ownerStemId;
  
  /** attributeAssignActionId */
  private String attributeAssignActionId;
  
  /** attributeAssignType */
  private String attributeAssignType;

  /** attributeDefNameId */
  private String attributeDefNameId;
  
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
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public PITAttributeAssign clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  
  
  
  /**
   * if permission is disallowed from a wider allow, null means false
   * @return the disallowed
   */
  public boolean isDisallowed() {
    return this.disallowed;
  }

  
  /**
   * if permission is disallowed from a wider allow, null means false
   * @param disallowed1 the disallowed to set
   */
  public void setDisallowed(boolean disallowed1) {
    this.disallowed = disallowed1;
  }

  /**
   * disallowed for the db string
   * @return the string
   */
  public String getDisallowedDb() {
    return this.disallowed ? "T" : "F";
  }
  
  /**
   * disallowed for the db string
   * @param theDisallowed the string T or F or null
   */
  public void setDisallowedDb(String theDisallowed) {
    this.disallowed = GrouperUtil.booleanValue(theDisallowed, false);
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
   * @return ownerAttributeAssignId
   */
  public String getOwnerAttributeAssignId() {
    return ownerAttributeAssignId;
  }

  /**
   * @param ownerAttributeAssignId
   */
  public void setOwnerAttributeAssignId(String ownerAttributeAssignId) {
    this.ownerAttributeAssignId = ownerAttributeAssignId;
  }

  /**
   * @return ownerAttributeDefId
   */
  public String getOwnerAttributeDefId() {
    return ownerAttributeDefId;
  }

  /**
   * @param ownerAttributeDefId
   */
  public void setOwnerAttributeDefId(String ownerAttributeDefId) {
    this.ownerAttributeDefId = ownerAttributeDefId;
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
  }

  /**
   * @return ownerMemberId
   */
  public String getOwnerMemberId() {
    return ownerMemberId;
  }

  /**
   * @param ownerMemberId
   */
  public void setOwnerMemberId(String ownerMemberId) {
    this.ownerMemberId = ownerMemberId;
  }

  /**
   * @return ownerMembershipId
   */
  public String getOwnerMembershipId() {
    return ownerMembershipId;
  }

  /**
   * @param ownerMembershipId
   */
  public void setOwnerMembershipId(String ownerMembershipId) {
    this.ownerMembershipId = ownerMembershipId;
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
  }

  /**
   * @return attributeAssignActionId
   */
  public String getAttributeAssignActionId() {
    return attributeAssignActionId;
  }

  /**
   * @param attributeAssignActionId
   */
  public void setAttributeAssignActionId(String attributeAssignActionId) {
    this.attributeAssignActionId = attributeAssignActionId;
  }

  /**
   * @return attributeAssignType
   */
  public String getAttributeAssignTypeDb() {
    return attributeAssignType;
  }

  /**
   * @param attributeAssignType
   */
  public void setAttributeAssignTypeDb(String attributeAssignType) {
    this.attributeAssignType = attributeAssignType;
  }

  /**
   * @return attributeDefNameId
   */
  public String getAttributeDefNameId() {
    return attributeDefNameId;
  }

  /**
   * @param attributeDefNameId
   */
  public void setAttributeDefNameId(String attributeDefNameId) {
    this.attributeDefNameId = attributeDefNameId;
  }

  /**
   * save this object
   */
  public void save() {
    // may need to create new child objects if this object is being re-enabled..
    Set<PITAttributeAssign> existingAll = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceId(this.getSourceId(), false);
    GrouperDAOFactory.getFactory().getPITAttributeAssign().saveOrUpdate(this);
    if (!this.isActive()) {
      return;
    }
    
    for (PITAttributeAssign existing : existingAll) {

      // add new values and end dates to existing ones.
      Set<PITAttributeAssignValue> values = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findActiveByAttributeAssignId(existing.getId());
      for (PITAttributeAssignValue value : values) {
        PITAttributeAssignValue valueCopy = value.clone();

        value.setEndTimeDb(this.getStartTimeDb());
        value.setActiveDb("F");
        value.setContextId(this.getContextId());
        value.update();
        
        valueCopy.setId(GrouperUuid.getUuid());
        valueCopy.setAttributeAssignId(this.getId());
        valueCopy.setStartTimeDb(this.getStartTimeDb());
        valueCopy.setContextId(this.getContextId());
        valueCopy.setHibernateVersionNumber(-1L);
        valueCopy.save();
      }
      
      // add new assignments and end dates to existing ones.
      Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findActiveByOwnerAttributeAssignId(existing.getId());
      for (PITAttributeAssign assignment : assignments) {
        PITAttributeAssign assignmentCopy = assignment.clone();

        assignment.setEndTimeDb(this.getStartTimeDb());
        assignment.setActiveDb("F");
        assignment.setContextId(this.getContextId());
        assignment.update();
        
        assignmentCopy.setId(GrouperUuid.getUuid());
        assignmentCopy.setOwnerAttributeAssignId(this.getId());
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
    GrouperDAOFactory.getFactory().getPITAttributeAssign().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(this);
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
    
    super.onPostSave(hibernateSession);
  }
  

  
  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public PITAttributeAssign dbVersion() {
    return (PITAttributeAssign)this.dbVersion;
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
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof PITAttributeAssign)) {
      return false;
    }
    
    return new EqualsBuilder().append(this.getId(), ((PITAttributeAssign) other).getId()).isEquals();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.getId()).toHashCode();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    if (this.isActive()) {
      throw new RuntimeException("Cannot delete active point in time attribute assign object with id=" + this.getId());
    }
    
    // delete attribute values
    Set<PITAttributeAssignValue> values = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findByAttributeAssignId(this.getId(), null);
    
    for (PITAttributeAssignValue value : values) {
      GrouperDAOFactory.getFactory().getPITAttributeAssignValue().delete(value);
    }
    
    
    // delete attribute assignments
    Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findByOwnerAttributeAssignId(this.getId());
    
    for (PITAttributeAssign assignment : assignments) {
      GrouperDAOFactory.getFactory().getPITAttributeAssign().delete(assignment);
    }
  }
  
  private PITAttributeAssignAction pitAttributeAssignAction = null;
  private PITAttributeDefName pitAttributeDefName = null;
  private PITGroup pitOwnerGroup = null;
  private PITAttributeAssign pitOwnerAttributeAssign = null;
  private PITAttributeDef pitOwnerAttributeDef = null;
  private PITMember pitOwnerMember = null;
  private PITMembership pitOwnerMembership = null;
  private PITStem pitOwnerStem = null;
  
  /**
   * @return pitAttributeAssignAction
   */
  public PITAttributeAssignAction getPITAttributeAssignAction() {
    if (pitAttributeAssignAction == null) {
      pitAttributeAssignAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(attributeAssignActionId, true);
    }
    
    return pitAttributeAssignAction;
  }
  
  /**
   * @return pitAttributeDefName
   */
  public PITAttributeDefName getPITAttributeDefName() {
    if (pitAttributeDefName == null) {
      pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefNameId, true);
    }
    
    return pitAttributeDefName;
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
   * @return pitOwnerAttributeAssign
   */
  public PITAttributeAssign getOwnerPITAttributeAssign() {
    if (pitOwnerAttributeAssign == null && ownerAttributeAssignId != null) {
      pitOwnerAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(ownerAttributeAssignId, true);
    }
    
    return pitOwnerAttributeAssign;
  }
  
  /**
   * @return pitOwnerAttributeDef
   */
  public PITAttributeDef getOwnerPITAttributeDef() {
    if (pitOwnerAttributeDef == null && ownerAttributeDefId != null) {
      pitOwnerAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(ownerAttributeDefId, true);
    }
    
    return pitOwnerAttributeDef;
  }
  
  /**
   * @return pitOwnerMember
   */
  public PITMember getOwnerPITMember() {
    if (pitOwnerMember == null && ownerMemberId != null) {
      pitOwnerMember = GrouperDAOFactory.getFactory().getPITMember().findById(ownerMemberId, true);
    }
    
    return pitOwnerMember;
  }
  
  /**
   * @return pitOwnerMembership
   */
  public PITMembership getOwnerPITMembership() {
    if (pitOwnerMembership == null && ownerMembershipId != null) {
      pitOwnerMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(ownerMembershipId, true);
    }
    
    return pitOwnerMembership;
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
