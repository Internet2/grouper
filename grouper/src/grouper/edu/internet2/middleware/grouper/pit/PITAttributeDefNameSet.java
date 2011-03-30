package edu.internet2.middleware.grouper.pit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITAttributeDefNameSet extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** depth */
  public static final String COLUMN_DEPTH = "depth";

  /** ifHasAttributeDefNameId */
  public static final String COLUMN_IF_HAS_ATTRIBUTE_DEF_NAME_ID = "if_has_attribute_def_name_id";

  /** thenHasAttributeDefNameId */
  public static final String COLUMN_THEN_HAS_ATTRIBUTE_DEF_NAME_ID = "then_has_attribute_def_name_id";

  /** parentAttrDefNameSetId */
  public static final String COLUMN_PARENT_ATTR_DEF_NAME_SET_ID = "parent_attr_def_name_set_id";
  
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasAttributeDefNameId */
  public static final String FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID = "ifHasAttributeDefNameId";

  /** constant for field name for: thenHasAttributeDefNameId */
  public static final String FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID = "thenHasAttributeDefNameId";

  /** constant for field name for: parentAttrDefNameSetId */
  public static final String FIELD_PARENT_ATTR_DEF_NAME_SET_ID = "parentAttrDefNameSetId";
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_DEPTH, FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID, FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID,
      FIELD_PARENT_ATTR_DEF_NAME_SET_ID, FIELD_ACTIVE_DB, FIELD_START_TIME_DB,
      FIELD_END_TIME_DB);

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_CONTEXT_ID, FIELD_DEPTH, FIELD_END_TIME_DB, 
      FIELD_ID, FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID, FIELD_PARENT_ATTR_DEF_NAME_SET_ID, 
      FIELD_START_TIME_DB, FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID);


  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ATTRIBUTE_DEF_NAME_SET = "grouper_pit_attr_def_name_set";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   */
  private String parentAttrDefNameSetId;

  /** attribute def name id of the parent */
  private String thenHasAttributeDefNameId;
  
  /** attribute def name id of the child */
  private String ifHasAttributeDefNameId;
  
  /**
   * depth - 0 for self records, 1 for immediate memberships, > 1 for effective 
   */
  private int depth;
  
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
   * @return parentAttrDefNameSetId
   */
  public String getParentAttrDefNameSetId() {
    return parentAttrDefNameSetId;
  }

  /**
   * @param parentAttrDefNameSetId
   */
  public void setParentAttrDefNameSetId(String parentAttrDefNameSetId) {
    this.parentAttrDefNameSetId = parentAttrDefNameSetId;
  }

  /**
   * @return thenHasAttributeDefNameId
   */
  public String getThenHasAttributeDefNameId() {
    return thenHasAttributeDefNameId;
  }

  /**
   * @param thenHasAttributeDefNameId
   */
  public void setThenHasAttributeDefNameId(String thenHasAttributeDefNameId) {
    this.thenHasAttributeDefNameId = thenHasAttributeDefNameId;
  }

  /**
   * @return ifHasAttributeDefNameId
   */
  public String getIfHasAttributeDefNameId() {
    return ifHasAttributeDefNameId;
  }

  /**
   * @param ifHasAttributeDefNameId
   */
  public void setIfHasAttributeDefNameId(String ifHasAttributeDefNameId) {
    this.ifHasAttributeDefNameId = ifHasAttributeDefNameId;
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
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().delete(this);
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

      Set<PITPermissionAllView> perms = GrouperDAOFactory.getFactory().getPITPermissionAllView().findNewOrDeletedFlatPermissionsAfterAttributeDefNameSetAddOrDelete(this.id);
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

      Set<PITPermissionAllView> perms = GrouperDAOFactory.getFactory().getPITPermissionAllView().findNewOrDeletedFlatPermissionsAfterAttributeDefNameSetAddOrDelete(this.id);
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
  public PITAttributeDefNameSet dbVersion() {
    return (PITAttributeDefNameSet)this.dbVersion;
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
      throw new RuntimeException("Cannot delete active point in time attribute def name set object with id=" + this.getId());
    }
    
    // Note that not all attribute def name sets that exist because of this attribute def name set are deleted by this.
    // We're assuming that attribute def name sets only get deleted when the attribute def name is deleted.  
    // So some attribute def name sets get deleted separately.

    Set<PITAttributeDefNameSet> childResults = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findImmediateChildren(this);

    for (PITAttributeDefNameSet child : childResults) {
      GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().delete(child);
    }
  }
}
