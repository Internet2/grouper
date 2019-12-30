/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdl;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 *
 */
public class TableSyncCreateTables {

  /**
   * 
   */
  public TableSyncCreateTables() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    createSyncTablesIfNotExist();
  }

  public static void createSyncTablesIfNotExist() {
    GrouperDdlUtils.changeDatabase(GrouperDdl.V31.getObjectName(), false, false, new DdlUtilsChangeDatabase() {
      
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        createSyncTableIfNotExists(ddlVersionBean);
        createSyncJobTableIfNotExists(ddlVersionBean);
        createSyncGroupingTableIfNotExists(ddlVersionBean);
        createSyncUserTableIfNotExists(ddlVersionBean);
        createSyncLogTableIfNotExists(ddlVersionBean);
  
      }
    });
  }
  
  public static void createSyncLogTableIfNotExists(DdlVersionBean ddlVersionBean) {
    
    final String tableName = "grouper_sync_log";
    
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
      return;
    } catch (Exception e) {
      
    }

    Database database = ddlVersionBean.getDatabase();

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_grouping_id", Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_user_id", Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_owner_id", Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_job_id", Types.VARCHAR, "40", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "10", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "sync_timestamp", Types.TIMESTAMP, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "records_processed", Types.INTEGER, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "records_changed", Types.INTEGER, "10", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_took_millis", Types.INTEGER, "10", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "server", Types.INTEGER, "200", false, false);

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,  tableName, "last log for this sync that affected this group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_grouping_id", "foreign key to grouper_sync_grouping table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_user_id", "foreign key to grouper_sync_user table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_owner_id", "either the grouper_sync_user_id or the grouper_sync_grouping_id or grouper_sync_job_id (if log for job wide)");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_job_id", "foreign key to grouper_sync_job table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "status", "SUCCESS, ERROR, WARNING, CONFIG_ERROR");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_timestamp", "when the last sync started");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "description", "description of last sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "records_processed", "how many records were processed the last time this sync ran");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "records_changed", "how many records were changed the last time this sync ran");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_took_millis", "how many millis it took to run this job");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "server", "which server this occurred on");
    
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_log_sy_gr_idx", false, "grouper_sync_job_id", "grouper_sync_grouping_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_log_sy_gr_idx", false, "grouper_sync_job_id", "grouper_sync_user_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_log_sy_gr_idx", true, "grouper_sync_job_id", "grouper_sync_owner_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_log_sy_ti_idx", false, "grouper_sync_job_id", "sync_timestamp");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_log_syid_ti_idx", false, "grouper_sync_id", "sync_timestamp");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), tableName, "grouper_sync_log_uid_fk", "grouper_sync_user", "grouper_sync_user_id", "id");
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), tableName, "grouper_sync_log_gid_fk", "grouper_sync_grouping", "grouper_sync_grouping_id", "id");
    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), tableName, "grouper_sync_log_sid_fk", "grouper_sync_job", "grouper_sync_job_id", "id");

  }
  
  /**
   * 
   */
  public static void createSyncGroupingTableIfNotExists(DdlVersionBean ddlVersionBean) {
    final String tableName = "grouper_sync_grouping";
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
      return;
    } catch (Exception e) {
      
    }

    Database database = ddlVersionBean.getDatabase();

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
        Types.VARCHAR, "40", true, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_id", 
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_id", 
        Types.VARCHAR, "400", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_name", 
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable", 
        Types.VARCHAR, "1", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target", 
        Types.VARCHAR, "1", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target_start", 
        Types.TIMESTAMP, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target_end", 
        Types.TIMESTAMP, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable_start", 
        Types.TIMESTAMP, "10", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable_end", 
        Types.TIMESTAMP, "10", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "total_count", 
        Types.INTEGER, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
        Types.TIMESTAMP, "10", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_from_id2", 
        Types.VARCHAR, "800", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_from_id3", 
        Types.VARCHAR, "800", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_to_id2", 
        Types.VARCHAR, "800", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_to_id3", 
        Types.VARCHAR, "800", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_time_work_was_done", Types.TIMESTAMP, null, false, false);

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_id", "foreign key back to the sync table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_id", "if this is groups, then this is the idIndex of the group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_name", "if this is groups, then this is the system name of the group");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable", "T if provisionable and F is not");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target", "T if exists in target/destination and F is not.  blank if not sure");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_start", "when this was put in target");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_end", "when this was taken out of target");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_start", "when this grouping started to be provisionable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_end", "when this grouping ended being provisionable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "total_count", "number of records in this grouping");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_from_id2", "for groups this is the group uuid");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_from_id3", "other metadata on groups");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_to_id2", "other metadata on groups");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_to_id3", "other metadata on groups");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_done", "last time a record was processed");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_gr_sync_id_idx", false, "grouper_sync_id", "provisionable");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_gr_group_id_idx", false, "grouping_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_gr_sy_gr_idx", true, "grouper_sync_id", "grouping_id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), tableName, "grouper_sync_gr_id_fk", "grouper_sync", "grouper_sync_id", "id");

  }

  /**
   * 
   */
  public static void createSyncUserTableIfNotExists(DdlVersionBean ddlVersionBean) {
    final String tableName = "grouper_sync_user";
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
      return;
    } catch (Exception e) {
      
    }

    Database database = ddlVersionBean.getDatabase();

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

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "in_target", 
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

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "total_count", 
        Types.INTEGER, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
        Types.TIMESTAMP, "10", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_from_id2", 
        Types.VARCHAR, "800", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_from_id3", 
        Types.VARCHAR, "800", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_to_id2", 
        Types.VARCHAR, "800", false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_to_id3", 
        Types.VARCHAR, "800", false, false);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_time_work_was_done", 
        Types.TIMESTAMP, null, false, false);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_ids", 
        Types.VARCHAR, "4000", false, false);

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,  tableName, "user metadata for sync");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_id", "foreign key to grouper_sync table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "member_id", "foreign key to the members sync table");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "source_id", "subject source id");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "subject_id", "subject id");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "subject_identifier", "netId or eppn or whatever");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target", "T if exists in target/destination and F is not.  blank if not sure");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_start", "when the user was put in the target");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "in_target_end", "when the user was taken out of the target");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable", "T if provisionable and F is not");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_start", "when this grouping started to be provisionable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_end", "when this grouping ended being provisionable");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "total_count", "number of records in this grouping");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "user_from_id2", "for groups this is the group idIndex");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "user_from_id3", "other metadata on groups");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "user_to_id2", "other metadat on groups");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "user_to_id3", "other metadata on groups");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_done", "last time a record was processed");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_ids", "list of group idIndexes or grouping ids (enough that can fit in the field)");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_us_sync_id_idx", false, "grouper_sync_id", "provisionable");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_us_mem_id_idx", false, "member_id", "provisionable");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_us_sm_idx", true, "grouper_sync_id", "member_id");

    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_gr_st_gr_idx", true, "grouper_sync_id", "source_id", "subject_id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), tableName, "grouper_sync_us_id_fk", "grouper_sync", "grouper_sync_id", "id");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), tableName, "grouper_sync_us_memid_fk", "grouper_members", "member_id", "id");

  }

  /**
   * 
   */
  public static void createSyncJobTableIfNotExists(DdlVersionBean ddlVersionBean) {
    final String tableName = "grouper_sync_job";
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
      return;
    } catch (Exception e) {
    }

    Database database = ddlVersionBean.getDatabase();

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
        Types.VARCHAR, "40", true, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_id", 
        Types.VARCHAR, "40", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "sync_type", 
        Types.VARCHAR, "50", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_state", 
        Types.VARCHAR, "50", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_sync_index_or_millis", 
        Types.INTEGER, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_time_work_was_done", 
        Types.TIMESTAMP, null, false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
        Types.TIMESTAMP, "10", false, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_st_ty_idx", true, "grouper_sync_id", "sync_type");

    GrouperDdlUtils.ddlutilsFindOrCreateForeignKey(ddlVersionBean.getDatabase(), tableName, "grouper_sync_job_id_fk", "grouper_sync", "grouper_sync_id", "id");

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, "Status of all jobs for the sync.  one record for full, one for incremental, etc");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_id", "uuid of the job in grouper_sync table");
            
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_type", "type of sync, e.g. for sql sync this is the job subtype");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_state", "running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_sync_index_or_millis", "either an int of last record checked, or an int of millis since 1970 of last record processed");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_done", "last time a record was processed");

    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
  
  }
  /**
   * 
   */
  public static void createSyncTableIfNotExists(DdlVersionBean ddlVersionBean) {
    final String tableName = "grouper_sync";
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
      return;
    } catch (Exception e) {
    }

    Database database = ddlVersionBean.getDatabase();

    Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
    
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
        Types.VARCHAR, "40", true, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "sync_engine", 
        Types.VARCHAR, "50", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisioner_name", 
        Types.VARCHAR, "100", false, true);

    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_count", 
        Types.INTEGER, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_count", 
        Types.INTEGER, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "records_count", 
        Types.INTEGER, "10", false, false);
  
    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
        Types.TIMESTAMP, "10", false, true);
  
    GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
        "grouper_sync_eng_idx", true, "sync_engine", "provisioner_name");

    GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
        tableName, "One record for every provisioner (not different records for full and real time)");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_engine", "e.g. for syncing sql, it sqlTableSync");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisioner_name", "name of provisioner must be unique in combination with sync_engine.  this is the config key generally");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
  
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_count", "if grouping this is the number of groups");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "user_count", "if has users, this is the number of users");
    
    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "records_count", "number of records including users, groups, etc");

  }
  
}
