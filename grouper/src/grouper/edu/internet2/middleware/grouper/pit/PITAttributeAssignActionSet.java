package edu.internet2.middleware.grouper.pit;

import java.util.LinkedHashSet;
import java.util.Set;

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
public class PITAttributeAssignActionSet extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** depth */
  public static final String COLUMN_DEPTH = "depth";

  /** ifHasAttrAssnActionId */
  public static final String COLUMN_IF_HAS_ATTR_ASSN_ACTION_ID = "if_has_attr_assn_action_id";

  /** thenHasAttrAssnActionId */
  public static final String COLUMN_THEN_HAS_ATTR_ASSN_ACTION_ID = "then_has_attr_assn_action_id";

  /** parentAttrAssignActionSetId */
  public static final String COLUMN_PARENT_ATTR_ASSN_ACTION_ID = "parent_attr_assn_action_id";
  
  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasAttrAssignActionId */
  public static final String FIELD_IF_HAS_ATTR_ASSN_ACTION_ID = "ifHasAttrAssignActionId";

  /** constant for field name for: parentAttrAssignActionSetId */
  public static final String FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID = "parentAttrAssignActionSetId";

  /** constant for field name for: thenHasAttrAssignActionId */
  public static final String FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID = "thenHasAttrAssignActionId";
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_DEPTH, FIELD_IF_HAS_ATTR_ASSN_ACTION_ID, FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID,
      FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID, FIELD_ACTIVE_DB, FIELD_START_TIME_DB,
      FIELD_END_TIME_DB);



  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ACTIVE_DB, FIELD_CONTEXT_ID, FIELD_DEPTH, FIELD_END_TIME_DB, 
      FIELD_ID, FIELD_IF_HAS_ATTR_ASSN_ACTION_ID, FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID, 
      FIELD_START_TIME_DB, FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID);


  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_ATTR_ASSIGN_ACTION_SET = "grouper_pit_attr_assn_actn_set";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   */
  private String parentAttrAssignActionSetId;

  /** action id of the parent */
  private String thenHasAttrAssignActionId;
  
  /** action id of the child */
  private String ifHasAttrAssignActionId;
  
  /**
   * depth - 0 for self records, 1 for immediate memberships, > 1 for effective 
   */
  private int depth;
  
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
   * @return parentAttrAssignActionSetId
   */
  public String getParentAttrAssignActionSetId() {
    return parentAttrAssignActionSetId;
  }

  /**
   * @param parentAttrAssignActionSetId
   */
  public void setParentAttrAssignActionSetId(String parentAttrAssignActionSetId) {
    this.parentAttrAssignActionSetId = parentAttrAssignActionSetId;
  }

  /**
   * @return thenHasAttrAssignActionId
   */
  public String getThenHasAttrAssignActionId() {
    return thenHasAttrAssignActionId;
  }

  /**
   * @param thenHasAttrAssignActionId
   */
  public void setThenHasAttrAssignActionId(String thenHasAttrAssignActionId) {
    this.thenHasAttrAssignActionId = thenHasAttrAssignActionId;
  }

  /**
   * @return ifHasAttrAssignActionId
   */
  public String getIfHasAttrAssignActionId() {
    return ifHasAttrAssignActionId;
  }

  /**
   * @param ifHasAttrAssignActionId
   */
  public void setIfHasAttrAssignActionId(String ifHasAttrAssignActionId) {
    this.ifHasAttrAssignActionId = ifHasAttrAssignActionId;
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
    GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().delete(this);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    // add change log entry for permissions
    if (!this.isActive() && this.dbVersion().isActive() && this.getDepth() == 1 && this.getNotificationsForRolesWithPermissionChangesOnSaveOrUpdate()) {
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
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    
    // add change log entry for permissions
    if (this.isActive() && this.getDepth() == 1 && this.getNotificationsForRolesWithPermissionChangesOnSaveOrUpdate()) {
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
    
    super.onPostSave(hibernateSession);
  }

  
  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public PITAttributeAssignActionSet dbVersion() {
    return (PITAttributeAssignActionSet)this.dbVersion;
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
      throw new RuntimeException("Cannot delete active point in time action set object with id=" + this.getId());
    }
    
    // Note that not all action sets that exist because of this action set are deleted by this.
    // We're assuming that action sets only get deleted when the action is deleted.  
    // So some action sets get deleted separately.

    Set<PITAttributeAssignActionSet> childResults = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findImmediateChildren(this);

    for (PITAttributeAssignActionSet child : childResults) {
      GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().delete(child);
    }
  }
}
