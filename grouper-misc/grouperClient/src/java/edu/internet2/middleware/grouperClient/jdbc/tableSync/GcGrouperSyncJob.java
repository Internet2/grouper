/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.List;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.builder.ToStringBuilder;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * Status of all jobs for the sync.  one record for full, one for incremental, etc
 */
@GcPersistableClass(tableName="grouper_sync_job", defaultFieldPersist=GcPersist.doPersist)
public class GcGrouperSyncJob implements GcSqlAssignPrimaryKey {

  /**
   * delete all data if table is here
   */
  public static void reset() {
    
    try {
      // if its not there forget about it... TODO remove this in 2.5+
      new GcDbAccess().connectionName("grouper").sql("select * from " + GcPersistableHelper.tableName(GcGrouperSyncJob.class) + " where 1 != 1").select(Integer.class);
    } catch (Exception e) {
      return;
    }

    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GcGrouperSyncJob.class)).executeSql();
  }

  /**
   * 
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GcGrouperSyncJob.class);

  /**
   * select grouper sync job by id
   * @param theConnectionName
   * @param id
   * @return the sync job
   */
  public static GcGrouperSyncJob retrieveById(String theConnectionName, String id) {
    theConnectionName = GcGrouperSync.defaultConnectionName(theConnectionName);
    GcGrouperSyncJob gcGrouperSyncJob = new GcDbAccess().connectionName(theConnectionName)
        .sql("select * from grouper_sync_job where id = ?").addBindVar(id).select(GcGrouperSyncJob.class);
    if (gcGrouperSyncJob != null) {
      gcGrouperSyncJob.connectionName = theConnectionName;
    }
    return gcGrouperSyncJob;
  }

  /**
   * 
   * @param connectionName
   */
  public void store() {
    try {
      this.lastUpdated = new Timestamp(System.currentTimeMillis());
      this.connectionName = GcGrouperSync.defaultConnectionName(this.connectionName);
      new GcDbAccess().connectionName(this.connectionName).storeToDatabase(this);
    } catch (RuntimeException e) {
      LOG.info("GrouperSyncJob uuid potential mismatch: " + this.grouperSyncId + ", " + this.syncType, e);
      // maybe a different uuid is there
      GcGrouperSyncJob gcGrouperSyncJob = this.grouperSync.retrieveJobBySyncType(this.syncType);
      if (gcGrouperSyncJob != null) {
        this.id = gcGrouperSyncJob.getId();
        new GcDbAccess().connectionName(connectionName).storeToDatabase(this);
        LOG.warn("GrouperSyncJob uuid mismatch corrected: " + this.grouperSyncId + ", " + this.syncType);
      }
    }
  }
  
  /**
   * 
   * @return sync
   */
  public GcGrouperSync retrieveGrouperSync() {
    if (this.grouperSync == null && this.grouperSyncId != null) {
      this.grouperSync = GcGrouperSync.retrieveById(this.connectionName, this.grouperSyncId);
    }
    return this.grouperSync;
  }

  /**
   * log for this sync job
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private GcGrouperSyncLog grouperSyncLog;
  
  /**
   * 
   * @return sync
   */
  public GcGrouperSyncLog retrieveGrouperSyncLogOrCreate() {
    if (this.grouperSyncLog == null) {
      if (this.id == null) {
        if (this.grouperSyncId == null || GrouperClientUtils.isBlank(this.syncType)) {
          throw new RuntimeException("Cant get a log on a blank job");
        }
        // get an id
        this.store();
      }
      this.grouperSyncLog = GcGrouperSyncLog.retrieveByJobAndOwner(this.connectionName, this.id, this.id);
      if (this.grouperSyncLog == null) {
        this.grouperSyncLog = new GcGrouperSyncLog();
        this.grouperSyncLog.setGrouperSyncJob(this);
        this.grouperSyncLog.setGrouperSyncOwnerId(this.id);
      }
    }
    return this.grouperSyncLog;
  }

  /**
   * 
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private GcGrouperSync grouperSync;
  
  /**
   * 
   * @return gc grouper sync
   */
  public GcGrouperSync getGrouperSync() {
    return this.grouperSync;
  }
  
  /**
   * 
   * @param gcGrouperSync
   */
  public void setGrouperSync(GcGrouperSync gcGrouperSync) {
    this.grouperSync = gcGrouperSync;
    this.grouperSyncId = gcGrouperSync == null ? null : gcGrouperSync.getId();
    this.connectionName = gcGrouperSync == null ? this.connectionName : gcGrouperSync.getConnectionName();
  }
  
  /**
   * connection name or null for default
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private String connectionName;

  /**
   * connection name or null for default
   * @return connection name
   */
  public String getConnectionName() {
    return this.connectionName;
  }

  /**
   * connection name or null for default
   * @param connectionName1
   */
  public void setConnectionName(String connectionName1) {
    this.connectionName = connectionName1;
  }

  /**
   * 
   * @param connectionName
   */
  public void delete() {
    this.connectionName = GcGrouperSync.defaultConnectionName(this.connectionName);
    new GcDbAccess().connectionName(this.connectionName).deleteFromDatabase(this);
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    System.out.println("none");
    
    for (GcGrouperSyncJob theGcGrouperSyncJob : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncJob.class)) {
      System.out.println(theGcGrouperSyncJob.toString());
    }
    
    // foreign key
    GcGrouperSync gcGrouperSync = new GcGrouperSync();
    gcGrouperSync.setSyncEngine("temp");
    gcGrouperSync.setProvisionerName("myJob");
    gcGrouperSync.store();

    
    GcGrouperSyncJob gcGrouperSyncJob = new GcGrouperSyncJob();
    gcGrouperSyncJob.setGrouperSync(gcGrouperSync);
    gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.running);
    gcGrouperSyncJob.setLastSyncIndexOrMillis(135L);
    gcGrouperSyncJob.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis() + 2000));
    gcGrouperSyncJob.setSyncType("testSyncType");
    gcGrouperSyncJob.store();
    
    System.out.println("stored");
    
    gcGrouperSyncJob = gcGrouperSync.retrieveJobBySyncType("testSyncType");
    System.out.println(gcGrouperSyncJob);
    
    gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.notRunning);
    gcGrouperSyncJob.store();

    System.out.println("updated");

    for (GcGrouperSyncJob theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncJob.class)) {
      System.out.println(theGcGrouperSyncStatus.toString());
    }

    gcGrouperSyncJob.delete();
    
    System.out.println("deleted");

    for (GcGrouperSyncJob theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncJob.class)) {
      System.out.println(theGcGrouperSyncStatus.toString());
    }
    
    System.out.println("retrieveOrCreate");
    gcGrouperSyncJob = gcGrouperSync.retrieveJobOrCreateBySyncType("testSyncType");    
    System.out.println(gcGrouperSyncJob);

    System.out.println("retrieve");
    gcGrouperSyncJob = gcGrouperSync.retrieveJobBySyncType("testSyncType");
    System.out.println(gcGrouperSyncJob);

    System.out.println("retrieveOrCreate");
    gcGrouperSyncJob = gcGrouperSync.retrieveJobOrCreateBySyncType("testSyncType");    
    System.out.println(gcGrouperSyncJob);

    System.out.println("deleted");
    gcGrouperSyncJob.delete();
    gcGrouperSync.delete();

    for (GcGrouperSyncJob theGcGrouperSyncStatus : new GcDbAccess().connectionName("grouper").selectList(GcGrouperSyncJob.class)) {
      System.out.println(theGcGrouperSyncStatus.toString());
    }

  }
  
  /**
   * get the job from a list
   * @param gcGrouperSyncJobs
   * @param syncType
   * @return job or null
   */
  public static GcGrouperSyncJob retrieveJobBySyncType(List<GcGrouperSyncJob> gcGrouperSyncJobs, String syncType) {
    for (GcGrouperSyncJob gcGrouperSyncJob : GrouperClientUtils.nonNull(gcGrouperSyncJobs)) {
      if (GrouperClientUtils.equals(syncType, gcGrouperSyncJob.getSyncType())) {
        return gcGrouperSyncJob;
      }
    }
    return null;
  }
  
  /**
   * assign heartbeat and see if other jobs are pending or running
   * @param provisionerName
   * @param isLargeJob is if this is a  big job and has precendence
   * @return false if should stop and true if should run
   */
  public boolean assignHeartbeatAndCheckForPendingJobs(boolean isLargeJob) {
    
    List<GcGrouperSyncJob> allGcGrouperSyncJobs = this.getGrouperSync().retrieveAllJobs();

    GcGrouperSyncJob gcGrouperSyncJob = GcGrouperSyncJob.retrieveJobBySyncType(allGcGrouperSyncJobs, syncType);
    
    // if doesnt exist, 
    if (gcGrouperSyncJob == null) {
      throw new RuntimeException("Why is this job not found????");
    }
    
    gcGrouperSyncJob.setHeartbeat(new Timestamp(System.currentTimeMillis()));

    // should already be running but just in case
    gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.running);

    gcGrouperSyncJob.store();
    
    for (GcGrouperSyncJob currentGrouperSyncJob : GrouperClientUtils.nonNull(allGcGrouperSyncJobs)) {
      if (GrouperClientUtils.equals(currentGrouperSyncJob.getSyncType(), syncType)) {
        continue;
      }
      
      //if the heartbeat is bad dontw worry about it
      if (currentGrouperSyncJob.getHeartbeat() == null || System.currentTimeMillis() - currentGrouperSyncJob.getHeartbeat().getTime() > 90000) {
        // dont worry about it
        continue;
      }
      
      if (GcGrouperSyncJobState.running == currentGrouperSyncJob.getJobState() && !isLargeJob) {
        return false;
      }
      
      // dont run if we are a small job
      if (GcGrouperSyncJobState.pending == currentGrouperSyncJob.getJobState() && !isLargeJob) {
        return false;
      }
    }

    // large jobs always keep going
    return true;
  }

  /**
   * assign heartbeat and end job
   */
  public void assignHeartbeatAndEndJob() {
    
    this.setHeartbeat(new Timestamp(System.currentTimeMillis()));

    // should already be running but just in case
    this.setJobState(GcGrouperSyncJobState.notRunning);

    this.store();
    
  }

  /**
   * 
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("id", this.id)
        .append("grouperSyncId", this.grouperSyncId)
        .append("syncType", this.syncType)
        .append("lastUpdated", this.lastUpdated)
        .append("jobState", this.jobStateDb)
        .append("lastSyncIndexOrMillis", this.lastSyncIndexOrMillis)
        .append("lastTimeWorkWasDone", this.lastTimeWorkWasDone).build();
  }

  /**
   * heartbeat updated every minute
   */
  private Timestamp heartbeat;
  
  
  
  /**
   * heartbeat updated every minute
   * @return heartbeat
   */
  public Timestamp getHeartbeat() {
    return this.heartbeat;
  }

  /**
   * heatbeat updated every minute
   * @param heartbeat1
   */
  public void setHeartbeat(Timestamp heartbeat1) {
    this.heartbeat = heartbeat1;
  }

  /**
   * 
   */
  public GcGrouperSyncJob() {
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
   * type of sync, e.g. for sql sync this is the job subtype
   */
  private String syncType;
  
  /**
   * type of sync, e.g. for sql sync this is the job subtype
   * @return sync type
   */
  public String getSyncType() {
    return this.syncType;
  }

  /**
   * type of sync, e.g. for sql sync this is the job subtype
   * @param syncType
   */
  public void setSyncType(String syncType) {
    this.syncType = syncType;
  }

  /**
   * uuid of the job in grouper_sync
   */
  private String grouperSyncId;
  
  /**
   * uuid of the job in grouper_sync
   * @return uuid of the job in grouper_sync
   */ 
  public String getGrouperSyncId() {
    return this.grouperSyncId;
  }

  /**
   * uuid of the job in grouper_sync
   * @param grouperSyncId1
   */
  public void setGrouperSyncId(String grouperSyncId1) {
    this.grouperSyncId = grouperSyncId1;
    if (this.grouperSync == null || !GrouperClientUtils.equals(this.grouperSync.getId(), grouperSyncId1)) {
      this.grouperSync = null;
    }
  }
  
  /**
   * running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning
   */
  @GcPersistableField(columnName="job_state")
  private String jobStateDb;
  
  
  /**
   * running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning
   * @return the jobState
   */
  public String getJobStateDb() {
    return this.jobStateDb;
  }

  /**
   * 
   * @return the state or null if not there
   */
  public GcGrouperSyncJobState getJobState() {
    return GcGrouperSyncJobState.valueOfIgnoreCase(this.jobStateDb);
  }
  
  /**
   * 
   * @param gcGrouperSyncJobState
   */
  public void setJobState(GcGrouperSyncJobState gcGrouperSyncJobState) {
    this.jobStateDb = gcGrouperSyncJobState == null ? null : gcGrouperSyncJobState.name();
  }
  
  /**
   * running, waitingForAnotherJobToFinish (if waiting for another job to finish), notRunning
   * @param jobState1 the jobState to set
   */
  public void setJobStateDb(String jobState1) {
    this.jobStateDb = jobState1;
  }

  /**
   * when last record processed if timestamp and not integer
   */
  private Timestamp lastSyncTimestamp;
  
  /**
   * when last record processed if timestamp and not integer
   * @return when processed
   */
  public Timestamp getLastSyncTimestamp() {
    return this.lastSyncTimestamp;
  }

  /**
   * when last record processed if timestamp and not integer
   * @param lastSyncTimestamp1
   */
  public void setLastSyncTimestamp(Timestamp lastSyncTimestamp1) {
    this.lastSyncTimestamp = lastSyncTimestamp1;
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

  /**
   * 
   */
  @Override
  public void gcSqlAssignNewPrimaryKeyForInsert() {
    this.id = GrouperClientUtils.uuid();
  }

}
