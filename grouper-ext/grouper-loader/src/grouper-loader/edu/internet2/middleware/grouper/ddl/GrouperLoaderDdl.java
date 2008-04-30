/*
 * @author mchyzer
 * $Id: GrouperLoaderDdl.java,v 1.1 2008-04-30 08:03:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.loader.util.GrouperDdlUtils;


/**
 * version for ddl of the grouper loader specific tables
 */
public enum GrouperLoaderDdl implements DdlVersionable {

  /** first version of grouper */
  V1 {
    /**
     * add the table grouploader_log for logging and detect and add columns
     * @see edu.internet2.middleware.grouper.ddl.GrouperLoaderDdl#updateVersionFromPrevious(org.apache.ddlutils.model.Database)
     */
    @Override
    public void updateVersionFromPrevious(Database database) {
      
      //see if the grouper_ext_loader_log table is there
      Table grouploaderLogTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database,"grouploader_log", 
          "log table with a row for each grouper loader job run");
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "id", "uuid of this log record", 
          Types.VARCHAR, "128", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_name", 
          "Could be group name (friendly) or just config name", Types.VARCHAR, "512", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "status", 
          "STARTED, SUCCESS, ERROR", Types.VARCHAR, "10", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "started_time", 
          "When the job was started", Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "ended_time", 
          "When the job ended (might be blank if daemon died)", Types.TIMESTAMP, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis", 
          "Milliseconds this process took", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis_get_data", 
          "Milliseconds this process took to get the data from the source", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "millis_load_data", 
          "Milliseconds this process took to load the data to grouper", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_type", 
          "GrouperLoaderJobType enum value", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_type", 
          "GrouperLoaderJobscheduleType enum value", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_description", 
          "More information about the job", Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_message", 
          "Could be a status or error message or stack", Types.VARCHAR, "4000", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "host", 
          "Host that this job ran on", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "group_uuid", 
          "If this job involves one group, this is uuid", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_quartz_cron", 
          "Quartz cron string for this col", Types.VARCHAR, "128", false, false);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_interval_seconds", 
          "How many seconds this is supposed to wait between runs", Types.INTEGER, null, false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(grouploaderLogTable, "job_schedule_priority", 
          "Priority of this job (5 is unprioritized, higher the better)", Types.INTEGER, null, false, false);
    }
  };

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getVersion()
   */
  public int getVersion() {
    return GrouperDdlUtils.versionIntFromEnum(this);
  }
  
  /**
   * keep the current version here, increment as things change
   * @return the current version
   */
  public static int currentVersion() {
    return 1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getObjectName()
   */
  public String getObjectName() {
    return GrouperDdlUtils.objectName(this);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#getDefaultTablePattern()
   */
  public String getDefaultTablePattern() {
    return "GROUPLOADER%";
  }

  /**
   * @see edu.internet2.middleware.grouper.ddl.DdlVersionable#updateVersionFromPrevious(org.apache.ddlutils.model.Database)
   */
  public abstract void updateVersionFromPrevious(Database database);  
}
