/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


/**
 *
 */
public class GcGrouperSyncStatus {

  /**
   * 
   */
  public GcGrouperSyncStatus() {
  }

  /**
   * use this for sql engine sync
   */
  public static final String SQL_SYNC_ENGINE = "sqlTableSync";
  
  /**
   * @param statusDatabase 
   * @param statusTable 
   * @param syncEngine 
   * @param jobName 
   * @param syncType 
   * @param jobState 
   * @param lastSyncIndexOrMillis 
   * @param lastRecordsChangedCount 
   * @param lastRecordsProcessedCount 
   * @param lastJobTookMillis 
   * @param lastDescription 
   * @param lastTimeWorkWasChecked 
   * @param lastTimeWorkWasDone 
   * 
   */
  public void statusSave(String statusDatabase, String statusTable, String syncEngine, String jobName, String syncType, String jobState, long lastSyncIndexOrMillis,
      int lastRecordsChangedCount, int lastRecordsProcessedCount, int lastJobTookMillis, String lastDescription, Timestamp lastTimeWorkWasChecked, 
      Timestamp lastTimeWorkWasDone) {
  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", 
    //        Types.VARCHAR, "40", true, true);
    //  
    //    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_engine", 
    //        "e.g. for syncing sql, it sqlTableSync");
    // 
    //    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "job_name", 
    //        "name of job must be unique in combination with sync_engine.  this is the config key generally");
    //
    //    GrouperDdlUtils.ddlutilsColumnComment(ddlVersionBean, tableName, "sync_type", 
    //        "type of sync, e.g. for sql sync this is the job subtype");
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "job_state", 
    //        Types.VARCHAR, "50", false, true);
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_sync_index_or_millis", 
    //        Types.INTEGER, "10", false, false);
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_records_changed_count", 
    //        Types.INTEGER, "10", false, false);
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_records_processed_count", 
    //        Types.INTEGER, "10", false, false);
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_job_took_millis", 
    //        Types.INTEGER, "10", false, false);
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_description", 
    //        Types.VARCHAR, "4000", false, false);
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_time_work_was_checked", 
    //        Types.TIMESTAMP, null, false, false);
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_time_work_was_done", 
    //        Types.TIMESTAMP, null, false, false);
    //  
    //    GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_updated", 
    //        Types.TIMESTAMP, "10", false, true);
    
    if (!StringUtils.isBlank(statusDatabase) && !StringUtils.isBlank(statusTable)) {
      
      String sql = null;
  
      sql = "update " + statusTable
          + "set id = ?, sync_type = ?, job_state = ?, last_sync_index_or_millis = ?, last_records_changed_count = ?, last_records_processed_count = ?, "
          + "last_job_took_millis = ?, last_description = ?, last_time_work_was_checked = ?, last_time_work_was_done = ?, last_updated = ? "
          + "where sync_engine = ?, job_name = ?";
  
      int rowsUpdated = new GcDbAccess().connectionName(statusDatabase).sql(sql).bindVars(new Object[] { GrouperClientUtils.uuid(),
          syncType, jobState, lastSyncIndexOrMillis, lastRecordsChangedCount, lastRecordsProcessedCount, lastJobTookMillis, lastDescription, lastTimeWorkWasChecked, 
          lastTimeWorkWasDone, new Timestamp(System.currentTimeMillis()), syncEngine, jobName}).executeSql();
  
      if (rowsUpdated == 0) {
  
        sql = "insert into " + statusTable
            + " ( id, sync_engine, job_name, sync_type, job_state, last_sync_index_or_millis, last_records_changed_count, last_records_processed_count, "
            + "last_job_took_millis, last_description, last_time_work_was_checked, last_time_work_was_done, last_updated ) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
  
        new GcDbAccess().connectionName(statusDatabase).sql(sql).bindVars(new Object[] { GrouperClientUtils.uuid(), syncEngine, 
            jobName, syncType, jobState, lastSyncIndexOrMillis, lastRecordsChangedCount, lastRecordsProcessedCount, lastJobTookMillis, lastDescription, lastTimeWorkWasChecked, 
            lastTimeWorkWasDone, new Timestamp(System.currentTimeMillis())}).executeSql();
  
      }
    }  
  }
  

}
