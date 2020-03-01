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

public class GcGrouperSyncMemberDao {

  /**
   * keep an internal cache of members by member id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncMember> internalCacheSyncMembers = new HashMap<String, GcGrouperSyncMember>();
  /**
   * keep an internal cache of members by uuid
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncMember> internalCacheSyncMembersById = new HashMap<String, GcGrouperSyncMember>();

  public GcGrouperSyncMemberDao() {
  }

  /**
   * select grouper sync member by member id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param memberId
   * @return the member
   */
  public GcGrouperSyncMember memberCreateByMemberId(String memberId) {
    GcGrouperSyncMember gcGrouperSyncMember = new GcGrouperSyncMember();
    gcGrouperSyncMember.setGrouperSync(this.getGcGrouperSync());
    gcGrouperSyncMember.setMemberId(memberId);
    this.internal_memberCacheAdd(gcGrouperSyncMember);
    return gcGrouperSyncMember;
  }

  /**
   * delete batch
   * @param gcGrouperSyncMembers
   * @return rows deleted (members and logs)
   */
  public int memberDelete(Collection<GcGrouperSyncMember> gcGrouperSyncMembers, boolean deleteMemberships, boolean deleteLogs) {
    int count = 0;
  
    if (GrouperClientUtils.length(gcGrouperSyncMembers) == 0) {
      return 0;
    }
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    Set<String> logMemberSyncIds = new HashSet<String>();
    
    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembers) {
      
      List<Object> currentBindVarRow = new ArrayList<Object>();
      currentBindVarRow.add(gcGrouperSyncMember.getId());
      batchBindVars.add(currentBindVarRow);
      
      logMemberSyncIds.add(gcGrouperSyncMember.getId());
      this.internal_memberCacheDelete(gcGrouperSyncMember);
    }
  
    String connectionName = gcGrouperSyncMembers.iterator().next().getConnectionName();
    
    if (deleteLogs) {
      count += this.getGcGrouperSync().getGcGrouperSyncLogDao().internal_logDeleteBatchByOwnerIds(logMemberSyncIds);
    }
    
    // TODO delete memberships? and membership log
  
    int[] rowDeleteCounts = new GcDbAccess().connectionName(connectionName).sql("delete from grouper_sync_member where id = ?")
      .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
  
    for (int rowDeleteCount : rowDeleteCounts) {
      count += rowDeleteCount;
    }
  
