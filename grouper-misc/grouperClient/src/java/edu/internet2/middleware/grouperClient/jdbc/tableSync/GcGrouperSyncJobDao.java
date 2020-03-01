package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * dao for jobs
 * @author mchyzer
 *
 */
public class GcGrouperSyncJobDao {

  /**
   * keep an internal cache of groups by sync type
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncJob> internalCacheSyncJobs = new HashMap<String, GcGrouperSyncJob>();

  /**
   * keep an internal cache of jobs by uuid
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncJob> internalCacheSyncJobsById = new HashMap<String, GcGrouperSyncJob>();

  public GcGrouperSyncJobDao() {
  }

  /**
   * select grouper sync jobs by job id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param syncType
   * @return the job
   */
  public GcGrouperSyncJob jobCreateBySyncType(String syncType) {
    GcGrouperSyncJob gcGrouperSyncJob = new GcGrouperSyncJob();
    gcGrouperSyncJob.setGrouperSync(this.getGcGrouperSync());
    gcGrouperSyncJob.setSyncType(syncType);
    this.internal_jobCacheAdd(gcGrouperSyncJob);
    return gcGrouperSyncJob;
  }

  /**
   * delete batch
   * @param gcGrouperSyncJobs
   * @param deleteLogs true if should delete logs
   * @return rows deleted (jobs and logs)
   */
  public int jobDelete(Collection<GcGrouperSyncJob> gcGrouperSyncJobs, boolean deleteLogs) {
    int count = 0;
  
    if (GrouperClientUtils.length(gcGrouperSyncJobs) == 0) {
      return 0;
    }
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    Set<String> logJobIds = new HashSet<String>();
    
    for (GcGrouperSyncJob gcGrouperSyncJob : gcGrouperSyncJobs) {
      
      List<Object> currentBindVarRow = new ArrayList<Object>();
      currentBindVarRow.add(gcGrouperSyncJob.getId());
      batchBindVars.add(currentBindVarRow);
      
      logJobIds.add(gcGrouperSyncJob.getId());
      this.internal_jobCacheDelete(gcGrouperSyncJob);
    }
  
    String connectionName = gcGrouperSyncJobs.iterator().next().getConnectionName();
    
    count += this.getGcGrouperSync().getGcGrouperSyncLogDao().internal_logDeleteBatchByOwnerIds(logJobIds);
    
    // TODO delete memberships? and membership log
  
    int[] rowDeleteCounts = new GcDbAccess().connectionName(connectionName).sql("delete from grouper_sync_job where id = ?")
      .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
  
    for (int rowDeleteCount : rowDeleteCounts) {
      count += rowDeleteCount;
    }
  
    return count;
    
  
  }

  /**
   * delete sync job
   * @param gcGrouperSyncJob
   * @return rows deleted (jobs and logs)
   */
  public int jobDelete(GcGrouperSyncJob gcGrouperSyncJob, boolean deleteLogs ) {
    
    if (gcGrouperSyncJob == null) {
      return 0;
    }
    
    this.internal_jobCacheDelete(gcGrouperSyncJob);

    int count = 0;
    
    if (deleteLogs) {
      count += this.getGcGrouperSync().getGcGrouperSyncLogDao().logDeleteByOwnerId(gcGrouperSyncJob.getId());
    }

    int rowDeleteCount = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql("delete from grouper_sync_job where id = ?")
      .bindVars(gcGrouperSyncJob.getId()).executeSql();
    
    count += rowDeleteCount;
      
    return count;

  }

  /**
   * delete all jobx for a sync
   * @param deleteLogs true if should delete logs associated with this job
   * @return the syncs
   */
  public int jobDeleteAll(boolean deleteLogs) {
    this.internalCacheSyncJobs.clear();
    this.internalCacheSyncJobsById.clear();
    
    int rowDeleteCount = 0;
    
    if (deleteLogs) {
      rowDeleteCount += new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_log where grouper_sync_owner_id in ( select id from grouper_sync_job gsj where gsj.grouper_sync_id = ?)")
        .bindVars(this.getGcGrouperSync().getId()).executeSql();
    }
    
    rowDeleteCount += new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_job where grouper_sync_id = ?)")
        .bindVars(this.getGcGrouperSync().getId()).executeSql();
    
