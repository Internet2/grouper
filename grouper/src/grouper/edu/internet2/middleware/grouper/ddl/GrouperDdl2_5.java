package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdlWorker;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordRecentlyUsed;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.messaging.GrouperMessageHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDdl2_5 {

  /**
   * if building to this version at least
   */
  public static boolean buildingToThisVersionAtLeast(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToThisVersionAtLeast = GrouperDdl.V32.getVersion() <= buildingToVersion;

    return buildingToThisVersionAtLeast;
  }

  /**
   * if building to this version at least
   */
  private static boolean buildingFromScratch(DdlVersionBean ddlVersionBean) {
    int buildingFromVersion = ddlVersionBean.getBuildingFromVersion();
    if (buildingFromVersion <= 0) {
      return true;
    }
    return false;
  }

  /**
   * if building to this version at least
   */
  private static boolean buildingToPreviousVersion(DdlVersionBean ddlVersionBean) {
    int buildingToVersion = ddlVersionBean.getBuildingToVersion();
    
    boolean buildingToPreviousVersion = GrouperDdl.V32.getVersion() > buildingToVersion;

    return buildingToPreviousVersion;
  }

  static void addGrouperPasswordComments(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperPasswordComments", true)) {
      return;
    }
    
    {
      GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          "entries for grouper usernames passwords");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD,
          GrouperPassword.COLUMN_ID, 
            "uuid of this entry (one user could have ui and ws credential)");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD,  
          GrouperPassword.COLUMN_USER_NAME,
            "username or local entity system name");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD,  
          GrouperPassword.COLUMN_MEMBER_ID,
            "this is a reference to the grouper members table");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD,  
          GrouperPassword.COLUMN_ENTITY_TYPE,
            "username or localEntity");
      
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD,  
          GrouperPassword.COLUMN_IS_HASHED,
            "T for is hashed, F for is public key");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_ENCRYPTION_TYPE, 
          "key type. eg: SHA-256 or RS-256");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_SALT, 
          "secure random prepended to hashed pass");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_PASSWORD, 
          "encrypted public key or encrypted hashed salted password");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_APPLICATION, 
          "ws (includes scim) or ui");
      
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_ALLOWED_FROM_CIDRS, 
          "network cidrs where credential is allowed from");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_RECENT_SOURCE_ADDRESSES, 
          "json with timestamps");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_FAILED_SOURCE_ADDRESSES, 
          "if restricted by cidr, this was failed IPs (json with timestamp)");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_LAST_AUTHENTICATED, 
          "when last authenticated");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_LAST_EDITED, 
          "when last edited");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_FAILED_LOGINS, 
          "json of failed attempts");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPassword.TABLE_GROUPER_PASSWORD, 
          GrouperPassword.COLUMN_HIBERNATE_VERSION_NUMBER,
          "hibernate uses this to version rows");
  
    }
  
    {
      GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
          GrouperPasswordRecentlyUsed.TABLE_GROUPER_PASSWORD_RECENTLY_USED, 
          "recently used jwt tokens so they arent re-used");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPasswordRecentlyUsed.TABLE_GROUPER_PASSWORD_RECENTLY_USED,
          GrouperPasswordRecentlyUsed.COLUMN_ID, 
            "uuid of this entry");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPasswordRecentlyUsed.TABLE_GROUPER_PASSWORD_RECENTLY_USED,
          GrouperPasswordRecentlyUsed.COLUMN_GROUPER_PASSWORD_ID, 
            "password uuid for this jwt");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPasswordRecentlyUsed.TABLE_GROUPER_PASSWORD_RECENTLY_USED,
          GrouperPasswordRecentlyUsed.COLUMN_JWT_JTI, 
            "unique identifier of the login");
  
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
          GrouperPasswordRecentlyUsed.TABLE_GROUPER_PASSWORD_RECENTLY_USED,
          GrouperPasswordRecentlyUsed.COLUMN_JWT_IAT, 
            "timestamp of this entry");
  
    }
  
  }

  static void addGroupEnabledDisabledColumns(Database database, DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addGroupEnabledDisabledColumns", true)) {
      return;
    }

    Table groupTable = GrouperDdlUtils.ddlutilsFindTable(database, Group.TABLE_GROUPER_GROUPS, true);
    
    
    if (buildingFromScratch(ddlVersionBean)) {
      
      //this is required if the group table is new    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, Group.COLUMN_ENABLED, Types.VARCHAR, "1", false, true, "T");
    } else {
      if (!GrouperDdlUtils.isPostgres()) {

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, Group.COLUMN_ENABLED, Types.VARCHAR, "1", false, false, "T");
        
      }
    }

    // just do nothing if there is no upgrade.  i.e. the database already has this
    if (!buildingFromScratch(ddlVersionBean) && GrouperDdlUtils.isPostgres() && ddlVersionBean.getBuildingFromVersion() < GrouperDdl.V32.getVersion()) {
      
      // this will recreate the grouper_groups table in postgres on an existing installation if you dont do this
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_groups ADD COLUMN enabled VARCHAR(1);\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_groups ADD COLUMN enabled_timestamp BIGINT;\n");
      ddlVersionBean.getAdditionalScripts().append("ALTER TABLE grouper_groups ADD COLUMN disabled_timestamp BIGINT;\n");
      ddlVersionBean.getAdditionalScripts().append("CREATE INDEX group_enabled_idx ON grouper_groups (enabled);\n");
      ddlVersionBean.getAdditionalScripts().append("CREATE INDEX group_enabled_time_idx ON grouper_groups (enabled_timestamp);\n");
      ddlVersionBean.getAdditionalScripts().append("CREATE INDEX group_disabled_time_idx ON grouper_groups (disabled_timestamp);\n");
      
    } else {
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, Group.COLUMN_ENABLED_TIMESTAMP, Types.BIGINT, "20", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, Group.COLUMN_DISABLED_TIMESTAMP, Types.BIGINT, "20", false, false);    
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupTable.getName(), "group_enabled_idx", false, Group.COLUMN_ENABLED); 
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupTable.getName(), "group_enabled_time_idx", false, Group.COLUMN_ENABLED_TIMESTAMP);
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupTable.getName(), "group_disabled_time_idx", false, Group.COLUMN_DISABLED_TIMESTAMP);
      
    }
    
    if (!buildingFromScratch(ddlVersionBean)) {
      boolean needUpdate = false;
      
      try {
        int count = HibernateSession.bySqlStatic().select(int.class, "select count(*) from grouper_groups");
        if (count > 0) {
          needUpdate = true;
        }
      } catch (Exception e) {
        needUpdate = false;
        LOG.info("Exception querying grouper_groups", e);
        // group table doesnt exist?
      }
      
      if (needUpdate) {
        ddlVersionBean.getAdditionalScripts().append(
            "update grouper_groups set enabled='T' where enabled is null;\n" +
            "commit;\n");
      }
    }
    
    if (!buildingFromScratch(ddlVersionBean) && GrouperDdlUtils.isPostgres()) {
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Group.TABLE_GROUPER_GROUPS + " ALTER COLUMN " + Group.COLUMN_ENABLED + " SET NOT NULL;\n");
      ddlVersionBean.getAdditionalScripts().append(          
          "ALTER TABLE " + Group.TABLE_GROUPER_GROUPS + " ALTER COLUMN " + Group.COLUMN_ENABLED + " SET DEFAULT 'T';\n");      
    }    
  }


  /**
   * add grouper password foreign keys
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperPasswordForeignKeys(DdlVersionBean ddlVersionBean, Database database) {
    

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperPasswordForeignKeys", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(database, GrouperPasswordRecentlyUsed.TABLE_GROUPER_PASSWORD_RECENTLY_USED,
      "fk_grouper_password_id", GrouperPassword.TABLE_GROUPER_PASSWORD, GrouperPasswordRecentlyUsed.COLUMN_GROUPER_PASSWORD_ID, GrouperPassword.COLUMN_ID);
  }
  
  /**
   * add grouper password tables
   * @param ddlVersionBean
   * @param database
   */
  static void addGrouperPasswordTables(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperPasswordTables", true)) {
      return;
    }

    
    {
      Table grouperPasswordTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          GrouperPassword.TABLE_GROUPER_PASSWORD);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_ID,
          Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_USER_NAME, 
          Types.VARCHAR, "255", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_MEMBER_ID, 
          Types.VARCHAR, "40", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_ENTITY_TYPE, 
          Types.VARCHAR, "20", false, false);
  
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_IS_HASHED, 
          Types.VARCHAR, "1", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_ENCRYPTION_TYPE, 
          Types.VARCHAR, "20", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_SALT, 
          Types.VARCHAR, "255", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_PASSWORD, 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_APPLICATION, 
          Types.VARCHAR, "20", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_ALLOWED_FROM_CIDRS, 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_RECENT_SOURCE_ADDRESSES, 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_FAILED_SOURCE_ADDRESSES, 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_LAST_AUTHENTICATED, 
          Types.BIGINT, "20", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_LAST_EDITED, 
          Types.BIGINT, "20", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_FAILED_LOGINS, 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordTable, GrouperPassword.COLUMN_HIBERNATE_VERSION_NUMBER, 
          Types.BIGINT, null, false, false);
      
    }
    
    {
      Table grouperPasswordRecentlyUsedTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          GrouperPasswordRecentlyUsed.TABLE_GROUPER_PASSWORD_RECENTLY_USED);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_ID,
          Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_GROUPER_PASSWORD_ID,
          Types.VARCHAR, "40", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_JWT_JTI, 
          Types.VARCHAR, "100", false, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperPasswordRecentlyUsedTable, GrouperPasswordRecentlyUsed.COLUMN_JWT_IAT, 
          Types.INTEGER, "11", false, true);
      
    }
    
  }

  /**
   * Add grouper password indexes
   * @param ddlVersionBean 
   * @param database
   */
  static void addGrouperPasswordIndexes(DdlVersionBean ddlVersionBean, Database database) {
    

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperPasswordIndexes", true)) {
      return;
    }
    
    {
      Table grouperPasswordTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          GrouperPassword.TABLE_GROUPER_PASSWORD);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, grouperPasswordTable.getName(), 
          "grppassword_username_idx", true, GrouperPassword.COLUMN_USER_NAME, GrouperPassword.COLUMN_APPLICATION);
      
    }
  }

  public static void addDdlWorkerTableIfNotThere() {

    // this is added on startup...
    
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().listSelect(Hib3GrouperDdlWorker.class, "select * from grouper_ddl_worker", null, null);
      return;
    } catch (Exception e) {
      //not found
    }

