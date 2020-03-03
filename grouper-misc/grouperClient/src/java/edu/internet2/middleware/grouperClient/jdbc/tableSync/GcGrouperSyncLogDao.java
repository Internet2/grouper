package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GcGrouperSyncLogDao {

  /**
   * keep an internal cache of logs by owner id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncLog> internalCacheSyncLogs = new HashMap<String, GcGrouperSyncLog>();
  /**
   * keep an internal cache of logs by uuid
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncLog> internalCacheSyncLogsById = new HashMap<String, GcGrouperSyncLog>();

  public GcGrouperSyncLogDao() {
  }

  /**
   * select grouper sync log by owner id.  note this does not actually store the object
   * @param connectionName
   * @param ownerId
   * @return the group
   */
  public GcGrouperSyncLog logCreateByOwnerId(String ownerId) {
    GcGrouperSyncLog gcGrouperSyncLog = new GcGrouperSyncLog();
    gcGrouperSyncLog.setGrouperSync(this.getGcGrouperSync());
    gcGrouperSyncLog.setGrouperSyncOwnerId(ownerId);
    this.internal_logCacheAdd(gcGrouperSyncLog);


    return gcGrouperSyncLog;
  }

  /**
   * delete old logs for this grouper sync
   * @param numberOfMillisOldToDelete
   * @return number of records deleted
   */
  public int logDeleteOldLogs(Integer numberOfMillisOldToDelete) {
    
    // default to one week
    numberOfMillisOldToDelete = GrouperClientUtils.defaultIfNull(numberOfMillisOldToDelete, 1000*60*60*24*7);
    List<Timestamp> theTimestamps = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select last_updated from grouper_sync_log where grouper_sync_id = ? and last_updated < ?")
        .addBindVar(this.getGcGrouperSync().getId()).addBindVar(new Timestamp(System.currentTimeMillis()-numberOfMillisOldToDelete))
        .selectList(Timestamp.class);

    if (theTimestamps.size() == 0) {
      return 0;
    }
    Collections.sort(theTimestamps);
    Collections.reverse(theTimestamps);
    // sometimes we have trouble deleting a lot of records at once, so delete them in batches of 5000
    int batchSize = 5000;
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(theTimestamps, batchSize);
    int count = 0;
    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      List<Timestamp> theBatch = GrouperClientUtils.batchList(theTimestamps, batchSize, batchIndex);
      
      // the last one is the latest
      Timestamp earliestTimestamp = theBatch.get(theBatch.size()-1);
      
      int currentCount = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql("delete from grouper_sync_log where grouper_sync_id = ? and last_updated < ?")
          .addBindVar(this.getGcGrouperSync().getId()).addBindVar(earliestTimestamp)
          .executeSql();
      count += currentCount;
    }
    return count;
  }

  /**
   * delete by owner
   * @param ownerId
   * @return rows deleted (logs)
   */
  public int logDeleteByOwnerId(String ownerId) {

    this.internal_logCacheDeleteByOwnerId(ownerId);
    return new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_log where grouper_sync_owner_id = ?")
      .bindVars(ownerId).executeSql();
  }
  
  /**
   * delete by sync group id from membership
   * @param syncGroupId
   * @return rows deleted (logs)
   */
  public int logDeleteByMembershipSyncGroupId(String syncGroupId) {

    this.internal_logCacheDeleteByMembershipSyncGroupId(syncGroupId);
    return new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_log gsl where grouper_sync_owner_id in ( select gsm.id from grouper_sync_membership gsm where gsm.grouper_sync_group_id = ? )")
      .bindVars(syncGroupId).executeSql();
  }
  
  /**
   * delete by sync member id from membership
   * @param syncMemberId
   * @return rows deleted (logs)
   */
  public int logDeleteByMembershipSyncMemberId(String syncMemberId) {

    this.internal_logCacheDeleteByMembershipSyncMemberId(syncMemberId);
    return new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_log gsl where grouper_sync_owner_id in ( select gsm.id from grouper_sync_membership gsm where gsm.grouper_sync_member_id = ? )")
      .bindVars(syncMemberId).executeSql();
  }
  
  /**
   * delete stuff with batches by owner id (e.g. foreign key to delete when deleted those)
   * @param ids
   * @return the number of records deleted
   */
  public int internal_logDeleteBatchByOwnerIds(Collection<String> ownerIds) {
  
    int count = 0;
    if (GrouperClientUtils.length(ownerIds) > 0) {

      // remove from cache
      this.internal_logCacheDeleteByOwnerIds(new HashSet<String>(ownerIds));

      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
          
      for (String ownerId : ownerIds) {
        List<Object> currentBindVarRow = new ArrayList<Object>();
        currentBindVarRow.add(ownerId);
        batchBindVars.add(currentBindVarRow);
        
      }
  
      int[] rowDeleteCounts = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
          "delete from grouper_sync_log where grouper_sync_owner_id = ?")
        .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
      
      for (int rowDeleteCount : rowDeleteCounts) {
        count += rowDeleteCount;
      }
      
    }
    return count;
  }

  /**
   * delete batch
   * @param gcGrouperSyncLogs
   * @return rows deleted (logs)
   */
  public int logDelete(Collection<GcGrouperSyncLog> gcGrouperSyncLogs) {
    
    int count = 0;
  
    if (GrouperClientUtils.length(gcGrouperSyncLogs) == 0) {
      return 0;
    }
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    for (GcGrouperSyncLog gcGrouperSyncLog : gcGrouperSyncLogs) {
      
      List<Object> currentBindVarRow = new ArrayList<Object>();
      currentBindVarRow.add(gcGrouperSyncLog.getId());
      batchBindVars.add(currentBindVarRow);
      
      this.internal_logCacheDelete(gcGrouperSyncLog);
    }
  
    String connectionName = gcGrouperSyncLogs.iterator().next().getConnectionName();
    
    int[] rowDeleteCounts = new GcDbAccess().connectionName(connectionName).sql("delete from grouper_sync_log where id = ?")
      .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
  
    for (int rowDeleteCount : rowDeleteCounts) {
      count += rowDeleteCount;
    }
  
    return count;
    
  
  }

  /**
   * delete sync log
   * @param gcGrouperSyncLog
   * @return rows deleted
   */
  public int logDelete(GcGrouperSyncLog gcGrouperSyncLog) {
    
    if (gcGrouperSyncLog == null) {
      return 0;
    }
    
    this.internal_logCacheDelete(gcGrouperSyncLog);

    int count = 0;

    int rowDeleteCount = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql("delete from grouper_sync_log where id = ?")
      .bindVars(gcGrouperSyncLog.getId()).executeSql();
    
    count += rowDeleteCount;
      
    return count;

  }

  /**
   * delete all log for a sync
   * @param deleteMemberships true if delete memberships and logs for memberships too
   * @return the syncs
   */
  public int logDeleteAll() {
    this.internalCacheSyncLogs.clear();
    this.internalCacheSyncLogsById.clear();
    
    int rowDeleteCount = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_log where grouper_sync_id = ?)")
        .bindVars(this.getGcGrouperSync().getId()).executeSql();
    
    return rowDeleteCount;
  }

  /**
   * select grouper sync log by sync id
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncLog> logRetrieveAll() {
    if (!this.logRetrievedAllObjectsFromDb) {
      for (GcGrouperSyncLog gcGrouperSyncLog : this.internal_logRetrieveFromDbAll()) {
        this.internal_logCacheAdd(gcGrouperSyncLog);
      }
      this.logRetrievedAllObjectsFromDb = true;
    }
    return new ArrayList<GcGrouperSyncLog>(this.internalCacheSyncLogs.values());
  }

  /**
   * select grouper sync log by id
   * @param gcGrouperSyncLogId
   * @return the group
   */
  public GcGrouperSyncLog logRetrieveById(String gcGrouperSyncLogId) {
    GcGrouperSyncLog gcGrouperSyncLog = this.internalCacheSyncLogsById.get(gcGrouperSyncLogId);
    if (gcGrouperSyncLog == null) {
      gcGrouperSyncLog = internal_logRetrieveFromDbById(gcGrouperSyncLogId);
    }
    return gcGrouperSyncLog;
  }

  /**
   * 
   * @param gcGrouperSyncLog
   */
  private void internal_logCacheAdd(GcGrouperSyncLog gcGrouperSyncLog) {
    if (gcGrouperSyncLog.getGrouperSyncOwnerId() != null) {
      this.internalCacheSyncLogs.put(gcGrouperSyncLog.getGrouperSyncOwnerId(), gcGrouperSyncLog);
    }
    if (gcGrouperSyncLog.getId() != null) { 
      this.internalCacheSyncLogsById.put(gcGrouperSyncLog.getId(), gcGrouperSyncLog);
    }
  }

  /**
   * 
   * @param gcGrouperSyncLog
   */
  public void internal_logCacheDelete(GcGrouperSyncLog gcGrouperSyncLog) {
    if (gcGrouperSyncLog.getGrouperSyncOwnerId() != null) {
      this.internalCacheSyncLogs.remove(gcGrouperSyncLog.getGrouperSyncOwnerId());
    }
    if (gcGrouperSyncLog.getId() != null) {
      this.internalCacheSyncLogsById.remove(gcGrouperSyncLog.getId());
    }
    
  }

  /**
   * delete from cache by owner id
   * @param gcGrouperSyncLog
   */
  public void internal_logCacheDeleteByOwnerId(String ownerId) {

    for (GcGrouperSyncLog gcGrouperSyncLog : new HashSet<GcGrouperSyncLog>(internalCacheSyncLogs.values())) {
      if (GrouperClientUtils.equals(ownerId, gcGrouperSyncLog.getGrouperSyncOwnerId())) {
        internal_logCacheDelete(gcGrouperSyncLog);
      }
    }
        
  }

  /**
   * delete from cache by membership sync group id
   * @param gcGrouperSyncLog
   */
  public void internal_logCacheDeleteByMembershipSyncGroupId(String syncGroupId) {

    for (GcGrouperSyncLog gcGrouperSyncLog : new HashSet<GcGrouperSyncLog>(internalCacheSyncLogs.values())) {
      GcGrouperSyncMembership gcGrouperSyncMembership = this.gcGrouperSync
          .getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromCacheById(gcGrouperSyncLog.getGrouperSyncOwnerId());
      if (gcGrouperSyncMembership != null && GrouperClientUtils.equals(syncGroupId, gcGrouperSyncMembership.getGrouperSyncGroupId())) {
        internal_logCacheDelete(gcGrouperSyncLog);
      }
    }
        
  }


  /**
   * delete from cache by membership sync member id
   * @param gcGrouperSyncLog
   */
  public void internal_logCacheDeleteByMembershipSyncMemberId(String syncMemberId) {

    for (GcGrouperSyncLog gcGrouperSyncLog : new HashSet<GcGrouperSyncLog>(internalCacheSyncLogs.values())) {
      GcGrouperSyncMembership gcGrouperSyncMembership = this.gcGrouperSync
          .getGcGrouperSyncMembershipDao().internal_membershipRetrieveFromCacheById(gcGrouperSyncLog.getGrouperSyncOwnerId());
      if (gcGrouperSyncMembership != null && GrouperClientUtils.equals(syncMemberId, gcGrouperSyncMembership.getGrouperSyncMemberId())) {
        internal_logCacheDelete(gcGrouperSyncLog);
      }
    }
        
  }

  /**
   * delete from cache by owner id
   * @param gcGrouperSyncLog
   */
  public void internal_logCacheDeleteByOwnerIds(Set<String> ownerIds) {

    for (GcGrouperSyncLog gcGrouperSyncLog : new HashSet<GcGrouperSyncLog>(internalCacheSyncLogs.values())) {
      if (ownerIds.contains(gcGrouperSyncLog.getGrouperSyncOwnerId())) {
        internal_logCacheDelete(gcGrouperSyncLog);
      }
    }
        
  }

  /**
   * select grouper sync logs by sync id
   * @return the logs
   */
  public List<GcGrouperSyncLog> internal_logRetrieveFromDbAll() {
    
    // clear the cache
    this.internalCacheSyncLogs.clear();
    this.internalCacheSyncLogsById.clear();
    
    List<GcGrouperSyncLog> gcGrouperSyncLogList = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_log where grouper_sync_id = ?").addBindVar(this.getGcGrouperSync().getId()).selectList(GcGrouperSyncLog.class);
    
    for (GcGrouperSyncLog gcGrouperSyncLog : gcGrouperSyncLogList) {
      gcGrouperSyncLog.setGrouperSync(this.getGcGrouperSync());
      this.internal_logCacheAdd(gcGrouperSyncLog);
  
    }
    return gcGrouperSyncLogList;
  }

  /**
   * select grouper sync log by owner id
   * @param connectionName
   * @param ownerId
   * @return the logs
   */
  public List<GcGrouperSyncLog> internal_logRetrieveFromDbByOwnerId(String ownerId) {
    
    List<GcGrouperSyncLog> gcGrouperSyncLogs = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_log where grouper_sync_owner_id = ?")
          .addBindVar(ownerId).selectList(GcGrouperSyncLog.class);
    for (GcGrouperSyncLog gcGrouperSyncLog : gcGrouperSyncLogs) {
      gcGrouperSyncLog.setGrouperSync(this.getGcGrouperSync());
      this.internal_logCacheAdd(gcGrouperSyncLog);
    }
    return gcGrouperSyncLogs;
    
  }

  /**
   * select grouper sync group by sync id and group id
   * @param grouperSyncId
   * @param grouperGroupIdsCollection
   * @param provisionerName
   * @return the groupId to syncGroup map
   */
  public Map<String, GcGrouperSyncLog> internal_logRetrieveFromDbByOwnerIds(Collection<String> grouperGroupIdsCollection) {
    
    Map<String, GcGrouperSyncLog> result = new HashMap<String, GcGrouperSyncLog>();
    
    if (GrouperClientUtils.length(grouperGroupIdsCollection) == 0) {
      return result;
    }
    
    List<String> groupIdsList = new ArrayList<String>(grouperGroupIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfGroupIds = GrouperClientUtils.batchList(groupIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_log where grouper_sync_owner_id in ( " 
          + GrouperClientUtils.appendQuestions(batchOfGroupIds.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql(sql);
      for (String groupId : batchOfGroupIds) {
        gcDbAccess.addBindVar(groupId);
      }
      
      List<GcGrouperSyncLog> gcGrouperSyncGroups = gcDbAccess.selectList(GcGrouperSyncLog.class);
      
      for (GcGrouperSyncLog gcGrouperSyncGroup : GrouperClientUtils.nonNull(gcGrouperSyncGroups)) {
        result.put(gcGrouperSyncGroup.getGrouperSyncOwnerId(), gcGrouperSyncGroup);
        gcGrouperSyncGroup.setGrouperSync(this.getGcGrouperSync());
        this.internal_logCacheAdd(gcGrouperSyncGroup);
      }
      
    }
    return result;
  }

  /**
   * select grouper sync group by gcGrouperSyncGroupId id
   * @param gcGrouperSyncGroupId
   * @return the gcGrouperSyncGroup
   */
  public GcGrouperSyncLog internal_logRetrieveFromDbById(String gcGrouperSyncGroupId) {
    
    GcGrouperSyncLog gcGrouperSyncGroup = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_log where id = ?")
          .addBindVar(gcGrouperSyncGroupId).select(GcGrouperSyncLog.class);
    if (gcGrouperSyncGroup != null) {
      gcGrouperSyncGroup.setGrouperSync(this.getGcGrouperSync());
      this.internal_logCacheAdd(gcGrouperSyncGroup);
    }
    return gcGrouperSyncGroup;
    
  }

  /**
   * select grouper sync log by owner id
   * @param grouperSyncId
   * @param syncLogIdsCollection
   * @param provisionerName
   * @return the logId to syncLog map
   */
  public Map<String, GcGrouperSyncLog> internal_logRetrieveFromDbByIds(Collection<String> syncLogIdsCollection) {
    
    Map<String, GcGrouperSyncLog> result = new HashMap<String, GcGrouperSyncLog>();
    
    if (GrouperClientUtils.length(syncLogIdsCollection) == 0) {
      return result;
    }
    
    List<String> syncIdsList = new ArrayList<String>(syncLogIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(syncIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfSyncIds = GrouperClientUtils.batchList(syncIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_log where grouper_sync_id = ? and id in ( " 
          + GrouperClientUtils.appendQuestions(batchOfSyncIds.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql(sql).addBindVar(this.getGcGrouperSync().getId());
      for (String logId : batchOfSyncIds) {
        gcDbAccess.addBindVar(logId);
      }
      
      List<GcGrouperSyncLog> gcGrouperSyncLogs = gcDbAccess.selectList(GcGrouperSyncLog.class);
      
      for (GcGrouperSyncLog gcGrouperSyncLog : GrouperClientUtils.nonNull(gcGrouperSyncLogs)) {
        result.put(gcGrouperSyncLog.getId(), gcGrouperSyncLog);
        gcGrouperSyncLog.setGrouperSync(this.getGcGrouperSync());
        this.internal_logCacheAdd(gcGrouperSyncLog);
      }
      
    }
    return result;
  }

  /**
   * 
   */
  private GcGrouperSync gcGrouperSync;
  /**
   * if all objects have been retrieved from db
   */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private boolean logRetrievedAllObjectsFromDb = false;
  
  
  
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
   * @return number of logs stored
   */
  public int internal_logStoreAll() {
    return this.internal_logStore(this.internalCacheSyncLogs.values());
  }
  
  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncLogs
   * @return number of changes
   */
  public int internal_logStore(Collection<GcGrouperSyncLog> gcGrouperSyncLogs) {
  
    if (GrouperClientUtils.length(gcGrouperSyncLogs) == 0) {
      return 0;
    }
  
    int batchSize = this.getGcGrouperSync().batchSize();
  
    List<GcGrouperSyncLog> gcGrouperSyncLogsList = new ArrayList<GcGrouperSyncLog>(gcGrouperSyncLogs);
    
    for (GcGrouperSyncLog gcGrouperSyncGroup : GrouperClientUtils.nonNull(gcGrouperSyncLogs)) {
      gcGrouperSyncGroup.storePrepare();
    }
  
    int changes = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeBatchToDatabase(gcGrouperSyncLogsList, batchSize);
    
    for (GcGrouperSyncLog gcGrouperSyncLog : GrouperClientUtils.nonNull(gcGrouperSyncLogs)) {
      this.internal_logCacheAdd(gcGrouperSyncLog);
    }
    return changes;
  }

  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncGroups
   */
  public void internal_logStore(GcGrouperSyncLog gcGrouperSyncLog) {
  
    gcGrouperSyncLog.storePrepare();
  
    new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeToDatabase(gcGrouperSyncLog);
  
  }

}
