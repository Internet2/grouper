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

public class GcGrouperSyncGroupDao {

  /**
   * keep an internal cache of groups by group id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncGroup> internalCacheSyncGroups = new HashMap<String, GcGrouperSyncGroup>();
  /**
   * keep an internal cache of groups by uuid
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncGroup> internalCacheSyncGroupsById = new HashMap<String, GcGrouperSyncGroup>();

  public GcGrouperSyncGroupDao() {
  }

  /**
   * select grouper sync group by group id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param groupId
   * @return the group
   */
  public GcGrouperSyncGroup groupCreateByGroupId(String groupId) {
    GcGrouperSyncGroup gcGrouperSyncGroup = new GcGrouperSyncGroup();
    gcGrouperSyncGroup.setGrouperSync(this.getGcGrouperSync());
    gcGrouperSyncGroup.setGroupId(groupId);
    this.internal_groupCacheAdd(gcGrouperSyncGroup);
    return gcGrouperSyncGroup;
  }

  /**
   * delete batch
   * @param gcGrouperSyncGroups
   * @return rows deleted (groups and logs)
   */
  public int groupDelete(Collection<GcGrouperSyncGroup> gcGrouperSyncGroups, boolean deleteMemberships, boolean deleteLogs) {
    int count = 0;
  
    if (GrouperClientUtils.length(gcGrouperSyncGroups) == 0) {
      return 0;
    }
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    Set<String> logGroupSyncIds = new HashSet<String>();
    
    for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroups) {
      
      List<Object> currentBindVarRow = new ArrayList<Object>();
      currentBindVarRow.add(gcGrouperSyncGroup.getId());
      batchBindVars.add(currentBindVarRow);
      
      logGroupSyncIds.add(gcGrouperSyncGroup.getId());
      this.internal_groupCacheDelete(gcGrouperSyncGroup);
    }
  
    String connectionName = gcGrouperSyncGroups.iterator().next().getConnectionName();
    
    if (deleteLogs) {
      count += this.getGcGrouperSync().getGcGrouperSyncLogDao().internal_logDeleteBatchByOwnerIds(logGroupSyncIds);
    }
    
    // TODO delete memberships? and membership log
  
    int[] rowDeleteCounts = new GcDbAccess().connectionName(connectionName).sql("delete from grouper_sync_group where id = ?")
      .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
  
    for (int rowDeleteCount : rowDeleteCounts) {
      count += rowDeleteCount;
    }
  