//    // if no tables are there, then dont do this :)
//    try {
//      HibernateSession.bySqlStatic().listSelect(Hib3GrouperDdlWorker.class, "select * from grouper_groups where 1!=1", null, null);
//    } catch (Exception e) {
//      //not found, dont add a random table to who knows what database...
//      return;
//    }
    
    
    boolean runScript = GrouperDdlUtils.autoDdl2_5orAbove();
    if (!runScript) {
      LOG.error("You need to add the grouper_ddl_worker table!");
    }
    try {
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), false, runScript, new DdlUtilsChangeDatabase() {
        
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          addDdlWorkerTable(ddlVersionBean, ddlVersionBean.getDatabase());
          addDdlWorkerIndexes(ddlVersionBean, ddlVersionBean.getDatabase());
          addDdlWorkerComments(ddlVersionBean, ddlVersionBean.getDatabase());
        }
      });

    } catch (Exception e2) {

      GrouperUtil.sleep(3000);

      try {
        // if you cant connect to it, its not there
        HibernateSession.bySqlStatic().listSelect(Hib3GrouperDdlWorker.class, "select * from grouper_ddl_worker", null, null);
        return;
      } catch (Exception e) {
        //not found
      }

      LOG.error("error creating the grouper_ddl_worker table", e2);
    }

  }

  /**
   * 
   */
  static void addDdlWorkerTable(DdlVersionBean ddlVersionBean, Database database) {
    if (ddlVersionBean.didWeDoThis("v2_5_addDdlWorkerTable", true)) {
      return;
    }
    {
      final String tableName = "grouper_ddl_worker";
    
      Table ddlWorkerTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(ddlWorkerTable, "id", 
          Types.VARCHAR, "40", true, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(ddlWorkerTable, "grouper", 
          Types.VARCHAR, "40", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(ddlWorkerTable, "worker_uuid", 
          Types.VARCHAR, "40", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(ddlWorkerTable, "heartbeat", 
          Types.TIMESTAMP, "10", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(ddlWorkerTable, "last_updated", 
          Types.TIMESTAMP, "10", false, true);
    }
  }  

  /**
   * Add grouper password indexes
   * @param ddlVersionBean 
   * @param database
   */
  static void addDdlWorkerIndexes(DdlVersionBean ddlVersionBean, Database database) {
    if (ddlVersionBean.didWeDoThis("v2_5_addDdlWorkerIndexes", true)) {
      return;
    }
    
    {
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, "grouper_ddl_worker", 
          "grouper_ddl_worker_grp_idx", true, "grouper");
      
    }
  }


  /**
   * 
   */
  static void addDdlWorkerComments(DdlVersionBean ddlVersionBean, Database database) {
    if (ddlVersionBean.didWeDoThis("v2_5_addDdlWorkerComments", true)) {
      return;
    }
  
    final String tableName = "grouper_ddl_worker";

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, "JVMs register a uuid so only one JVM does the DDL upgrades at a time");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper", "this just holds the word grouper, so there is only one row here");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "worker_uuid", "random uuid from a jvm to do work on the database");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "heartbeat", "while the ddl is running, keep a heartbeat updated");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
    
  }
  /**
   * 
   */
  static void addSyncTables(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addSyncTables", true)) {
      return;
    }
    {
      final String tableName = "grouper_sync";
    
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
          Types.VARCHAR, "40", true, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "sync_engine", 
          Types.VARCHAR, "50", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisioner_name", 
          Types.VARCHAR, "100", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_count", 
          Types.INTEGER, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_count", 
          Types.INTEGER, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "records_count", 
          Types.INTEGER, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "incremental_index", 
          Types.BIGINT, "15", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "incremental_timestamp", 
          Types.TIMESTAMP, null, false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_incremental_sync_run", 
          Types.TIMESTAMP, null, false, false);
    
      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_full_sync_start", 
            Types.TIMESTAMP, null, false, false);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_full_sync_run", 
          Types.TIMESTAMP, null, false, false);
    
      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_full_metadata_sync_start", 
            Types.TIMESTAMP, null, false, false);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_full_metadata_sync_run", 
          Types.TIMESTAMP, null, false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
          Types.TIMESTAMP, "10", false, true);
    }
    {
      final String tableName = "grouper_sync_job";
      
      Table syncJobTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "id", 
          Types.VARCHAR, "40", true, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "grouper_sync_id", 
          Types.VARCHAR, "40", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "sync_type", 
          Types.VARCHAR, "50", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "job_state", 
          Types.VARCHAR, "50", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "last_sync_index", 
          Types.BIGINT, "15", false, false);
      
      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "last_sync_start", 
            Types.TIMESTAMP, null, false, false);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "last_sync_timestamp", 
          Types.TIMESTAMP, null, false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "last_time_work_was_done", 
          Types.TIMESTAMP, null, false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "heartbeat", 
          Types.TIMESTAMP, null, false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "quartz_job_name", 
          Types.VARCHAR, "400", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "percent_complete", 
          Types.INTEGER, "8", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "last_updated", 
          Types.TIMESTAMP, "10", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "error_message", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncJobTable, "error_timestamp", 
          Types.TIMESTAMP, "10", false, false);
      
    }
    {
      Table grouperSyncGroupTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
          "grouper_sync_group");
          
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "id", 
          Types.VARCHAR, "40", true, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "grouper_sync_id", 
          Types.VARCHAR, "40", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "group_id", 
          Types.VARCHAR, "40", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "group_name", 
          Types.VARCHAR, "1024", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "group_id_index", 
          Types.BIGINT, "12", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "provisionable", 
          Types.VARCHAR, "1", false, false);
    
