package edu.internet2.middleware.grouper.pit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

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
  
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

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
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_ACTIVE_DB, FIELD_START_TIME_DB, FIELD_END_TIME_DB,
      FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, FIELD_ATTRIBUTE_ASSIGN_TYPE, FIELD_ATTRIBUTE_DEF_NAME_ID,
      FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, FIELD_OWNER_ATTRIBUTE_DEF_ID, FIELD_OWNER_GROUP_ID,
      FIELD_OWNER_MEMBER_ID, FIELD_OWNER_MEMBERSHIP_ID, FIELD_OWNER_STEM_ID);


  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, FIELD_ATTRIBUTE_ASSIGN_TYPE, FIELD_ATTRIBUTE_DEF_NAME_ID, 
      FIELD_CONTEXT_ID, FIELD_END_TIME_DB, FIELD_ID, 
      FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, FIELD_OWNER_ATTRIBUTE_DEF_ID, FIELD_OWNER_GROUP_ID, FIELD_OWNER_MEMBER_ID, 
      FIELD_OWNER_MEMBERSHIP_ID, FIELD_OWNER_STEM_ID, FIELD_START_TIME_DB);


  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ATTRIBUTE_ASSIGN = "grouper_pit_attribute_assign";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;
  
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

  /** whether there will be flat permission notifications when this object is saved or updated */ 
  private boolean flatPermissionNotificationsOnSaveOrUpdate = false;
  
  /**
   * @return boolean
   */
  public boolean getFlatPermissionNotificationsOnSaveOrUpdate() {
    return flatPermissionNotificationsOnSaveOrUpdate;
  }
  
  /**
   * @param flatPermissionNotificationsOnSaveOrUpdate
   */
  public void setFlatPermissionNotificationsOnSaveOrUpdate(boolean flatPermissionNotificationsOnSaveOrUpdate) {
    this.flatPermissionNotificationsOnSaveOrUpdate = flatPermissionNotificationsOnSaveOrUpdate;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public PITAttributeAssign clone() {
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
    // if the id already exists for an inactive attribute assign, let's rename the id to avoid a conflict.
    PITAttributeAssign existing = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(this.getId());
    if (existing != null && !existing.isActive() && this.isActive()) {
      
      // first make a copy of the attribute assignment with a new id
      PITAttributeAssign existingCopy = existing.clone();
      existingCopy.setId(GrouperUuid.getUuid());
      existingCopy.setHibernateVersionNumber(-1L);
      existingCopy.save();
      
      // next update the assignment id for attribute values and other attribute assignments where this is the owner
      GrouperDAOFactory.getFactory().getPITAttributeAssignValue().updateAttributeAssignId(existing.getId(), existingCopy.getId());
      GrouperDAOFactory.getFactory().getPITAttributeAssign().updateOwnerAttributeAssignId(existing.getId(), existingCopy.getId());
      
      // delete the old assignment and add the new one now...
      existing.delete();
      GrouperDAOFactory.getFactory().getPITAttributeAssign().saveOrUpdate(this);

      {
        // next add values for the assignment being saved and add end dates to existing values.
        Set<PITAttributeAssignValue> values = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findActiveByAttributeAssignId(existingCopy.getId());
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
          
          value.setAttributeAssignId(this.getId());
          value.setStartTimeDb(this.getStartTimeDb());
          value.setContextId(this.getContextId());
          value.update();
        }
      }
      
      {
        // do the same for other attribute assignments where this is the owner
        Set<PITAttributeAssign> assignments = GrouperDAOFactory.getFactory().getPITAttributeAssign().findActiveByOwnerAttributeAssignId(existingCopy.getId());
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
          
          assignment.setOwnerAttributeAssignId(this.getId());
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
        }
      }
      
      return;
    }
    
    GrouperDAOFactory.getFactory().getPITAttributeAssign().saveOrUpdate(this);
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
    
    // add change log entry for flat permissions
    if (!this.isActive() && this.dbVersion().isActive() && this.getFlatPermissionNotificationsOnSaveOrUpdate()) {
      Set<ChangeLogEntry> changeLogEntryBatch = new LinkedHashSet<ChangeLogEntry>();
      int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
      if (batchSize <= 0) {
        batchSize = 1;
      }

      Set<PITPermissionAllView> perms = GrouperDAOFactory.getFactory().getPITPermissionAllView().findNewOrDeletedFlatPermissionsAfterAttributeAssignAddOrDelete(this.id);
      Iterator<PITPermissionAllView> iter = perms.iterator();
      
      Set<MultiKey> processed = new HashSet<MultiKey>();
      
      while (iter.hasNext()) {
        PITPermissionAllView perm = iter.next();
        
        MultiKey key = new MultiKey(perm.getAttributeDefNameId(), perm.getActionId(), perm.getMemberId());
        if (processed.add(key)) {
          ChangeLogEntry changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PERMISSION_DELETE,
              ChangeLogLabels.PERMISSION_DELETE.attributeDefNameName.name(), perm.getAttributeDefNameName(),
              ChangeLogLabels.PERMISSION_DELETE.attributeDefNameId.name(), perm.getAttributeDefNameId(),
              ChangeLogLabels.PERMISSION_DELETE.action.name(), perm.getAction(),
              ChangeLogLabels.PERMISSION_DELETE.actionId.name(), perm.getActionId(),
              ChangeLogLabels.PERMISSION_DELETE.subjectId.name(), perm.getSubjectId(),
              ChangeLogLabels.PERMISSION_DELETE.subjectSourceId.name(), perm.getSubjectSourceId(),
              ChangeLogLabels.PERMISSION_DELETE.memberId.name(), perm.getMemberId());
              
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
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    
    // add change log entry for flat permissions
    if (this.isActive() && this.getFlatPermissionNotificationsOnSaveOrUpdate()) {
      Set<ChangeLogEntry> changeLogEntryBatch = new LinkedHashSet<ChangeLogEntry>();
      int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 20);
      if (batchSize <= 0) {
        batchSize = 1;
      }

      Set<PITPermissionAllView> perms = GrouperDAOFactory.getFactory().getPITPermissionAllView().findNewOrDeletedFlatPermissionsAfterAttributeAssignAddOrDelete(this.id);
      Iterator<PITPermissionAllView> iter = perms.iterator();
      
      Set<MultiKey> processed = new HashSet<MultiKey>();
      
      while (iter.hasNext()) {
        PITPermissionAllView perm = iter.next();
        
        MultiKey key = new MultiKey(perm.getAttributeDefNameId(), perm.getActionId(), perm.getMemberId());
        if (processed.add(key)) {
          ChangeLogEntry changeLogEntry = new ChangeLogEntry(false, ChangeLogTypeBuiltin.PERMISSION_ADD,
              ChangeLogLabels.PERMISSION_ADD.attributeDefNameName.name(), perm.getAttributeDefNameName(),
              ChangeLogLabels.PERMISSION_ADD.attributeDefNameId.name(), perm.getAttributeDefNameId(),
              ChangeLogLabels.PERMISSION_ADD.action.name(), perm.getAction(),
              ChangeLogLabels.PERMISSION_ADD.actionId.name(), perm.getActionId(),
              ChangeLogLabels.PERMISSION_ADD.subjectId.name(), perm.getSubjectId(),
              ChangeLogLabels.PERMISSION_ADD.subjectSourceId.name(), perm.getSubjectSourceId(),
              ChangeLogLabels.PERMISSION_ADD.memberId.name(), perm.getMemberId());
              
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
}