    return rowDeleteCount;
  }

  /**
   * select grouper sync job by sync id and sync type
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncJob> jobRetrieveAll() {
    if (!this.jobRetrievedAllObjectsFromDb) {
      for (GcGrouperSyncJob gcGrouperSyncJob : this.internal_jobRetrieveFromDbAll()) {
        this.internal_jobCacheAdd(gcGrouperSyncJob);
      }
      this.jobRetrievedAllObjectsFromDb = true;
    }
    return new ArrayList<GcGrouperSyncJob>(this.internalCacheSyncJobs.values());
  }

  /**
   * select grouper sync job by sync type
   * @param connectionName
   * @param syncType
   * @return the job
   */
  public GcGrouperSyncJob jobRetrieveBySyncType(String syncType) {
    GcGrouperSyncJob gcGrouperSyncJob = this.internalCacheSyncJobs.get(syncType);
    if (gcGrouperSyncJob == null) {
      gcGrouperSyncJob = internal_jobRetrieveFromDbBySyncType(syncType);
    }
    return gcGrouperSyncJob;
  }

  /**
   * select grouper sync job by job id
   * @param gcGrouperSyncJobId
   * @return the job
   */
  public GcGrouperSyncJob jobRetrieveById(String gcGrouperSyncJobId) {
    GcGrouperSyncJob gcGrouperSyncJob = this.internalCacheSyncJobsById.get(gcGrouperSyncJobId);
    if (gcGrouperSyncJob == null) {
      gcGrouperSyncJob = internal_jobRetrieveFromDbById(gcGrouperSyncJobId);
    }
    return gcGrouperSyncJob;
  }

  /**
   * select grouper sync group by group id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param syncType
   * @return the group
   */
  public GcGrouperSyncJob jobRetrieveOrCreateBySyncType(String syncType) {
    GcGrouperSyncJob gcGrouperSyncJob = this.jobRetrieveBySyncType(syncType);
    if (gcGrouperSyncJob == null) {
      gcGrouperSyncJob = internal_jobRetrieveFromDbBySyncType(syncType);
    }
    if (gcGrouperSyncJob == null) {
      gcGrouperSyncJob = this.jobCreateBySyncType(syncType);
    }
    return gcGrouperSyncJob;
  }

  /**
   * 
   * @param gcGrouperSyncJob
   * @return log
   */
  public GcGrouperSyncLog jobCreateLog(GcGrouperSyncJob gcGrouperSyncJob) {
    return this.gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId(gcGrouperSyncJob.getId());
  }

  /**
   * 
   * @param gcGrouperSyncJob
   */
  private void internal_jobCacheAdd(GcGrouperSyncJob gcGrouperSyncJob) {
    if (gcGrouperSyncJob.getSyncType() != null) {
      this.internalCacheSyncJobs.put(gcGrouperSyncJob.getSyncType(), gcGrouperSyncJob);
    }
    if (gcGrouperSyncJob.getId() != null) { 
      this.internalCacheSyncJobsById.put(gcGrouperSyncJob.getId(), gcGrouperSyncJob);
    }
  }

  /**
   * 
   * @param gcGrouperSyncJob
   */
  public void internal_jobCacheDelete(GcGrouperSyncJob gcGrouperSyncJob) {
    if (gcGrouperSyncJob.getSyncType() != null) {
      this.internalCacheSyncJobs.remove(gcGrouperSyncJob.getSyncType());
    }
    if (gcGrouperSyncJob.getId() != null) {
      this.internalCacheSyncJobsById.remove(gcGrouperSyncJob.getId());
    }
    
  }

  /**
   * select grouper sync group by sync id and group id
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncJob> internal_jobRetrieveFromDbAll() {
    
    // clear the cache
    this.internalCacheSyncJobs.clear();
    this.internalCacheSyncJobsById.clear();
    
    List<GcGrouperSyncJob> gcGrouperSyncJobList = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_job where grouper_sync_id = ?").addBindVar(this.getGcGrouperSync().getId()).selectList(GcGrouperSyncJob.class);
    
    for (GcGrouperSyncJob gcGrouperSyncJob : gcGrouperSyncJobList) {
      gcGrouperSyncJob.setGrouperSync(this.getGcGrouperSync());
      this.internal_jobCacheAdd(gcGrouperSyncJob);
  
    }
    return gcGrouperSyncJobList;
  }

  /**
   * select grouper sync job by sync type
   * @param connectionName
   * @param syncType
   * @return the group
   */
  public GcGrouperSyncJob internal_jobRetrieveFromDbBySyncType(String syncType) {
    
    GcGrouperSyncJob gcGrouperSyncJob = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_job where grouper_sync_id = ? and sync_type = ?")
          .addBindVar(this.getGcGrouperSync().getId()).addBindVar(syncType).select(GcGrouperSyncJob.class);
    if (gcGrouperSyncJob != null) {
      gcGrouperSyncJob.setGrouperSync(this.getGcGrouperSync());
      this.internal_jobCacheAdd(gcGrouperSyncJob);
    }
    return gcGrouperSyncJob;
    
  }

  /**
   * select grouper sync job by gcGrouperSyncJobId id
   * @param gcGrouperSyncJobId
   * @return the gcGrouperSyncJob
   */
  public GcGrouperSyncJob internal_jobRetrieveFromDbById(String gcGrouperSyncJobId) {
    
    GcGrouperSyncJob gcGrouperSyncJob = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_job where id = ?")
          .addBindVar(gcGrouperSyncJobId).select(GcGrouperSyncJob.class);
    if (gcGrouperSyncJob != null) {
      gcGrouperSyncJob.setGrouperSync(this.getGcGrouperSync());
      this.internal_jobCacheAdd(gcGrouperSyncJob);
    }
    return gcGrouperSyncJob;
    
  }

  /**
   * 
   */
  private GcGrouperSync gcGrouperSync;
  /**
   * if all objects have been retrieved from db
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private boolean jobRetrievedAllObjectsFromDb = false;
  
  
  
  /**
   * 
   * @return
   */
  public GcGrouperSync getGcGrouperSync() {
    return gcGrouperSync;
  }

  /**
   * 
   * @param gcGrouperSync
   */
  public void setGcGrouperSync(GcGrouperSync gcGrouperSync) {
    this.gcGrouperSync = gcGrouperSync;
  }

  /**
   * 
   * @return number of groups stored
   */
  public int internal_jobStore() {
    return this.internal_jobStore(this.internalCacheSyncJobs.values());
  }
  
  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncJobs
   * @return number of changes
   */
  public int internal_jobStore(Collection<GcGrouperSyncJob> gcGrouperSyncJobs) {
  
    if (GrouperClientUtils.length(gcGrouperSyncJobs) == 0) {
      return 0;
    }
  
    int batchSize = this.getGcGrouperSync().batchSize();
  
    List<GcGrouperSyncJob> gcGrouperSyncJobsList = new ArrayList<GcGrouperSyncJob>(gcGrouperSyncJobs);
    
    for (GcGrouperSyncJob gcGrouperSyncJob : GrouperClientUtils.nonNull(gcGrouperSyncJobs)) {
      gcGrouperSyncJob.storePrepare();
    }
  
    int changes = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeBatchToDatabase(gcGrouperSyncJobsList, batchSize);
    
    for (GcGrouperSyncJob gcGrouperSyncJob : GrouperClientUtils.nonNull(gcGrouperSyncJobs)) {
      this.internal_jobCacheAdd(gcGrouperSyncJob);
    }
    return changes;
  }

  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncJobs
   */
  public void internal_jobStore(GcGrouperSyncJob gcGrouperSyncJob) {
  
    gcGrouperSyncJob.storePrepare();
  
    new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeToDatabase(gcGrouperSyncJob);
  
  }

}