    return count;
    
  
  }

  /**
   * delete sync member
   * @param gcGrouperSyncMember
   * @return rows deleted (members and logs)
   */
  public int memberDelete(GcGrouperSyncMember gcGrouperSyncMember, boolean deleteMemberships, boolean deleteLogs ) {
    
    if (gcGrouperSyncMember == null) {
      return 0;
    }
    
    this.internal_memberCacheDelete(gcGrouperSyncMember);

    int count = 0;
    
    if (deleteLogs) {
      count += this.getGcGrouperSync().getGcGrouperSyncLogDao().logDeleteByOwnerId(gcGrouperSyncMember.getId());
    }
    
    // TODO delete memberships?

    int rowDeleteCount = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql("delete from grouper_sync_member where id = ?")
      .bindVars(gcGrouperSyncMember.getId()).executeSql();
    
    count += rowDeleteCount;
      
    return count;

  }

  /**
   * delete all members for a sync
   * @param deleteMemberships true if delete memberships and logs for memberships too
   * @param deleteLogs delete logs too
   * @return the syncs
   */
  public int memberDeleteAll(boolean deleteMemberships, boolean deleteLogs) {
    this.internalCacheSyncMembers.clear();
    this.internalCacheSyncMembersById.clear();
    
    int rowDeleteCount = 0;
    
    if (deleteLogs) {
      rowDeleteCount += new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_log where grouper_sync_owner_id in ( select id from grouper_sync_member gsg where gsg.grouper_sync_id = ?)")
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
        "delete from grouper_sync_member where grouper_sync_id = ?)")
        .bindVars(this.getGcGrouperSync().getId()).executeSql();
    
    return rowDeleteCount;
  }

  /**
   * select grouper sync member by sync id and member id
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncMember> memberRetrieveAll() {
    if (!this.memberRetrievedAllObjectsFromDb) {
      for (GcGrouperSyncMember gcGrouperSyncMember : this.internal_memberRetrieveFromDbAll()) {
        this.internal_memberCacheAdd(gcGrouperSyncMember);
      }
      this.memberRetrievedAllObjectsFromDb = true;
    }
    return new ArrayList<GcGrouperSyncMember>(this.internalCacheSyncMembers.values());
  }

  /**
   * select grouper sync member by member id
   * @param connectionName
   * @param memberId
   * @return the member
   */
  public GcGrouperSyncMember memberRetrieveByMemberId(String memberId) {
    GcGrouperSyncMember gcGrouperSyncMember = this.internalCacheSyncMembers.get(memberId);
    if (gcGrouperSyncMember == null) {
      gcGrouperSyncMember = internal_memberRetrieveFromDbByMemberId(memberId);
    }
    return gcGrouperSyncMember;
  }

  /**
   * select grouper sync member by sync id and member id
   * @param grouperSyncId
   * @param grouperMemberIdsCollection
   * @param provisionerName
   * @return the memberId to syncMember map
   */
  public Map<String, GcGrouperSyncMember> memberRetrieveByMemberIds(
      Collection<String> grouperMemberIdsCollection) {
  
    Map<String, GcGrouperSyncMember> result = new HashMap<String, GcGrouperSyncMember>();
    
    Set<String> memberIdsToGetFromDb = new HashSet<String>();
    
    // try from cache
    for (String memberId : GrouperClientUtils.nonNull(grouperMemberIdsCollection)) {
      GcGrouperSyncMember gcGrouperSyncMember = this.internalCacheSyncMembers.get(memberId);
      if (gcGrouperSyncMember != null) {
        result.put(memberId, gcGrouperSyncMember);
      } else {
        memberIdsToGetFromDb.add(memberId);
      }
    }
    
    // or else get from db
    if (memberIdsToGetFromDb.size() > 0) {
      Map<String, GcGrouperSyncMember> fromDb = internal_memberRetrieveFromDbByMemberIds(memberIdsToGetFromDb);
      result.putAll(fromDb);
    }
    
    return result;
    
  }

  /**
   * select grouper sync member by member id
   * @param gcGrouperSyncMemberId
   * @return the member
   */
  public GcGrouperSyncMember memberRetrieveById(String gcGrouperSyncMemberId) {
    GcGrouperSyncMember gcGrouperSyncMember = this.internalCacheSyncMembersById.get(gcGrouperSyncMemberId);
    if (gcGrouperSyncMember == null) {
      gcGrouperSyncMember = internal_memberRetrieveFromDbById(gcGrouperSyncMemberId);
    }
    return gcGrouperSyncMember;
  }

  /**
   * select grouper sync member by member id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param memberId
   * @return the member
   */
  public GcGrouperSyncMember memberRetrieveOrCreateByMemberId(String memberId) {
    GcGrouperSyncMember gcGrouperSyncMember = this.memberRetrieveByMemberId(memberId);
    if (gcGrouperSyncMember == null) {
      gcGrouperSyncMember = internal_memberRetrieveFromDbByMemberId(memberId);
    }
    if (gcGrouperSyncMember == null) {
      gcGrouperSyncMember = this.memberCreateByMemberId(memberId);
    }
    return gcGrouperSyncMember;
  }

  /**
   * select grouper sync member by member id.  note, this does not store the members to the database, do that later
   * @param grouperSyncId
   * @param grouperMemberIdsCollection
   * @param provisionerName
   * @return the memberId to syncMember map
   */
  public Map<String, GcGrouperSyncMember> memberRetrieveOrCreateByMemberIds(
      Collection<String> grouperMemberIdsCollection) {
  
    Map<String, GcGrouperSyncMember> result = this.memberRetrieveByMemberIds(grouperMemberIdsCollection);
  
    // if done, return
    if (GrouperClientUtils.length(grouperMemberIdsCollection) == 0 || grouperMemberIdsCollection.size() == result.size()) {
      return result;
    }
    
    Set<String> memberIdsToCreate = new HashSet<String>(grouperMemberIdsCollection);
    memberIdsToCreate.removeAll(result.keySet());
    
    for (String memberIdToCreate : memberIdsToCreate) {
      GcGrouperSyncMember gcGrouperSyncMember = this.memberCreateByMemberId(memberIdToCreate);
      result.put(memberIdToCreate, gcGrouperSyncMember);
    }
    
    return result;
    
  }

  /**
   * 
   * @param gcGrouperSyncMemberId
   * @return log
   */
  public GcGrouperSyncLog memberCreateLog(GcGrouperSyncMember gcGrouperSyncMember) {
    return this.gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId(gcGrouperSyncMember.getId());
  }

  /**
   * 
   * @param gcGrouperSyncMember
   */
  private void internal_memberCacheAdd(GcGrouperSyncMember gcGrouperSyncMember) {
    if (gcGrouperSyncMember.getMemberId() != null) {
      this.internalCacheSyncMembers.put(gcGrouperSyncMember.getMemberId(), gcGrouperSyncMember);
    }
    if (gcGrouperSyncMember.getId() != null) { 
      this.internalCacheSyncMembersById.put(gcGrouperSyncMember.getId(), gcGrouperSyncMember);
    }
  }

  /**
   * 
   * @param gcGrouperSyncMember
   */
  public void internal_memberCacheDelete(GcGrouperSyncMember gcGrouperSyncMember) {
    if (gcGrouperSyncMember.getMemberId() != null) {
      this.internalCacheSyncMembers.remove(gcGrouperSyncMember.getMemberId());
    }
    if (gcGrouperSyncMember.getId() != null) {
      this.internalCacheSyncMembersById.remove(gcGrouperSyncMember.getId());
    }
    
  }

  /**
   * select grouper sync member by sync id and member id
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncMember> internal_memberRetrieveFromDbAll() {
    
    // clear the cache
    this.internalCacheSyncMembers.clear();
    this.internalCacheSyncMembersById.clear();
    
    List<GcGrouperSyncMember> gcGrouperSyncMemberList = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_member where grouper_sync_id = ?").addBindVar(this.getGcGrouperSync().getId()).selectList(GcGrouperSyncMember.class);
    
    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMemberList) {
      gcGrouperSyncMember.setGrouperSync(this.getGcGrouperSync());
      this.internal_memberCacheAdd(gcGrouperSyncMember);
  
    }
    return gcGrouperSyncMemberList;
  }

  /**
   * select grouper sync member by member id
   * @param connectionName
   * @param memberId
   * @return the member
   */
  public GcGrouperSyncMember internal_memberRetrieveFromDbByMemberId(String memberId) {
    
    GcGrouperSyncMember gcGrouperSyncMember = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_member where grouper_sync_id = ? and member_id = ?")
          .addBindVar(this.getGcGrouperSync().getId()).addBindVar(memberId).select(GcGrouperSyncMember.class);
    if (gcGrouperSyncMember != null) {
      gcGrouperSyncMember.setGrouperSync(this.getGcGrouperSync());
      this.internal_memberCacheAdd(gcGrouperSyncMember);
    }
    return gcGrouperSyncMember;
    
  }

  /**
   * select grouper sync member by sync id and member id
   * @param grouperSyncId
   * @param grouperMemberIdsCollection
   * @param provisionerName
   * @return the memberId to syncMember map
   */
  public Map<String, GcGrouperSyncMember> internal_memberRetrieveFromDbByMemberIds(Collection<String> grouperMemberIdsCollection) {
    
    Map<String, GcGrouperSyncMember> result = new HashMap<String, GcGrouperSyncMember>();
    
    if (GrouperClientUtils.length(grouperMemberIdsCollection) == 0) {
      return result;
    }
    
    List<String> memberIdsList = new ArrayList<String>(grouperMemberIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(memberIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfMemberIds = GrouperClientUtils.batchList(memberIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_member where grouper_sync_id = ? and member_id in ( " 
          + GrouperClientUtils.appendQuestions(batchOfMemberIds.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql(sql).addBindVar(this.getGcGrouperSync().getId());
      for (String memberId : batchOfMemberIds) {
        gcDbAccess.addBindVar(memberId);
      }
      
      List<GcGrouperSyncMember> gcGrouperSyncMembers = gcDbAccess.selectList(GcGrouperSyncMember.class);
      
      for (GcGrouperSyncMember gcGrouperSyncMember : GrouperClientUtils.nonNull(gcGrouperSyncMembers)) {
        result.put(gcGrouperSyncMember.getMemberId(), gcGrouperSyncMember);
        gcGrouperSyncMember.setGrouperSync(this.getGcGrouperSync());
        this.internal_memberCacheAdd(gcGrouperSyncMember);
      }
      
    }
    return result;
  }

  /**
   * select grouper sync member by gcGrouperSyncMemberId id
   * @param gcGrouperSyncMemberId
   * @return the gcGrouperSyncMember
   */
  public GcGrouperSyncMember internal_memberRetrieveFromDbById(String gcGrouperSyncMemberId) {
    
    GcGrouperSyncMember gcGrouperSyncMember = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_member where id = ?")
          .addBindVar(gcGrouperSyncMemberId).select(GcGrouperSyncMember.class);
    if (gcGrouperSyncMember != null) {
      gcGrouperSyncMember.setGrouperSync(this.getGcGrouperSync());
      this.internal_memberCacheAdd(gcGrouperSyncMember);
    }
    return gcGrouperSyncMember;
    
  }

  /**
   * select grouper sync member by sync id and member id
   * @param grouperSyncId
   * @param syncMemberIdsCollection
   * @param provisionerName
   * @return the id to syncMember map
   */
  public Map<String, GcGrouperSyncMember> internal_memberRetrieveFromDbByIds(Collection<String> syncMemberIdsCollection) {
    
    Map<String, GcGrouperSyncMember> result = new HashMap<String, GcGrouperSyncMember>();
    
    if (GrouperClientUtils.length(syncMemberIdsCollection) == 0) {
      return result;
    }
    
    List<String> syncIdsList = new ArrayList<String>(syncMemberIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(syncIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfSyncIds = GrouperClientUtils.batchList(syncIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_member where grouper_sync_id = ? and id in ( " 
          + GrouperClientUtils.appendQuestions(batchOfSyncIds.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql(sql).addBindVar(this.getGcGrouperSync().getId());
      for (String memberId : batchOfSyncIds) {
        gcDbAccess.addBindVar(memberId);
      }
      
      List<GcGrouperSyncMember> gcGrouperSyncMembers = gcDbAccess.selectList(GcGrouperSyncMember.class);
      
      for (GcGrouperSyncMember gcGrouperSyncMember : GrouperClientUtils.nonNull(gcGrouperSyncMembers)) {
        result.put(gcGrouperSyncMember.getId(), gcGrouperSyncMember);
        gcGrouperSyncMember.setGrouperSync(this.getGcGrouperSync());
        this.internal_memberCacheAdd(gcGrouperSyncMember);
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
  private boolean memberRetrievedAllObjectsFromDb = false;
  
  
  
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
   * @return number of members stored
   */
  public int internal_memberStore() {
    return this.internal_memberStore(this.internalCacheSyncMembers.values());
  }
  
  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncMembers
   * @return number of changes
   */
  public int internal_memberStore(Collection<GcGrouperSyncMember> gcGrouperSyncMembers) {
  
    if (GrouperClientUtils.length(gcGrouperSyncMembers) == 0) {
      return 0;
    }
  
    int batchSize = this.getGcGrouperSync().batchSize();
  
    List<GcGrouperSyncMember> gcGrouperSyncMembersList = new ArrayList<GcGrouperSyncMember>(gcGrouperSyncMembers);
    
    for (GcGrouperSyncMember gcGrouperSyncMember : GrouperClientUtils.nonNull(gcGrouperSyncMembers)) {
      gcGrouperSyncMember.storePrepare();
    }
  
    int changes = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeBatchToDatabase(gcGrouperSyncMembersList, batchSize);
    
    for (GcGrouperSyncMember gcGrouperSyncMember : GrouperClientUtils.nonNull(gcGrouperSyncMembers)) {
      this.internal_memberCacheAdd(gcGrouperSyncMember);
    }
    return changes;
  }

  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncMembers
   */
  public void internal_memberStore(GcGrouperSyncMember gcGrouperSyncMember) {
  
    gcGrouperSyncMember.storePrepare();
  
    new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeToDatabase(gcGrouperSyncMember);
  
  }

}
