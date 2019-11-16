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
import edu.internet2.middleware.grouper.messaging.GrouperMessageHibernate;


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
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_description", "description of last work done");
      
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
