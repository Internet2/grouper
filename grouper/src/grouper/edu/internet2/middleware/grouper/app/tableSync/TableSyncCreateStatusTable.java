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
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 *
 */
public class TableSyncCreateStatusTable {

  /**
   * 
   */
  public TableSyncCreateStatusTable() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    createRealTimeTableIfNotExists();
    createGroupingTableIfNotExists();
  }

  /**
   * 
   */
  public static void createGroupingTableIfNotExists() {
    final String tableName = "grouper_sync_grouping";
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
      return;
    } catch (Exception e) {
      
    }

    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
      
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();
  
        Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
            Types.VARCHAR, "40", true, true);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouper_sync_status_id", 
            Types.VARCHAR, "40", false, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_id", 
            Types.VARCHAR, "400", false, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "grouping_name", 
            Types.VARCHAR, "4000", false, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", 
            Types.INTEGER, "10", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_took_millis", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable", 
            Types.VARCHAR, "1", false, true, "F");
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable_start_millis", 
            Types.INTEGER, "10", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "provisionable_end_millis", 
            Types.INTEGER, "10", false, false);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "total_count", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_grouping_sync", 
            Types.TIMESTAMP, "10", false, true);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_incremental_sync", 
            Types.TIMESTAMP, "10", false, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
            Types.TIMESTAMP, "10", false, true);
      
        GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean,  tableName, "if doing grouping level syncs, this is the last status");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouper_sync_status_id", "foreign key to grouper_sync_status table");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_id", "for groups this is the group uuid");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "grouping_name", "for groups this is the group system name");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "status", "STARTED, RUNNING, SUCCESS, ERROR, WARNING, CONFIG_ERROR");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_took_millis", "how long the grouping sync took");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable", "T if provisionable and F is not");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_start_millis", "millis since 1970 that this grouping started to be provisionable");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "provisionable_end_millis", "millis since 1970 that this grouping ended being provisionable");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_grouping_sync", "when this grouping was last synced");
        
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_incremental_sync", "when a record in this grouping was last synced incrementally");
        
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "total_count", "number of records in this grouping");
        
        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
            "grouper_sync_gr_status_id_idx", true, "grouper_sync_status_id", "provisionable");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
            "grouper_sync_gr_group_id_idx", true, "grouping_id", "provisionable");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
            "grouper_sync_gr_name_idx", true, "grouping_name", "provisionable");


      }
      
    });

  }

  /**
   * 
   */
  public static void createRealTimeTableIfNotExists() {
    final String tableName = "grouper_sync_status";
    try {
      // if you cant connrc to it, its not there
      HibernateSession.bySqlStatic().select(Integer.class, "select count(1) from " + tableName);
      return;
    } catch (Exception e) {
    }

    GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
      
      public void changeDatabase(DdlVersionBean ddlVersionBean) {
        
        Database database = ddlVersionBean.getDatabase();
  
        Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
        
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
            Types.VARCHAR, "40", true, true);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "sync_engine", 
            Types.VARCHAR, "50", false, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_name", 
            Types.VARCHAR, "100", false, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "sync_type", 
            Types.VARCHAR, "50", false, true);

        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_state", 
            Types.VARCHAR, "50", false, true);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_sync_index_or_millis", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "records_changed_count", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "records_processed_count", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_took_millis", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", 
            Types.VARCHAR, "4000", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_time_work_was_checked", 
            Types.TIMESTAMP, null, false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_time_work_was_done", 
            Types.TIMESTAMP, null, false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
            Types.TIMESTAMP, "10", false, true);
      
        GrouperDdlUtils.ddlutilsTableComment(ddlVersionBean, 
            tableName, "If doing real time syncs, this is the last status");
        
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "id", "uuid of this record in this table");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_engine", "e.g. for syncing sql, it sqlTableSync");
        
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_name", "name of job must be unique in combination with sync_engine.  this is the config key generally");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_type", "type of sync, e.g. for sql sync this is the job subtype");
        
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_state", "running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_sync_index_or_millis", "either an int of last record checked, or an int of millis since 1970 of last record processed");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "description", "description of last work done");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_checked", "timestamp of last time work was checked");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_done", "last time a record was processed");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "hibernate_version_number", "incrementing id so record is not updated by two separate places at once");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "records_changed_count", "records changed during last run");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "records_processed_count", "records looked at during last run");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_took_millis", "how long the last job took to run");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
            "grouper_sync_engine_name_idx", true, "sync_engine", "job_name");


      }
      
    });

  }
  
}
