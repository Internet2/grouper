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
public class TableSyncCreateRealTimeTable {

  /**
   * 
   */
  public TableSyncCreateRealTimeTable() {
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
    final String tableName = "grouper_sync_real_time_status";
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
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_name", 
            Types.VARCHAR, "50", false, true);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_state", 
            Types.VARCHAR, "50", false, true);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_sync_index_or_millis", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_records_changed_count", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_records_processed_count", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_job_took_millis", 
            Types.INTEGER, "10", false, false);
      
        GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_description", 
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
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_name", "name of job must be unique.  this is the config key");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_state", "running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_sync_index_or_millis_1970", "either an int of last record checked, or an int of millis since 1970 of last record processed");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_description", "description of last work done");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_checked", "timestamp of last time work was checked");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_time_work_was_done", "last time a record was processed");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_updated", "when this record was last updated");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "hibernate_version_number", "incrementing id so record is not updated by two separate places at once");
      
        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_records_changed_count", "records changed during last run");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_records_processed_count", "records looked at during last run");

        GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "last_job_took_millis", "how long the last job took to run");

        GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, 
            "grouper_sync_job_name", true, "job_name");


      }
      
    });

  }
  
}