//      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
//
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_grouper", 
//            Types.VARCHAR, "1", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_grouper_insert_or_exists", 
//            Types.VARCHAR, "1", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_grouper_start", 
//            Types.TIMESTAMP, "10", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_grouper_end", 
//            Types.TIMESTAMP, "10", false, false);
//
//      }
//
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_target", 
          Types.VARCHAR, "1", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_target_insert_or_exists", 
          Types.VARCHAR, "1", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_target_start", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "in_target_end", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "provisionable_start", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "provisionable_end", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_updated", 
          Types.TIMESTAMP, "10", false, true);
    
      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_group_sync_start", 
            Types.TIMESTAMP, "10", false, false);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_group_sync", 
          Types.TIMESTAMP, "10", false, false);
    
      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_group_metadata_sync_start", 
            Types.TIMESTAMP, "10", false, false);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_group_metadata_sync", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "group_from_id2", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "group_from_id3", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "group_to_id2", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "group_to_id3", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "metadata_updated", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "error_message", 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "error_timestamp", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouperSyncGroupTable, "last_time_work_was_done", 
          Types.TIMESTAMP, null, false, false);
    
      if (GrouperDdl2_5_40.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdl2_5_40.addGrouperSyncErrorCodeColumnsGroup(database, ddlVersionBean);
      }
      
      if (GrouperDdl2_5_51.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdl2_5_51.addGrouperSyncGroupMetadataJsonColumn(database, ddlVersionBean);
      }

    }
    
    {
      final String tableName = "grouper_sync_member";
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
          Types.VARCHAR, "40", true, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_id", 
          Types.VARCHAR, "40", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_id", 
          Types.VARCHAR, "128", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "source_id", 
          Types.VARCHAR, "255", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_id", 
          Types.VARCHAR, "255", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "subject_identifier", 
          Types.VARCHAR, "255", false, false);
    