    return count;
    
  
  }

  /**
     * delete sync group
     * @param gcGrouperSyncGroup
     * @return rows deleted (groups and logs)
     */
    public int groupDelete(GcGrouperSyncGroup gcGrouperSyncGroup, boolean deleteMemberships, boolean deleteLogs ) {
      
      if (gcGrouperSyncGroup == null) {
        return 0;
      }
      
      this.internal_groupCacheDelete(gcGrouperSyncGroup);
  
      int count = 0;
      
      if (deleteLogs) {
        count += this.getGcGrouperSync().getGcGrouperSyncLogDao().logDeleteByOwnerId(gcGrouperSyncGroup.getId());
      }
      
      // TODO delete memberships?
  
      int rowDeleteCount = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql("delete from grouper_sync_group where id = ?")
        .bindVars(gcGrouperSyncGroup.getId()).executeSql();
      
      count += rowDeleteCount;
        
      return count;
  
    }

  /**
   * delete all groups for a sync
   * @param deleteMemberships true if delete memberships and logs for memberships too
   * @param deleteLogs delete logs too
   * @return the syncs
   */
  public int groupDeleteAll(boolean deleteMemberships, boolean deleteLogs) {
    this.internalCacheSyncGroups.clear();
    this.internalCacheSyncGroupsById.clear();
    
    int rowDeleteCount = 0;
    
    if (deleteLogs) {
      rowDeleteCount += new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_log where grouper_sync_owner_id in ( select id from grouper_sync_group gsg where gsg.grouper_sync_id = ?)")
        .bindVars(this.getGcGrouperSync().getId()).executeSql();
    }
    
    if (deleteMemberships) {
      if (deleteLogs) {
        rowDeleteCount += new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
            "delete from grouper_sync_log where grouper_sync_owner_id in ( select id from grouper_sync_membership gsm where gsm.grouper_sync_id = ?)")
            .bindVars(this.getGcGrouperSync().getId()).executeSql();
      }
      rowDeleteCount += new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
          "delete from grouper_sync_membership where grouper_sync_id = ?)")
          .bindVars(this.getGcGrouperSync().getId()).executeSql();
    }
    rowDeleteCount += new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_group where grouper_sync_id = ?)")
        .bindVars(this.getGcGrouperSync().getId()).executeSql();
    
    return rowDeleteCount;
  }

  /**
   * select grouper sync group by sync id and group id
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncGroup> groupRetrieveAll() {
    if (!this.groupRetrievedAllObjectsFromDb) {
      for (GcGrouperSyncGroup gcGrouperSyncGroup : this.internal_groupRetrieveFromDbAll()) {
        this.internal_groupCacheAdd(gcGrouperSyncGroup);
      }
      this.groupRetrievedAllObjectsFromDb = true;
    }
    return new ArrayList<GcGrouperSyncGroup>(this.internalCacheSyncGroups.values());
  }

  /**
   * select grouper sync group by group id
   * @param connectionName
   * @param groupId
   * @return the group
   */
  public GcGrouperSyncGroup groupRetrieveByGroupId(String groupId) {
    GcGrouperSyncGroup gcGrouperSyncGroup = this.internalCacheSyncGroups.get(groupId);
    if (gcGrouperSyncGroup == null) {
      gcGrouperSyncGroup = internal_groupRetrieveFromDbByGroupId(groupId);
    }
    return gcGrouperSyncGroup;
  }

  /**
   * select grouper sync group by sync id and group id
   * @param grouperSyncId
   * @param grouperGroupIdsCollection
   * @param provisionerName
   * @return the groupId to syncGroup map
   */
  public Map<String, GcGrouperSyncGroup> groupRetrieveByGroupIds(
      Collection<String> grouperGroupIdsCollection) {
  
    Map<String, GcGrouperSyncGroup> result = new HashMap<String, GcGrouperSyncGroup>();
    
    Set<String> groupIdsToGetFromDb = new HashSet<String>();
    
    // try from cache
    for (String groupId : GrouperClientUtils.nonNull(grouperGroupIdsCollection)) {
      GcGrouperSyncGroup gcGrouperSyncGroup = this.internalCacheSyncGroups.get(groupId);
      if (gcGrouperSyncGroup != null) {
        result.put(groupId, gcGrouperSyncGroup);
      } else {
        groupIdsToGetFromDb.add(groupId);
      }
    }
    
    // or else get from db
    if (groupIdsToGetFromDb.size() > 0) {
      Map<String, GcGrouperSyncGroup> fromDb = internal_groupRetrieveFromDbByGroupIds(groupIdsToGetFromDb);
      result.putAll(fromDb);
    }
    
    return result;
    
  }

  /**
   * select grouper sync group by group id
   * @param gcGrouperSyncGroupId
   * @return the group
   */
  public GcGrouperSyncGroup groupRetrieveById(String gcGrouperSyncGroupId) {
    GcGrouperSyncGroup gcGrouperSyncGroup = this.internalCacheSyncGroupsById.get(gcGrouperSyncGroupId);
    if (gcGrouperSyncGroup == null) {
      gcGrouperSyncGroup = internal_groupRetrieveFromDbById(gcGrouperSyncGroupId);
    }
    return gcGrouperSyncGroup;
  }

  /**
   * select grouper sync group by group id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param groupId
   * @return the group
   */
  public GcGrouperSyncGroup groupRetrieveOrCreateByGroupId(String groupId) {
    GcGrouperSyncGroup gcGrouperSyncGroup = this.groupRetrieveByGroupId(groupId);
    if (gcGrouperSyncGroup == null) {
      gcGrouperSyncGroup = internal_groupRetrieveFromDbByGroupId(groupId);
    }
    if (gcGrouperSyncGroup == null) {
      gcGrouperSyncGroup = this.groupCreateByGroupId(groupId);
    }
    return gcGrouperSyncGroup;
  }

  /**
   * select grouper sync group by group id.  note, this does not store the groups to the database, do that later
   * @param grouperSyncId
   * @param grouperGroupIdsCollection
   * @param provisionerName
   * @return the groupId to syncGroup map
   */
  public Map<String, GcGrouperSyncGroup> groupRetrieveOrCreateByGroupIds(
      Collection<String> grouperGroupIdsCollection) {
  
    Map<String, GcGrouperSyncGroup> result = this.groupRetrieveByGroupIds(grouperGroupIdsCollection);
  
    // if done, return
    if (GrouperClientUtils.length(grouperGroupIdsCollection) == 0 || grouperGroupIdsCollection.size() == result.size()) {
      return result;
    }
    
    Set<String> groupIdsToCreate = new HashSet<String>(grouperGroupIdsCollection);
    groupIdsToCreate.removeAll(result.keySet());
    
    for (String groupIdToCreate : groupIdsToCreate) {
      GcGrouperSyncGroup gcGrouperSyncGroup = this.groupCreateByGroupId(groupIdToCreate);
      result.put(groupIdToCreate, gcGrouperSyncGroup);
    }
    
    return result;
    
  }

  /**
   * 
   * @param gcGrouperSyncGroupId
   * @return log
   */
  public GcGrouperSyncLog groupCreateLog(GcGrouperSyncGroup gcGrouperSyncGroup) {
    return this.gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId(gcGrouperSyncGroup.getId());
  }

  /**
   * 
   * @param gcGrouperSyncGroup
   */
  private void internal_groupCacheAdd(GcGrouperSyncGroup gcGrouperSyncGroup) {
    if (gcGrouperSyncGroup.getGroupId() != null) {
      this.internalCacheSyncGroups.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
    }
    if (gcGrouperSyncGroup.getId() != null) { 
      this.internalCacheSyncGroupsById.put(gcGrouperSyncGroup.getId(), gcGrouperSyncGroup);
    }
  }

  /**
   * 
   * @param gcGrouperSyncGroup
   */
  public void internal_groupCacheDelete(GcGrouperSyncGroup gcGrouperSyncGroup) {
    if (gcGrouperSyncGroup.getGroupId() != null) {
      this.internalCacheSyncGroups.remove(gcGrouperSyncGroup.getGroupId());
    }
    if (gcGrouperSyncGroup.getId() != null) {
      this.internalCacheSyncGroupsById.remove(gcGrouperSyncGroup.getId());
    }
    
  }

  /**
   * select grouper sync group by sync id and group id
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncGroup> internal_groupRetrieveFromDbAll() {
    
    // clear the cache
    this.internalCacheSyncGroups.clear();
    this.internalCacheSyncGroupsById.clear();
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroupList = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_group where grouper_sync_id = ?").addBindVar(this.getGcGrouperSync().getId()).selectList(GcGrouperSyncGroup.class);
    
    for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroupList) {
      gcGrouperSyncGroup.setGrouperSync(this.getGcGrouperSync());
      this.internal_groupCacheAdd(gcGrouperSyncGroup);
  
    }
    return gcGrouperSyncGroupList;
  }

  /**
   * select grouper sync group by group id
   * @param connectionName
   * @param groupId
   * @return the group
   */
  public GcGrouperSyncGroup internal_groupRetrieveFromDbByGroupId(String groupId) {
    
    GcGrouperSyncGroup gcGrouperSyncGroup = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_group where grouper_sync_id = ? and group_id = ?")
          .addBindVar(this.getGcGrouperSync().getId()).addBindVar(groupId).select(GcGrouperSyncGroup.class);
    if (gcGrouperSyncGroup != null) {
      gcGrouperSyncGroup.setGrouperSync(this.getGcGrouperSync());
      this.internal_groupCacheAdd(gcGrouperSyncGroup);
    }
    return gcGrouperSyncGroup;
    
  }

  /**
   * select grouper sync group by sync id and group id
   * @param grouperSyncId
   * @param grouperGroupIdsCollection
   * @param provisionerName
   * @return the groupId to syncGroup map
   */
  public Map<String, GcGrouperSyncGroup> internal_groupRetrieveFromDbByGroupIds(Collection<String> grouperGroupIdsCollection) {
    
    Map<String, GcGrouperSyncGroup> result = new HashMap<String, GcGrouperSyncGroup>();
    
    if (GrouperClientUtils.length(grouperGroupIdsCollection) == 0) {
      return result;
    }
    
    List<String> groupIdsList = new ArrayList<String>(grouperGroupIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfGroupIds = GrouperClientUtils.batchList(groupIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_group where grouper_sync_id = ? and group_id in ( " 
          + GrouperClientUtils.appendQuestions(batchOfGroupIds.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql(sql).addBindVar(this.getGcGrouperSync().getId());
      for (String groupId : batchOfGroupIds) {
        gcDbAccess.addBindVar(groupId);
      }
      
      List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcDbAccess.selectList(GcGrouperSyncGroup.class);
      
      for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperClientUtils.nonNull(gcGrouperSyncGroups)) {
        result.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
        gcGrouperSyncGroup.setGrouperSync(this.getGcGrouperSync());
        this.internal_groupCacheAdd(gcGrouperSyncGroup);
      }
      
    }
    return result;
  }

  /**
   * select grouper sync group by gcGrouperSyncGroupId id
   * @param gcGrouperSyncGroupId
   * @return the gcGrouperSyncGroup
   */
  public GcGrouperSyncGroup internal_groupRetrieveFromDbById(String gcGrouperSyncGroupId) {
    
    GcGrouperSyncGroup gcGrouperSyncGroup = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_group where id = ?")
          .addBindVar(gcGrouperSyncGroupId).select(GcGrouperSyncGroup.class);
    if (gcGrouperSyncGroup != null) {
      gcGrouperSyncGroup.setGrouperSync(this.getGcGrouperSync());
      this.internal_groupCacheAdd(gcGrouperSyncGroup);
    }
    return gcGrouperSyncGroup;
    
  }

  /**
   * select grouper sync group by sync id and group id
   * @param grouperSyncId
   * @param syncGroupIdsCollection
   * @param provisionerName
   * @return the id to syncGroup map
   */
  public Map<String, GcGrouperSyncGroup> internal_groupRetrieveFromDbByIds(Collection<String> syncGroupIdsCollection) {
    
    Map<String, GcGrouperSyncGroup> result = new HashMap<String, GcGrouperSyncGroup>();
    
    if (GrouperClientUtils.length(syncGroupIdsCollection) == 0) {
      return result;
    }
    
    List<String> syncIdsList = new ArrayList<String>(syncGroupIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(syncIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfSyncIds = GrouperClientUtils.batchList(syncIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_group where grouper_sync_id = ? and id in ( " 
          + GrouperClientUtils.appendQuestions(batchOfSyncIds.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql(sql).addBindVar(this.getGcGrouperSync().getId());
      for (String groupId : batchOfSyncIds) {
        gcDbAccess.addBindVar(groupId);
      }
      
      List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcDbAccess.selectList(GcGrouperSyncGroup.class);
      
      for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperClientUtils.nonNull(gcGrouperSyncGroups)) {
        result.put(gcGrouperSyncGroup.getId(), gcGrouperSyncGroup);
        gcGrouperSyncGroup.setGrouperSync(this.getGcGrouperSync());
        this.internal_groupCacheAdd(gcGrouperSyncGroup);
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
  private boolean groupRetrievedAllObjectsFromDb = false;
  
  
  
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
  public int internal_groupStore() {
    return this.internal_groupStore(this.internalCacheSyncGroups.values());
  }
  
  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncGroups
   * @return number of changes
   */
  public int internal_groupStore(Collection<GcGrouperSyncGroup> gcGrouperSyncGroups) {
  
    if (GrouperClientUtils.length(gcGrouperSyncGroups) == 0) {
      return 0;
    }
  
    int batchSize = this.getGcGrouperSync().batchSize();
  
    List<GcGrouperSyncGroup> gcGrouperSyncGroupsList = new ArrayList<GcGrouperSyncGroup>(gcGrouperSyncGroups);
    
    for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperClientUtils.nonNull(gcGrouperSyncGroups)) {
      gcGrouperSyncGroup.storePrepare();
    }
  
    int changes = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeBatchToDatabase(gcGrouperSyncGroupsList, batchSize);
    
    for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperClientUtils.nonNull(gcGrouperSyncGroups)) {
      this.internal_groupCacheAdd(gcGrouperSyncGroup);
    }
    return changes;
  }

  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncGroups
   */
  public void internal_groupStore(GcGrouperSyncGroup gcGrouperSyncGroup) {
  
    gcGrouperSyncGroup.storePrepare();
  
    new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeToDatabase(gcGrouperSyncGroup);
  
  }

}
