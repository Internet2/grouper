package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;

import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * last log for this sync that affected this group
 * @author mchyzer
 *
 */
@GcPersistableClass(tableName="grouper_sync", defaultFieldPersist=GcPersist.doPersist)
public class GcGrouperSyncLog implements GcSqlAssignPrimaryKey {

  /**
   * 
   */
  public GcGrouperSyncLog() {
  }

  
  /**
   * uuid of this record in this table
   */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true)
  private String id;

  
  /**
   * uuid of this record in this table
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * uuid of this record in this table
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /**
   * foreign key to grouper_sync_grouping table
   */
  private String grouperSyncGroupingId;


  /**
   * foreign key to grouper_sync_grouping table
   * @return grouping id
   */
  public String getGrouperSyncGroupingId() {
    return this.grouperSyncGroupingId;
  }

  /**
   * foreign key to grouper_sync_grouping table
   * @param grouperSyncGroupingId1
   */
  public void setGrouperSyncGroupingId(String grouperSyncGroupingId1) {
    this.grouperSyncGroupingId = grouperSyncGroupingId1;
  }
  
  /**
   * foreign key to grouper_sync_user table
   */
  private String grouperSyncUserId;


  /**
   * foreign key to grouper_sync_user table
   * @return foreign key
   */
  public String getGrouperSyncUserId() {
    return this.grouperSyncUserId;
  }

  /**
   * foreign key to grouper_sync_user table
   * @param grouperSyncUserId1
   */
  public void setGrouperSyncUserId(String grouperSyncUserId1) {
    this.grouperSyncUserId = grouperSyncUserId1;
  }
  
  /**
   * either the grouper_sync_user_id or the grouper_sync_grouping_id or grouper_sync_job_id (if log for job wide)
   */
  private String grouperSyncOwnerId;


  /**
   * either the grouper_sync_user_id or the grouper_sync_grouping_id or grouper_sync_job_id (if log for job wide)
   * @return owner id
   */
  public String getGrouperSyncOwnerId() {
    return this.grouperSyncOwnerId;
  }

  /**
   * either the grouper_sync_user_id or the grouper_sync_grouping_id or grouper_sync_job_id (if log for job wide)
   * @param grouperSyncOwnerId1
   */
  public void setGrouperSyncOwnerId(String grouperSyncOwnerId1) {
    this.grouperSyncOwnerId = grouperSyncOwnerId1;
  }
  
  /**
   * foreign key to grouper_sync_job table
   */
  private String grouperSyncJobId;


  /**
   * foreign key to grouper_sync_job table
   * @return foreign key
   */
  public String getGrouperSyncJobId() {
    return this.grouperSyncJobId;
  }

  /**
   * foreign key to grouper_sync_job table
   * @param grouperSyncJobId1
   */
  public void setGrouperSyncJobId(String grouperSyncJobId1) {
    this.grouperSyncJobId = grouperSyncJobId1;
  }

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   */
  private String status;


  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   * @return status
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * SUCCESS, ERROR, WARNING, CONFIG_ERROR
   * @param status1
   */
  public void setStatus(String status1) {
    this.status = status1;
  }
  
  /**
   * when the last sync started
   */
  private Timestamp syncTimestamp;


  /**
   * when the last sync started
   * @return timestamp
   */
  public Timestamp getSyncTimestamp() {
    return this.syncTimestamp;
  }

  /**
   * when the last sync started
   * @param syncTimestamp1
   */
  public void setSyncTimestamp(Timestamp syncTimestamp1) {
    this.syncTimestamp = syncTimestamp1;
  }

  /**
   * description of last sync
   */
  private String description;


  /**
   * description of last sync
   * @return description
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * description of last sync
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }
  
  /**
   * how many records were processed the last time this sync ran
   */
  private String recordsProcessed;


  /**
   * how many records were processed the last time this sync ran
   * @return records processed
   */
  public String getRecordsProcessed() {
    return this.recordsProcessed;
  }

  /**
   * how many records were processed the last time this sync ran
   * @param recordsProcessed1
   */
  public void setRecordsProcessed(String recordsProcessed1) {
    this.recordsProcessed = recordsProcessed1;
  }
  
  /**
   * how many records were changed the last time this sync ran
   */
  private String recordsChanged;


  /**
   * how many records were changed the last time this sync ran
   * @return records changed
   */
  public String getRecordsChanged() {
    return this.recordsChanged;
  }

  /**
   * how many records were changed the last time this sync ran
   * @param recordsChanged1
   */
  public void setRecordsChanged(String recordsChanged1) {
    this.recordsChanged = recordsChanged1;
  }
  
  /**
   * how many millis it took to run this job
   */
  private Integer jobTookMillis;


  /**
   * how many millis it took to run this job
   * @return job millis
   */
  public Integer getJobTookMillis() {
    return this.jobTookMillis;
  }

  /**
   * how many millis it took to run this job
   * @param jobTookMillis
   */
  public void setJobTookMillis(Integer jobTookMillis) {
    this.jobTookMillis = jobTookMillis;
  }
  
  /**
   * which server this occurred on
   */
  private String server;


  /**
   * which server this occurred on
   * @return server
   */
  public String getServer() {
    return this.server;
  }

  /**
   * which server this occurred on
   * @param server1
   */
  public void setServer(String server1) {
    this.server = server1;
  }
  
  /**
   * 
   */
  @Override
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    this.id = GrouperClientUtils.uuid();
  }

}