//      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
//
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_grouper", 
//            Types.VARCHAR, "1", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_grouper_insert_or_exists", 
//            Types.VARCHAR, "1", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_grouper_start", 
//            Types.TIMESTAMP, "10", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_grouper_end", 
//            Types.TIMESTAMP, "10", false, false);
//
//      }
//
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target", 
          Types.VARCHAR, "1", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target_insert_or_exists", 
          Types.VARCHAR, "1", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target_start", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target_end", 
          Types.TIMESTAMP, "10", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable", 
          Types.VARCHAR, "1", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable_start", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable_end", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
          Types.TIMESTAMP, "10", false, true);
    
      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_user_sync_start", 
            Types.TIMESTAMP, "10", false, false);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_user_sync", 
          Types.TIMESTAMP, "10", false, false);
    
      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_user_metadata_sync_start", 
            Types.TIMESTAMP, "10", false, false);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_user_metadata_sync", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_from_id2", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_from_id3", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_to_id2", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "member_to_id3", 
          Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "metadata_updated", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_time_work_was_done", 
          Types.TIMESTAMP, null, false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "error_message", Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "error_timestamp", Types.TIMESTAMP, "10", false, false);
    
      if (GrouperDdl2_5_40.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdl2_5_40.addGrouperSyncErrorCodeColumnsMember(database, ddlVersionBean);
      }

      
    }
    {
      final String tableName = "grouper_sync_membership";
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
          Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_id", 
          Types.VARCHAR, "40", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_group_id", 
          Types.VARCHAR, "40", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_member_id", 
          Types.VARCHAR, "40", false, true);
      
//      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
//
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_grouper", 
//            Types.VARCHAR, "1", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_grouper_insert_or_exists", 
//            Types.VARCHAR, "1", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_grouper_start", 
//            Types.TIMESTAMP, "10", false, false);
//      
//        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_grouper_end", 
//            Types.TIMESTAMP, "10", false, false);
//
//      }
//
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target", 
          Types.VARCHAR, "1", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target_insert_or_exists", 
          Types.VARCHAR, "1", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target_start", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target_end", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
          Types.TIMESTAMP, "10", false, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "membership_id", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "membership_id2", 
          Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "metadata_updated", 
          Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "error_message", Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "error_timestamp", Types.TIMESTAMP, "10", false, false);
    
      if (GrouperDdl2_5_40.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdl2_5_40.addGrouperSyncErrorCodeColumnsMembership(database, ddlVersionBean);
      }

    }
    {
      final String tableName = "grouper_sync_log";
      
      Table syncLogTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "id", Types.VARCHAR, "40", true, true);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "grouper_sync_owner_id", Types.VARCHAR, "40", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "grouper_sync_id", Types.VARCHAR, "40", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "status", Types.VARCHAR, "20", false, false);
    
      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "sync_timestamp_start", Types.TIMESTAMP, "10", false, false);
      }
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "sync_timestamp", Types.TIMESTAMP, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "description", Types.VARCHAR, "4000", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "records_processed", Types.INTEGER, "10", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "records_changed", Types.INTEGER, "10", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "job_took_millis", Types.INTEGER, "10", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "server", Types.VARCHAR, "200", false, false);
    
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(syncLogTable, "last_updated", Types.TIMESTAMP, "10", false, true);
      

    }
  }

  /**
   * 
   */
  static void addSyncComments(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addSyncComments", true)) {
      return;
    }
  
    {
      final String tableName = "grouper_sync";
      GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
          tableName, "One record for every provisioner (not different records for full and real time)");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_engine", "e.g. for syncing sql, it sqlTableSync");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisioner_name", "name of provisioner must be unique.  this is the config key generally");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_count", "if group this is the number of groups");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "user_count", "if has users, this is the number of users");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "records_count", "number of records including users, groups, etc");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "incremental_index", "int of last record processed");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "incremental_timestamp", "timestamp of last record processed");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_incremental_sync_run", "when incremental sync ran");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_full_sync_run", "when last full sync ran");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_full_metadata_sync_run", "when last full metadata sync ran.  this needs to run when groups get renamed");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
    }   
    {
      final String tableName = "grouper_sync_job";
      
      GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
          tableName, "Status of all jobs for the sync.  one record for full, one for incremental, etc");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_id", "uuid of the job in grouper_sync table");
              
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_type", "type of sync, e.g. for sql sync this is the job subtype");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_state", "running, pending (if waiting for another job to finish), notRunning");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_sync_index", "either an int of last record checked, or an int of millis since 1970 of last record processed");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_sync_timestamp", "when last record processed if timestamp and not integer");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "heartbeat", "when a job is running this must be updated every 60 seconds in a thread or the job will be deemed not running by other jobs");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_done", "last time a record was processed");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "quartz_job_name", "name of quartz job if applicable");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "percent_complete", "0-100 percent complete of this job");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "error_message", "if there was an error when syncing this group, this is the message");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "error_timestamp", "timestamp of error if there was an error when syncing this group");
    
      
    }
    {
      String tableName = "grouper_sync_group";
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_id", "foreign key back to the sync table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_id", "if this is groups, then this is the uuid of the group, though not a real foreign key");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_name", "if this is groups, then this is the system name of the group");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_id_index", "if this is groups, then this is the id index of the group");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable", "T if provisionable and F is not");
    
//      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
//        
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper", "T if exists in grouper and F is not.  blank if not sure");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_insert_or_exists", "T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_start", "when this was put in grouper");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_end", "when this was taken out of grouper");
//
//      }
//      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target", "T if exists in target/destination and F is not.  blank if not sure");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_insert_or_exists", "T if inserted on the in_target_start date, or F if it existed then and not sure when inserted");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_start", "when this was put in target");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_end", "when this was taken out of target");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_start", "when this group started to be provisionable");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_end", "when this group ended being provisionable");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_group_sync", "when this group was last synced");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_group_metadata_sync", "when this groups name and description and metadata was synced");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_from_id2", "other metadata on groups");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_from_id3", "other metadata on groups");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_to_id2", "other metadata on groups");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "group_to_id3", "other metadata on groups");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_done", "last time a record was processed");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "metadata_updated", "when the metadata was last updated (if it times out)");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "error_message", "if there was an error when syncing this object, this is the message");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "error_timestamp", "timestamp of error if there was an error when syncing this object");
      
      if (GrouperDdl2_5_40.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdl2_5_40.addGrouperSyncErrorCodeCommentsGroup(database, ddlVersionBean);
      }
      
      if (GrouperDdl2_5_51.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdl2_5_51.addGrouperSyncGroupMetadataJsonComment(database, ddlVersionBean);
      }
    }
    {
      final String tableName = "grouper_sync_member";
      
      
      GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,  tableName, "user metadata for sync");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_id", "foreign key to grouper_sync table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "member_id", "foreign key to the members table, though not a real foreign key");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "source_id", "subject source id");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "subject_id", "subject id");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "subject_identifier", "netId or eppn or whatever");
      
