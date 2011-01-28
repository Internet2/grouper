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
public class PITRoleSet extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** depth */
  public static final String COLUMN_DEPTH = "depth";

  /** ifHasRoleId */
  public static final String COLUMN_IF_HAS_ROLE_ID = "if_has_role_id";

  /** thenHasRoleId */
  public static final String COLUMN_THEN_HAS_ROLE_ID = "then_has_role_id";

  /** parentRoleSetId */
  public static final String COLUMN_PARENT_ROLE_SET_ID = "parent_role_set_id";
  
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasRoleId */
  public static final String FIELD_IF_HAS_ROLE_ID = "ifHasRoleId";

  /** constant for field name for: thenHasRoleId */
  public static final String FIELD_THEN_HAS_ROLE_ID = "thenHasRoleId";

  /** constant for field name for: parentRoleSetId */
  public static final String FIELD_PARENT_ROLE_SET_ID = "parentRoleSetId";
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_DEPTH, FIELD_IF_HAS_ROLE_ID, FIELD_THEN_HAS_ROLE_ID,
      FIELD_PARENT_ROLE_SET_ID, FIELD_ACTIVE_DB, FIELD_START_TIME_DB,
      FIELD_END_TIME_DB);


  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_CONTEXT_ID, FIELD_DEPTH, FIELD_END_TIME_DB, 
      FIELD_ID, FIELD_IF_HAS_ROLE_ID, FIELD_PARENT_ROLE_SET_ID, 
      FIELD_START_TIME_DB, FIELD_THEN_HAS_ROLE_ID);


  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ROLE_SET = "grouper_pit_role_set";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   */
  private String parentRoleSetId;

  /** role id of the parent */
  private String thenHasRoleId;
  
  /** role id of the child */
  private String ifHasRoleId;
  
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
   * @return parentRoleSetId
   */
  public String getParentRoleSetId() {
    return parentRoleSetId;
  }

  /**
   * @param parentRoleSetId
   */
  public void setParentRoleSetId(String parentRoleSetId) {
    this.parentRoleSetId = parentRoleSetId;
  }

  /**
   * @return thenHasRoleId
   */
  public String getThenHasRoleId() {
    return thenHasRoleId;
  }

  /**
   * @param thenHasRoleId
   */
  public void setThenHasRoleId(String thenHasRoleId) {
    this.thenHasRoleId = thenHasRoleId;
  }

  /**
   * @return ifHasRoleId
   */
  public String getIfHasRoleId() {
    return ifHasRoleId;
  }

  /**
   * @param ifHasRoleId
   */
  public void setIfHasRoleId(String ifHasRoleId) {
    this.ifHasRoleId = ifHasRoleId;
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
    GrouperDAOFactory.getFactory().getPITRoleSet().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITRoleSet().delete(this);
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

      Set<PITPermissionAllView> perms = GrouperDAOFactory.getFactory().getPITPermissionAllView().findNewOrDeletedFlatPermissionsAfterRoleSetAddOrDelete(this.id);
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

      Set<PITPermissionAllView> perms = GrouperDAOFactory.getFactory().getPITPermissionAllView().findNewOrDeletedFlatPermissionsAfterRoleSetAddOrDelete(this.id);
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
  public PITRoleSet dbVersion() {
    return (PITRoleSet)this.dbVersion;
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
