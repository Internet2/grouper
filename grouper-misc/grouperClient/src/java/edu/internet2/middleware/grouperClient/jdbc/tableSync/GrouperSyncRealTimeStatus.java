/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * map to grouper_sync_real_time_status table
 */
@GcPersistableClass(tableName="grouper_sync_real_time_status", defaultFieldPersist=GcPersist.doPersist)
public class GrouperSyncRealTimeStatus {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperSyncRealTimeStatus grouperSyncRealTimeStatus = new GrouperSyncRealTimeStatus();
    grouperSyncRealTimeStatus.setId(GrouperClientUtils.uuid());
    grouperSyncRealTimeStatus.setJobName("testing");
    grouperSyncRealTimeStatus.setJobState("running");
    grouperSyncRealTimeStatus.setLastRecordsChangedCount(123L);
    grouperSyncRealTimeStatus.setLastUpdated(new Timestamp(System.currentTimeMillis()));
    new GcDbAccess().connectionName("grouper").storeToDatabase(grouperSyncRealTimeStatus);

  }
  
  /**
   * 
   */
  public GrouperSyncRealTimeStatus() {
  }

  /**
   * uuid of this record in this table
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String id;

  
  /**
   * uuid of this record in this table
   * @return the id
   */
  public String getId() {
    return this.id;
  }

  
  /**
   * uuid of this record in this table
   * @param id1 the id to set
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * uuid of this record in this table
   */
  private String jobName;
  
  /**
   * uuid of this record in this table
   * @return the jobName
   */
  public String getJobName() {
    return this.jobName;
  }

  
  /**
   * uuid of this record in this table
   * @param jobName1 the jobName to set
   */
  public void setJobName(String jobName1) {
    this.jobName = jobName1;
  }

  /**
   * running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning
   */
  private String jobState;
  
  
  /**
   * running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning
   * @return the jobState
   */
  public String getJobState() {
    return this.jobState;
  }

  
  /**
   * running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning
   * @param jobState1 the jobState to set
   */
  public void setJobState(String jobState1) {
    this.jobState = jobState1;
  }

  /**
   * either an int of last record checked, or an int of millis since 1970 of last record processed
   */
  private Long lastSyncIndexOrMillis;
  
  
  /**
   * either an int of last record checked, or an int of millis since 1970 of last record processed
   * @return the lastSyncIndexOrMillis
   */
  public Long getLastSyncIndexOrMillis() {
    return this.lastSyncIndexOrMillis;
  }

  
  /**
   * either an int of last record checked, or an int of millis since 1970 of last record processed
   * @param lastSyncIndexOrMillis1 the lastSyncIndexOrMillis to set
   */
  public void setLastSyncIndexOrMillis(Long lastSyncIndexOrMillis1) {
    this.lastSyncIndexOrMillis = lastSyncIndexOrMillis1;
  }

  /**
   * records changed during last run
   */
  private Long lastRecordsChangedCount;
  
  
  /**
   * records changed during last run
   * @return the lastRecordsChangeCount
   */
  public Long getLastRecordsChangedCount() {
    return this.lastRecordsChangedCount;
  }

  
  /**
   * records changed during last run
   * @param lastRecordsChangeCount1 the lastRecordsChangeCount to set
   */
  public void setLastRecordsChangedCount(Long lastRecordsChangeCount1) {
    this.lastRecordsChangedCount = lastRecordsChangeCount1;
  }

  /**
   * records looked at during last run, e.g. for full sync this is total count
   */
  private Long lastRecordsProcessedCount;
  

  
  /**
   * records looked at during last run, e.g. for full sync this is total count
   * @return the lastRecordsProcessedCount
   */
  public Long getLastRecordsProcessedCount() {
    return this.lastRecordsProcessedCount;
  }

  
  /**
   * records looked at during last run, e.g. for full sync this is total count
   * @param lastRecordsProcessedCount1 the lastRecordsProcessedCount to set
   */
  public void setLastRecordsProcessedCount(Long lastRecordsProcessedCount1) {
    this.lastRecordsProcessedCount = lastRecordsProcessedCount1;
  }

  /**
   * how long the last job took to run
   */
  private Long lastJobTookMillis;
  
  
  /**
   * how long the last job took to run
   * @return the lastJobTookMillis
   */
  public Long getLastJobTookMillis() {
    return this.lastJobTookMillis;
  }

  
  /**
   * how long the last job took to run
   * @param lastJobTookMillis1 the lastJobTookMillis to set
   */
  public void setLastJobTookMillis(Long lastJobTookMillis1) {
    this.lastJobTookMillis = lastJobTookMillis1;
  }

  
  /**
   * description of last work done
   */
  private String lastDescription;
  
  /**
   * description of last work done
   * @return the lastDescription
   */
  public String getLastDescription() {
    return this.lastDescription;
  }

  
  /**
   * description of last work done
   * @param lastDescription1 the lastDescription to set
   */
  public void setLastDescription(String lastDescription1) {
    this.lastDescription = lastDescription1;
  }

  /**
   * timestamp of last time work was checked
   */
  private Timestamp lastTimeWorkWasChecked;
  
  
  /**
   * timestamp of last time work was checked
   * @return the lastTimeWorkWasChecked
   */
  public Timestamp getLastTimeWorkWasChecked() {
    return this.lastTimeWorkWasChecked;
  }

  
  /**
   * timestamp of last time work was checked
   * @param lastTimeWorkWasChecked1 the lastTimeWorkWasChecked to set
   */
  public void setLastTimeWorkWasChecked(Timestamp lastTimeWorkWasChecked1) {
    this.lastTimeWorkWasChecked = lastTimeWorkWasChecked1;
  }

  
  /**
   * last time a record was processed
   */
  private Timestamp lastTimeWorkWasDone;
  

  /**
   * last time a record was processed
   * @return the lastTimeWorkWasDone
   */
  public Timestamp getLastTimeWorkWasDone() {
    return this.lastTimeWorkWasDone;
  }

  
  /**
   * last time a record was processed
   * @param lastTimeWorkWasDone1 the lastTimeWorkWasDone to set
   */
  public void setLastTimeWorkWasDone(Timestamp lastTimeWorkWasDone1) {
    this.lastTimeWorkWasDone = lastTimeWorkWasDone1;
  }

  /**
   * when this record was last updated
   */
  private Timestamp lastUpdated;
  
  /**
   * when this record was last updated
   * @return the lastUpdated
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdated;
  }

  
  /**
   * when this record was last updated
   * @param lastUpdated1 the lastUpdated to set
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }


  
}