//      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
//        
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper", "T if exists in grouper and F is not.  blank if not sure");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_insert_or_exists", "T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_start", "when this was put in grouper");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_end", "when this was taken out of grouper");
//
//      }
//
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target", "T if exists in target/destination and F is not.  blank if not sure");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_insert_or_exists", "T if inserted on the in_target_start date, or F if it existed then and not sure when inserted");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_start", "when the user was put in the target");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_end", "when the user was taken out of the target");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable", "T if provisionable and F is not");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_start", "when this user started to be provisionable");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_end", "when this user ended being provisionable");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_user_sync", "when this user was last synced, includes metadata and memberships");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_user_metadata_sync", "when this users name and description and metadata was synced");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "member_from_id2", "for users this is the user idIndex");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "member_from_id3", "other metadata on users");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "member_to_id2", "other metadat on users");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "member_to_id3", "other metadata on users");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_done", "last time a record was processed");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "metadata_updated", "when the metadata was last updated (if it times out)");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "error_message", "if there was an error when syncing this object, this is the message");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "error_timestamp", "timestamp of error if there was an error when syncing this object");
      if (GrouperDdl2_5_40.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdl2_5_40.addGrouperSyncErrorCodeCommentsMember(database, ddlVersionBean);
      }

    }
    {
      final String tableName = "grouper_sync_membership";
      
      GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,  tableName, "record of a sync_group and a sync_member represents a sync'ed membership");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_id", "foreign key back to sync table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_group_id", "foreign key back to sync group table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_member_id", "foreign key back to sync member table");
      
//      if (GrouperDdl2_5_38.buildingToThisVersionAtLeast(ddlVersionBean)) {
//        
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper", "T if exists in grouper and F is not.  blank if not sure");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_insert_or_exists", "T if inserted to grouper on the in_target_start date, or F if it existed then and not sure when inserted");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_start", "when this was put in grouper");
//
//        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_grouper_end", "when this was taken out of grouper");
//
//      }
//
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target", "T if exists in target/destination and F is not.  blank if not sure");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_insert_or_exists", "T if inserted on the in_target_start date, or F if it existed then and not sure when inserted");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_start", "when this was put in target");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_end", "when this was taken out of target");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "membership_id", "other metadata on membership");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "membership_id2", "other metadata on membership");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "metadata_updated", "when the metadata was last updated (if it times out)");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "error_message", "if there was an error when syncing this object, this is the message");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "error_timestamp", "timestamp of error if there was an error when syncing this object");

      if (GrouperDdl2_5_40.buildingToThisVersionAtLeast(ddlVersionBean)) {
        GrouperDdl2_5_40.addGrouperSyncErrorCodeCommentsMembership(database, ddlVersionBean);
      }

    }
  
    {
      final String tableName = "grouper_sync_log";
      GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,  tableName, "last log for this sync that affected this group or member etc");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_owner_id", "either the grouper_sync_membership_id or the grouper_sync_member_id or the grouper_sync_group_id or grouper_sync_job_id (if log for job wide)");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_id", "foreign key to grouper_sync table");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "status", "SUCCESS, ERROR, WARNING, CONFIG_ERROR");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_timestamp", "when the last sync started");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "description", "description of last sync");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "records_processed", "how many records were processed the last time this sync ran");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "records_changed", "how many records were changed the last time this sync ran");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_took_millis", "how many millis it took to run this job");
    
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "server", "which server this occurred on");
      
      GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
    
      
    }
  }

  /**
   * 
   */
  static void addSyncForeignKeys(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addSyncForeignKeys", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), "grouper_sync_job", 
        "grouper_sync_job_id_fk", "grouper_sync", "grouper_sync_id", "id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), "grouper_sync_group", 
        "grouper_sync_gr_id_fk", "grouper_sync", "grouper_sync_id", "id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), "grouper_sync_member", 
        "grouper_sync_us_id_fk", "grouper_sync", "grouper_sync_id", "id");
    
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), "grouper_sync_membership", 
        "grouper_sync_me_gid_fk", "grouper_sync_group", "grouper_sync_group_id", "id");
    
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), "grouper_sync_membership", 
        "grouper_sync_me_uid_fk", "grouper_sync_member", "grouper_sync_member_id", "id");
  
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), "grouper_sync_membership", 
        "grouper_sync_me_id_fk", "grouper_sync", "grouper_sync_id", "id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), "grouper_sync_log", 
        "grouper_sync_log_sy_fk", "grouper_sync", "grouper_sync_id", "id");
  }

  /**
   * 
   */
  static void addSyncIndexes(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addSyncIndexes", true)) {
      return;
    }
  
    {
      final String tableName = "grouper_sync";
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_eng_idx", true, "sync_engine", "provisioner_name");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_eng_prov_idx", true, "provisioner_name");
    }
    {
      final String tableName = "grouper_sync_job";
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_st_ty_idx", true, "grouper_sync_id", "sync_type");
    
    }
    {
      String tableName = "grouper_sync_group";
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_gr_sync_id_idx", false, "grouper_sync_id", "last_updated");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_gr_group_id_idx", false, "group_id", "last_updated");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_gr_sy_gr_idx", true, "grouper_sync_id", "group_id");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_gr_f2_idx", false, "grouper_sync_id", "group_from_id2(255)");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_gr_f3_idx", false, "grouper_sync_id", "group_from_id3(255)");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_gr_t2_idx", false, "grouper_sync_id", "group_to_id2(255)");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_gr_t3_idx", false, "grouper_sync_id", "group_to_id3(255)");
    
    }
    {
      final String tableName = "grouper_sync_member";
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_us_sync_id_idx", false, "grouper_sync_id", "last_updated");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_us_mem_id_idx", false, "member_id", "last_updated");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_us_sm_idx", true, "grouper_sync_id", "member_id");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_us_f2_idx", false, "grouper_sync_id", "member_from_id2(255)");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_us_f3_idx", false, "grouper_sync_id", "member_from_id3(255)");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_us_t2_idx", false, "grouper_sync_id", "member_to_id2(255)");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_us_t3_idx", false, "grouper_sync_id", "member_to_id3(255)");
    
      // there could be some overlap as things change with subjects (edge case)
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_us_st_gr_idx", false, "grouper_sync_id", "source_id", "subject_id");
    
    }
    {
      final String tableName = "grouper_sync_membership";
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_mship_gr_idx", true, "grouper_sync_id", "grouper_sync_group_id", "grouper_sync_member_id");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_mship_me_idx", false, "grouper_sync_member_id", "last_updated");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_mship_me_idx", false, "grouper_sync_group_id", "last_updated");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_mship_sy_idx", false, "grouper_sync_id", "last_updated");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_mship_f1_idx", false, "grouper_sync_id", "membership_id(255)");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_mship_f2_idx", false, "grouper_sync_id", "membership_id2(255)");
    
    }
    {
      final String tableName = "grouper_sync_log";
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_log_sy_idx", false, "grouper_sync_id", "sync_timestamp");
    
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
          "grouper_sync_log_ow_idx", false, "grouper_sync_owner_id", "sync_timestamp");

    }
  }

  static void adjustMessageMemberIdSize(DdlVersionBean ddlVersionBean, Database database) {
    

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_adjustMessageMemberIdSize", true)) {
      return;
    }

    //GRP-1979 - change grouper_message.from_member_id from 100->40 to match the key it references
    {
      Table messageTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,
        GrouperMessageHibernate.TABLE_GROUPER_MESSAGE);
  
      Column column = GrouperDdlUtils.ddlutilsFindColumn(
        messageTable, GrouperMessageHibernate.COLUMN_FROM_MEMBER_ID, true);
  
      if ((column.getTypeCode() == Types.VARCHAR) && !"40".equals(column.getSize())) {
        if (ddlVersionBean.isHsql()) {
          ddlVersionBean.appendAdditionalScriptUnique("\nALTER TABLE grouper_message ALTER COLUMN from_member_id VARCHAR(40);\n");
        } else if (ddlVersionBean.isOracle()) {
          ddlVersionBean.appendAdditionalScriptUnique("\nALTER TABLE GROUPER_MESSAGE MODIFY (FROM_MEMBER_ID VARCHAR(40));\n");
        } else if (ddlVersionBean.isPostgres()) {
          ddlVersionBean.appendAdditionalScriptUnique("\nALTER TABLE grouper_message ALTER COLUMN from_member_id TYPE VARCHAR(40);\nCOMMIT;\n");
        } else if (ddlVersionBean.isMysql()) {
          // disable fk checks to prevent "Cannot change column 'from_member_id': used in a foreign key constraint 'fk_message_from_member_id'"
          ddlVersionBean.appendAdditionalScriptUnique("\nSET FOREIGN_KEY_CHECKS=0;\n");
          ddlVersionBean.appendAdditionalScriptUnique("ALTER TABLE grouper_message MODIFY from_member_id VARCHAR(40);\n");
          ddlVersionBean.appendAdditionalScriptUnique("SET FOREIGN_KEY_CHECKS=1;\n");
        } else {
          //everywhere except MySQL and MSSQL generates a table drop/create
          column.setSize("40");
        }
      }
    }
  }

  static void addCompositeTypeIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addCompositeTypeIndex", true)) {
      return;
    }
    Table compositeTable = GrouperDdlUtils.ddlutilsFindTable(database, Composite.TABLE_GROUPER_COMPOSITES, true);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, compositeTable.getName(), 
        "composite_type_idx", false, "type");
  }

  static void addAttributeAssignTypeIndex(DdlVersionBean ddlVersionBean, Database database) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_addAttributeAssignTypeIndex", true)) {
      return;
    }
    Table attributeAssignTable = GrouperDdlUtils.ddlutilsFindTable(database, AttributeAssign.TABLE_GROUPER_ATTRIBUTE_ASSIGN, true);
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, attributeAssignTable.getName(), 
        "attr_asgn_type_idx", false, 
        AttributeAssign.COLUMN_ATTRIBUTE_ASSIGN_TYPE);
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDdl2_5.class);

  static void createViewGrouperRolesV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_createViewGrouperRolesV", true)) {
      return;
    }

    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_roles_v", 
        "Contains one record for each role, with friendly names for some attributes and some more information",
        GrouperUtil.toSet("EXTENSION", 
            "NAME", 
            "DISPLAY_EXTENSION", 
            "DISPLAY_NAME", 
            "DESCRIPTION", 
            "PARENT_STEM_NAME", 
            "ROLE_ID", 
            "PARENT_STEM_ID", 
            "ENABLED",
            "ENABLED_TIMESTAMP",
            "DISABLED_TIMESTAMP",
            "MODIFIER_SOURCE", 
            "MODIFIER_SUBJECT_ID", 
            "CREATOR_SOURCE", 
            "CREATOR_SUBJECT_ID", 
            "IS_COMPOSITE_OWNER", 
            "IS_COMPOSITE_FACTOR", 
            "CREATOR_ID", 
            "CREATE_TIME", 
            "MODIFIER_ID", 
            "MODIFY_TIME", 
            "HIBERNATE_VERSION_NUMBER", "CONTEXT_ID"),
        GrouperUtil.toSet("EXTENSION: part of role name not including path information, e.g. theRole", 
            "NAME: name of the role, e.g. school:stem1:theRole", 
            "DISPLAY_EXTENSION: name for display of the role, e.g. My school:The stem 1:The role", 
            "DISPLAY_NAME: name for display of the role without any path information, e.g. The role", 
            "DESCRIPTION: contains user entered information about the group e.g. why it exists", 
            "PARENT_STEM_NAME: name of the stem this role is in, e.g. school:stem1", 
            "ROLE_ID: uuid unique id of the role", 
            "PARENT_STEM_ID: uuid unique id of the stem this role is in", 
            "ENABLED: T or F to indicate if this role is enabled",
            "ENABLED_TIMESTAMP: when the role will be enabled if the time is in the future",
            "DISABLED_TIMESTAMP: when the role will be disabled if the time is in the future",
            "MODIFIER_SOURCE: source name of the subject who last modified this role, e.g. schoolPersonSource", 
            "MODIFIER_SUBJECT_ID: subject id of the subject who last modified this role, e.g. 12345", 
            "CREATOR_SOURCE: source name of the subject who created this role, e.g. schoolPersonSource", 
            "CREATOR_SUBJECT_ID: subject id of the subject who created this role, e.g. 12345", 
            "IS_COMPOSITE_OWNER: T if this is a result of a composite operation (union, intersection, complement), or blank if not", 
            "IS_COMPOSITE_FACTOR: T if this is a member of a composite operation, e.g. one of the grouper being unioned, intersected, or complemented", 
            "CREATOR_ID: member id of the subject who created this role, foreign key to grouper_members", 
            "CREATE_TIME: number of millis since 1970 since this role was created", 
            "MODIFIER_ID: member id of the subject who last modified this role, foreign key to grouper_members", 
            "MODIFY_TIME: number of millis since 1970 since this role was last changed", 
            "HIBERNATE_VERSION_NUMBER: increments by 1 for each update",
            "Context id links together multiple operations into one high level action"),
            "select  "
            + "gg.extension as extension, "
            + "gg.name as name, "
            + "gg.display_extension as display_extension, "
            + "gg.display_name as display_name, "
            + "gg.description as description, "
            + "gs.NAME as parent_stem_name, "
            + "gg.id as role_id, "
            + "gs.ID as parent_stem_id, "
            + "gg.enabled, "
            + "gg.enabled_timestamp, "
            + "gg.disabled_timestamp, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, "
            + "(select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, "
            + "(select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, "
            + "gg.CREATOR_ID, "
            + "gg.CREATE_TIME, "
            + "gg.MODIFIER_ID, "
            + "gg.MODIFY_TIME, "
            + "gg.HIBERNATE_VERSION_NUMBER, gg.context_id  "
            + " from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID and" +
                " type_of_group = 'role' ");
  }

  static void dropViewGrouperGroupsV(DdlVersionBean ddlVersionBean) {

    if (buildingFromScratch(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_dropViewGrouperGroupsV", true)) {
      return;
    }

    // we only need to drop in hsql
    if (ddlVersionBean.isHsql() || ddlVersionBean.isPostgres()) {
    
      GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_groups_v", false);
    }
  }
  static void dropViewGrouperRolesV(DdlVersionBean ddlVersionBean) {

    if (buildingFromScratch(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_dropViewGrouperRolesV", true)) {
      return;
    }

    // we only need to drop in hsql
    if (ddlVersionBean.isHsql() || ddlVersionBean.isPostgres()) {
      GrouperDdlUtils.ddlutilsDropViewIfExists(ddlVersionBean, "grouper_roles_v", false);
    }
  }
  

  static void createViewGrouperGroupsV(DdlVersionBean ddlVersionBean) {

    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }

    if (ddlVersionBean.didWeDoThis("v2_5_createViewGrouperGroupsV", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_groups_v", 
        "Contains one record for each group, with friendly names for some attributes and some more information",
        GrouperUtil.toSet("EXTENSION", 
            "NAME", 
            "DISPLAY_EXTENSION", 
            "DISPLAY_NAME", 
            "DESCRIPTION", 
            "PARENT_STEM_NAME", 
            "TYPE_OF_GROUP", 
            "GROUP_ID", 
            "PARENT_STEM_ID", 
            "ENABLED",
            "ENABLED_TIMESTAMP",
            "DISABLED_TIMESTAMP",
            "MODIFIER_SOURCE", 
            "MODIFIER_SUBJECT_ID", 
            "CREATOR_SOURCE", 
            "CREATOR_SUBJECT_ID", 
            "IS_COMPOSITE_OWNER", 
            "IS_COMPOSITE_FACTOR", 
            "CREATOR_ID", 
            "CREATE_TIME", 
            "MODIFIER_ID", 
            "MODIFY_TIME", 
            "HIBERNATE_VERSION_NUMBER", "CONTEXT_ID"),
        GrouperUtil.toSet("EXTENSION: part of group name not including path information, e.g. theGroup", 
            "NAME: name of the group, e.g. school:stem1:theGroup", 
            "DISPLAY_EXTENSION: name for display of the group, e.g. My school:The stem 1:The group", 
            "DISPLAY_NAME: name for display of the group without any path information, e.g. The group", 
            "DESCRIPTION: contains user entered information about the group e.g. why it exists", 
            "PARENT_STEM_NAME: name of the stem this group is in, e.g. school:stem1", 
            "TYPE_OF_GROUP: group if it is a group, role if it is a role", 
            "GROUP_ID: uuid unique id of the group", 
            "PARENT_STEM_ID: uuid unique id of the stem this group is in", 
            "ENABLED: T or F to indicate if this group is enabled",
            "ENABLED_TIMESTAMP: when the group will be enabled if the time is in the future",
            "DISABLED_TIMESTAMP: when the group will be disabled if the time is in the future",
            "MODIFIER_SOURCE: source name of the subject who last modified this group, e.g. schoolPersonSource", 
            "MODIFIER_SUBJECT_ID: subject id of the subject who last modified this group, e.g. 12345", 
            "CREATOR_SOURCE: source name of the subject who created this group, e.g. schoolPersonSource", 
            "CREATOR_SUBJECT_ID: subject id of the subject who created this group, e.g. 12345", 
            "IS_COMPOSITE_OWNER: T if this is a result of a composite operation (union, intersection, complement), or blank if not", 
            "IS_COMPOSITE_FACTOR: T if this is a member of a composite operation, e.g. one of the grouper being unioned, intersected, or complemeneted", 
            "CREATOR_ID: member id of the subject who created this group, foreign key to grouper_members", 
            "CREATE_TIME: number of millis since 1970 since this group was created", 
            "MODIFIER_ID: member id of the subject who last modified this group, foreign key to grouper_members", 
            "MODIFY_TIME: number of millis since 1970 since this group was last changed", 
            "HIBERNATE_VERSION_NUMBER: increments by 1 for each update",
            "Context id links together multiple operations into one high level action"),
            "select  "
            + "gg.extension as extension, "
            + "gg.name as name, "
            + "gg.display_extension as display_extension, "
            + "gg.display_name as display_name, "
            + "gg.description as description, "
            + "gs.NAME as parent_stem_name, "
            + "gg.type_of_group, "
            + "gg.id as group_id, "
            + "gs.ID as parent_stem_id, "
            + "gg.enabled, "
            + "gg.enabled_timestamp, "
            + "gg.disabled_timestamp, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.MODIFIER_ID) as modifier_subject_id, "
            + "(select gm.SUBJECT_SOURCE from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_source, "
            + "(select gm.SUBJECT_ID from grouper_members gm where gm.ID = gg.CREATOR_ID) as creator_subject_id, "
            + "(select distinct 'T' from grouper_composites gc where gc.OWNER = gg.ID) as is_composite_owner, "
            + "(select distinct 'T' from grouper_composites gc where gc.LEFT_FACTOR = gg.ID or gc.right_factor = gg.id) as is_composite_factor, "
            + "gg.CREATOR_ID, "
            + "gg.CREATE_TIME, "
            + "gg.MODIFIER_ID, "
            + "gg.MODIFY_TIME, "
            + "gg.HIBERNATE_VERSION_NUMBER, gg.context_id  "
            + " from grouper_groups gg, grouper_stems gs where gg.PARENT_STEM = gs.ID ");
  }

  static void createViewGrouperGroupsVplaceholder(DdlVersionBean ddlVersionBean) {
  
    if (!buildingToPreviousVersion(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_createViewGrouperGroupsVplaceholder", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_groups_v", 
        "Contains one record for each group, with friendly names for some attributes and some more information",
        GrouperUtil.toSet("EXTENSION"),
        GrouperUtil.toSet("EXTENSION: part of group name not including path information, e.g. theGroup"),
            "select  "
            + "gg.extension as extension from grouper_groups gg ");
  }

  static void createViewGrouperRolesVplaceholder(DdlVersionBean ddlVersionBean) {
  
    if (!buildingToPreviousVersion(ddlVersionBean)) {
      return;
    }
  
    if (ddlVersionBean.didWeDoThis("v2_5_createViewGrouperRolesVplaceholder", true)) {
      return;
    }
  
    GrouperDdlUtils.ddlutilsCreateOrReplaceView(ddlVersionBean, "grouper_roles_v", 
        "Contains one record for each role, with friendly names for some attributes and some more information",
        GrouperUtil.toSet("EXTENSION"),
        GrouperUtil.toSet("EXTENSION: part of role name not including path information, e.g. theRole"),
            "select  "
            + "gg.extension as extension from grouper_groups gg");
  }

  static void addConfigurationComments(DdlVersionBean ddlVersionBean, Database database) {
  
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_5_addConfigurationComments", true)) {
      return;
    }
    
    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG, 
        "database configuration for config files which allowe database overrides");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_ID, 
          "uuid of record is unique for all records in table and primary key");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_FILE_NAME, 
          "Config file name of the config this record relates to, e.g. grouper.config.properties");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_KEY, 
          "key of the config, not including elConfig");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_VALUE, 
          "Value of the config");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_COMMENT, 
          "documentation of the config value");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_FILE_HIERARCHY, 
          "config file hierarchy, e.g. base, institution, or env");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_ENCRYPTED, 
          "if the value is encrypted");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_SEQUENCE, 
          "if there is more data than fits in the column this is the 0 indexed order");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_CONFIG_VERSION_INDEX, 
          "for built in configs, this is the index that will identify if the database configs should be replaced from the java code");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_LAST_UPDATED, 
          "when this record was inserted or last updated");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, 
        GrouperConfigHibernate.TABLE_GROUPER_CONFIG,
        GrouperConfigHibernate.COLUMN_HIBERNATE_VERSION_NUMBER, 
          "hibernate version for optimistic locking");
    
  }

  static void addGrouperExternalSubjectIdentifierIndex(DdlVersionBean ddlVersionBean, Database database) {
    
    if (buildingToPreviousVersion(ddlVersionBean) && ddlVersionBean.isSmallIndexes()) {
      if (ddlVersionBean.didWeDoThis("v2_5_addGrouperExternalSubjectIdentifierIndexRemoveAdd", true)) {
        return;
      }
      // add an old one since "unique" changed for mysql
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ExternalSubject.TABLE_GROUPER_EXT_SUBJ, 
          "grouper_ext_subj_idfr_idx", false, ExternalSubject.COLUMN_IDENTIFIER+"(255)");
      
      return;
    }
    if (!buildingToThisVersionAtLeast(ddlVersionBean)) {
      return;
    }
    
    if (ddlVersionBean.didWeDoThis("v2_5_addGrouperExternalSubjectIdentifierIndex", true)) {
      return;
    }
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, ExternalSubject.TABLE_GROUPER_EXT_SUBJ, 
        "grouper_ext_subj_idfr_idx", true, ExternalSubject.COLUMN_IDENTIFIER+"(255)");
  }

  public static void addDdlWorkerTableViaScript(boolean runScript) {
  
      if (!runScript) {
        LOG.error("You need to add the grouper_ddl_worker table!");
      }
      try {
        
        String scriptOverrideDatabase = GrouperDdlUtils.findScriptOverrideDatabase();
        
        String resource = "ddl/GrouperDdl_Grouper_createDdlWorker_" + scriptOverrideDatabase + ".sql";
        
        String script = GrouperUtil.readResourceIntoString(resource, false);
        
        GrouperDdlUtils.runScriptIfShouldAndPrintOutput(script, runScript);
  
      } catch (Exception e2) {
  
        GrouperUtil.sleep(3000);
  
        try {
          // if you cant connect to it, its not there
          HibernateSession.bySqlStatic().listSelect(Hib3GrouperDdlWorker.class, "select * from grouper_ddl_worker", null, null);
          return;
        } catch (Exception e) {
          //not found
        }
  
        LOG.error("error creating the grouper_ddl_worker table", e2);
      }
  
    }
  
}
