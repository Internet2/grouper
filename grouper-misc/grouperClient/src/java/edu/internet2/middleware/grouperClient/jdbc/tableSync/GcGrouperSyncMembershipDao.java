package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * membership dao
 * @author mchyzer
 *
 */
public class GcGrouperSyncMembershipDao {

  // TODO search for membership id
  // TODO look for to dos in other classes
  /**
   * keep an internal cache of memberships by syncGroupId and syncMemberId
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<MultiKey, GcGrouperSyncMembership> internalCacheSyncMemberships = new HashMap<MultiKey, GcGrouperSyncMembership>();
  /**
   * keep an internal cache of memberships by uuid
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, GcGrouperSyncMembership> internalCacheSyncMembershipsById = new HashMap<String, GcGrouperSyncMembership>();

  public GcGrouperSyncMembershipDao() {
  }

  /**
   * select grouper sync membership by group id and member id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param membershipId
   * @return the membership
   */
  public GcGrouperSyncMembership membershipCreateByGroupAndMember(GcGrouperSyncGroup gcGrouperSyncGroup, GcGrouperSyncMember gcGrouperSyncMember) {
    GcGrouperSyncMembership gcGrouperSyncMembership = this.membershipCreateBySyncGroupIdAndSyncMemberId(gcGrouperSyncGroup.getId(), gcGrouperSyncMember.getId());
    gcGrouperSyncMembership.setGrouperSyncGroup(gcGrouperSyncGroup);
    gcGrouperSyncMembership.setGrouperSyncMember(gcGrouperSyncMember);
    return gcGrouperSyncMembership;
  }

  /**
   * create grouper sync membership.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param groupId
   * @return the group
   */
  public GcGrouperSyncMembership internal_membershipCreateBySyncGroupIdAndSyncMemberIdHelper(String syncGroupId, String syncMemberId) {
    GcGrouperSyncMembership gcGrouperSyncMembership = new GcGrouperSyncMembership();
    gcGrouperSyncMembership.setGrouperSync(this.getGcGrouperSync());
    gcGrouperSyncMembership.setGrouperSyncGroupId(syncGroupId);
    gcGrouperSyncMembership.setGrouperSyncMemberId(syncMemberId);
    return gcGrouperSyncMembership;
  }

  /**
   * select grouper sync membership by group id and member id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param membershipId
   * @return the membership
   */
  public GcGrouperSyncMembership membershipCreateBySyncGroupIdAndSyncMemberId(String syncGroupId, String syncMemberId) {
    GcGrouperSyncMembership gcGrouperSyncMembership = internal_membershipCreateBySyncGroupIdAndSyncMemberIdHelper(syncGroupId, syncMemberId);
    this.internal_membershipStore(gcGrouperSyncMembership);
    this.gcGrouperSync.addObjectCreatedCount(1);
    this.internal_membershipCacheAdd(gcGrouperSyncMembership);
    return gcGrouperSyncMembership;
  }

  /**
   * delete batch
   * @param gcGrouperSyncMemberships
   * @return rows deleted (memberships and logs)
   */
  public int membershipDelete(Collection<GcGrouperSyncMembership> gcGrouperSyncMemberships, boolean deleteLogs) {
    int count = 0;
  
    if (GrouperClientUtils.length(gcGrouperSyncMemberships) == 0) {
      return 0;
    }
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    Set<String> logMembershipSyncIds = new HashSet<String>();
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMemberships) {
      
      List<Object> currentBindVarRow = new ArrayList<Object>();
      currentBindVarRow.add(gcGrouperSyncMembership.getId());
      batchBindVars.add(currentBindVarRow);

      logMembershipSyncIds.add(gcGrouperSyncMembership.getId());
      this.internal_membershipCacheDelete(gcGrouperSyncMembership);
    }
  
    String connectionName = gcGrouperSyncMemberships.iterator().next().getConnectionName();
    
    if (deleteLogs) {
      count += this.getGcGrouperSync().getGcGrouperSyncLogDao().internal_logDeleteBatchByOwnerIds(logMembershipSyncIds);
    }
    
    int[] rowDeleteCounts = new GcDbAccess().connectionName(connectionName).sql("delete from grouper_sync_membership where id = ?")
      .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
  
    for (int rowDeleteCount : rowDeleteCounts) {
      count += rowDeleteCount;
    }
  
    return count;
    
  
  }

  /**
   * delete sync membership
   * @param gcGrouperSyncMembership
   * @return rows deleted (memberships and logs)
   */
  public int membershipDelete(GcGrouperSyncMembership gcGrouperSyncMembership, boolean deleteLogs ) {
    
    if (gcGrouperSyncMembership == null) {
      return 0;
    }
    
    this.internal_membershipCacheDelete(gcGrouperSyncMembership);

    int count = 0;
    
    if (deleteLogs) {
      count += this.getGcGrouperSync().getGcGrouperSyncLogDao().logDeleteByOwnerId(gcGrouperSyncMembership.getId());
    }
    
    int rowDeleteCount = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql("delete from grouper_sync_membership where id = ?")
      .bindVars(gcGrouperSyncMembership.getId()).executeSql();
    
    count += rowDeleteCount;
      
    return count;

  }

  /**
   * delete all memberships for a sync
   * @param deleteMemberships true if delete memberships and logs for memberships too
   * @param deleteLogs delete logs too
   * @return the syncs
   */
  public int membershipDeleteAll(boolean deleteMemberships, boolean deleteLogs) {
    this.internalCacheSyncMemberships.clear();
    this.internalCacheSyncMembershipsById.clear();
    
    int rowDeleteCount = 0;
    
    if (deleteLogs) {
      rowDeleteCount += new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_log where grouper_sync_owner_id in ( select id from grouper_sync_membership gsg where gsg.grouper_sync_id = ?)")
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
        "delete from grouper_sync_membership where grouper_sync_id = ?)")
        .bindVars(this.getGcGrouperSync().getId()).executeSql();
    
    return rowDeleteCount;
  }

  /**
   * select grouper sync membership by sync id and membership id
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncMembership> membershipRetrieveAll() {
    if (!this.membershipRetrievedAllObjectsFromDb) {
      for (GcGrouperSyncMembership gcGrouperSyncMembership : this.internal_membershipRetrieveFromDbAll()) {
        this.internal_membershipCacheAdd(gcGrouperSyncMembership);
      }
      this.membershipRetrievedAllObjectsFromDb = true;
    }
    return new ArrayList<GcGrouperSyncMembership>(this.internalCacheSyncMemberships.values());
  }

  /**
   * select grouper sync membership by membership id
   * @param connectionName
   * @param membershipId
   * @return the membership
   */
  public GcGrouperSyncMembership membershipRetrieveBySyncGroupIdAndSyncMemberId(String syncGroupId, String syncMemberId) {
    GcGrouperSyncMembership gcGrouperSyncMembership = this.internalCacheSyncMemberships.get(new MultiKey(syncGroupId, syncMemberId));
    if (gcGrouperSyncMembership == null) {
      gcGrouperSyncMembership = internal_membershipRetrieveFromDbBySyncGroupIdAndSyncMemberId(syncGroupId, syncMemberId);
    }
    return gcGrouperSyncMembership;
  }


  /**
   * select grouper sync membership by group uuid and member uuid, note this assumes the syncGroup and syncMember exist
   * @param groupId
   * @param memberId 
   * @return the membership
   */
  public GcGrouperSyncMembership membershipRetrieveByGroupIdAndMemberId(String groupId, String memberId) {
    GcGrouperSyncGroup gcGrouperSyncGroup = this.gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(groupId);
    GcGrouperSyncMember gcGrouperSyncMember = this.gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(memberId);
    if (gcGrouperSyncGroup == null || gcGrouperSyncMember == null 
        || gcGrouperSyncGroup.getId() == null || gcGrouperSyncMember.getId() == null) {
      return null;
    }
    GcGrouperSyncMembership gcGrouperSyncMembership = this.membershipRetrieveBySyncGroupIdAndSyncMemberId(
        gcGrouperSyncGroup.getId(), gcGrouperSyncMember.getId());
    return gcGrouperSyncMembership;
  }

  /**
   * select or create grouper sync membership by group uuid and member uuid, note this assumes the syncGroup and syncMember have been retrieved and
   * are in cache.  if they arent there this is an error.  create the groups and members before the membership!
   * @param groupId
   * @param memberId 
   * @return the membership
   */
  public Map<MultiKey, GcGrouperSyncMembership> membershipRetrieveOrCreateByGroupIdsAndMemberIds(Collection<MultiKey> groupIdsAndMemberIds) {
    
    Map<MultiKey, GcGrouperSyncMembership> result = this.membershipRetrieveByGroupIdsAndMemberIds(groupIdsAndMemberIds);
    
    // if done, return
    if (GrouperClientUtils.length(groupIdsAndMemberIds) == 0 || groupIdsAndMemberIds.size() == result.size()) {
      return result;
    }

    Set<MultiKey> groupIdsAndMemberIdsToCreate = new HashSet<MultiKey>(groupIdsAndMemberIds);
    groupIdsAndMemberIdsToCreate.removeAll(result.keySet());
    
    Set<String> groupIds = new HashSet<String>();
    Set<String> memberIds = new HashSet<String>();
    
    // we need to get the sync groups and sync members
    for (MultiKey groupIdAndMemberId : groupIdsAndMemberIds) {
      groupIds.add((String)groupIdAndMemberId.getKey(0));
      memberIds.add((String)groupIdAndMemberId.getKey(1));
    }

    Map<String, GcGrouperSyncGroup> groupIdToSyncGroup = this.gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIds);
    Map<String, GcGrouperSyncMember> memberIdToSyncMember = this.gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIds);

    Set<GcGrouperSyncMembership> syncMembershipsToStore = new HashSet<GcGrouperSyncMembership>();

    // go through and each and see if we have them
    for (MultiKey groupIdAndMemberId : groupIdsAndMemberIdsToCreate) {
      String groupId = (String)groupIdAndMemberId.getKey(0);
      String memberId = (String)groupIdAndMemberId.getKey(1);
      GcGrouperSyncGroup gcGrouperSyncGroup = groupIdToSyncGroup.get(groupId);
      GcGrouperSyncMember gcGrouperSyncMember = memberIdToSyncMember.get(memberId);

      if (gcGrouperSyncGroup == null || gcGrouperSyncGroup.getId() == null) {
        throw new RuntimeException("Cant find group! " + groupId);
      }

      if (gcGrouperSyncMember == null || gcGrouperSyncMember.getId() == null) {
        throw new RuntimeException("Cant find member! " + memberId);
      }
      GcGrouperSyncMembership gcGrouperSyncMembership = 
          this.internal_membershipCreateBySyncGroupIdAndSyncMemberIdHelper(gcGrouperSyncGroup.getId(), gcGrouperSyncMember.getId());
      result.put(groupIdAndMemberId, gcGrouperSyncMembership);
      syncMembershipsToStore.add(gcGrouperSyncMembership);
    }
        
    int changes = this.internal_membershipStore(syncMembershipsToStore);
    this.gcGrouperSync.addObjectCreatedCount(changes);

    for (GcGrouperSyncMembership gcGrouperSyncMembership : syncMembershipsToStore) {
      this.internal_membershipCacheAdd(gcGrouperSyncMembership);
    }
    
    return result;
  }

  /**
   * select or create grouper sync membership by group uuid and member uuid, note this assumes the syncGroup and syncMember have been retrieved and
   * are in cache.  if they arent there this is an error.  create the groups and members before the membership!
   * @param groupId
   * @param memberId 
   * @return the membership
   */
  public GcGrouperSyncMembership membershipRetrieveOrCreateByGroupIdAndMemberId(String groupId, String memberId) {
    GcGrouperSyncGroup gcGrouperSyncGroup = this.gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(groupId);
    GcGrouperSyncMember gcGrouperSyncMember = this.gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(memberId);
    if (gcGrouperSyncGroup == null) {
       throw new RuntimeException("Cant find group by groupId! " + groupId);
    }
    if (gcGrouperSyncMember == null) {
      throw new RuntimeException("Cant find member by memberId! " + groupId);
   }
    GcGrouperSyncMembership gcGrouperSyncMembership = this.membershipRetrieveOrCreateBySyncGroupIdAndSyncMemberId(
        gcGrouperSyncGroup.getId(), gcGrouperSyncMember.getId());
    return gcGrouperSyncMembership;
  }

  /**
   * select grouper sync membership by sync id and membership id
   * @param grouperSyncId
   * @param grouperMembershipIdsCollection
   * @param provisionerName
   * @return the membershipId to syncMembership map
   */
  public Map<MultiKey, GcGrouperSyncMembership> membershipRetrieveBySyncGroupIdsAndSyncMemberIds(
      Collection<MultiKey> syncGroupIdsAndSyncMemberIds) {
  
    Map<MultiKey, GcGrouperSyncMembership> result = new HashMap<MultiKey, GcGrouperSyncMembership>();
    
    Set<MultiKey> membershipIdsToGetFromDb = new HashSet<MultiKey>();
    
    // try from cache
    for (MultiKey syncGroupIdAndSyncMemberId : GrouperClientUtils.nonNull(syncGroupIdsAndSyncMemberIds)) {
      GcGrouperSyncMembership gcGrouperSyncMembership = this.internalCacheSyncMemberships.get(syncGroupIdAndSyncMemberId);
      if (gcGrouperSyncMembership != null) {
        result.put(syncGroupIdAndSyncMemberId, gcGrouperSyncMembership);
      } else {
        membershipIdsToGetFromDb.add(syncGroupIdAndSyncMemberId);
      }
    }
    
    // or else get from db
    if (membershipIdsToGetFromDb.size() > 0) {
      Map<MultiKey, GcGrouperSyncMembership> fromDb = internal_membershipRetrieveFromDbBySyncGroupIdsAndSyncMemberIds(membershipIdsToGetFromDb);
      result.putAll(fromDb);
    }
    
    return result;
    
  }

  /**
   * select grouper sync membership by membership id
   * @param gcGrouperSyncMembershipId
   * @return the membership
   */
  public GcGrouperSyncMembership internal_membershipRetrieveFromCacheById(String gcGrouperSyncMembershipId) {
    GcGrouperSyncMembership gcGrouperSyncMembership = this.internalCacheSyncMembershipsById.get(gcGrouperSyncMembershipId);
    return gcGrouperSyncMembership;
  }

  /**
   * select grouper sync membership by membership id
   * @param gcGrouperSyncMembershipId
   * @return the membership
   */
  public GcGrouperSyncMembership membershipRetrieveById(String gcGrouperSyncMembershipId) {
    GcGrouperSyncMembership gcGrouperSyncMembership = this.internalCacheSyncMembershipsById.get(gcGrouperSyncMembershipId);
    if (gcGrouperSyncMembership == null) {
      gcGrouperSyncMembership = internal_membershipRetrieveFromDbById(gcGrouperSyncMembershipId);
    }
    return gcGrouperSyncMembership;
  }

  /**
   * delete from cache by syncGroupId
   * @param syncGroupId
   */
  public void internal_membershipCacheDeleteBySyncGroupId(String syncGroupId) {

    for (GcGrouperSyncMembership gcGrouperSyncMembership : new HashSet<GcGrouperSyncMembership>(internalCacheSyncMemberships.values())) {
      if (GrouperClientUtils.equals(syncGroupId, gcGrouperSyncMembership.getGrouperSyncGroupId())) {
        internal_membershipCacheDelete(gcGrouperSyncMembership);
      }
    }
        
  }


  /**
   * delete from cache by syncMemberId
   * @param syncMemberId
   */
  public void internal_membershipCacheDeleteBySyncMemberId(String syncMemberId) {

    for (GcGrouperSyncMembership gcGrouperSyncMembership : new HashSet<GcGrouperSyncMembership>(internalCacheSyncMemberships.values())) {
      if (GrouperClientUtils.equals(syncMemberId, gcGrouperSyncMembership.getGrouperSyncMemberId())) {
        internal_membershipCacheDelete(gcGrouperSyncMembership);
      }
    }
        
  }

  /**
   * 
   * @param syncGroupIds
   * @return number deleted
   */
  public int membershipDeleteBySyncGroupIds(Collection<String> syncGroupIds, boolean deleteLogs) {
    int count = 0;
    if (GrouperClientUtils.length(syncGroupIds) > 0) {

      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
          
      for (String syncGroupId : syncGroupIds) {

        this.internal_membershipCacheDeleteBySyncGroupId(syncGroupId);
        
        List<Object> currentBindVarRow = new ArrayList<Object>();
        currentBindVarRow.add(syncGroupId);
        batchBindVars.add(currentBindVarRow);
        
      }
  
      int[] rowDeleteCounts = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
          "delete from grouper_sync_membership where grouper_sync_group_id = ?")
        .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
      
      for (int rowDeleteCount : rowDeleteCounts) {
        count += rowDeleteCount;
      }
      
    }
    return count;

  }
  
  /**
   * 
   * @param syncMemberIds
   * @return number deleted
   */
  public int membershipDeleteBySyncMemberIds(Collection<String> syncMemberIds, boolean deleteLogs) {
    int count = 0;
    if (GrouperClientUtils.length(syncMemberIds) > 0) {

      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
          
      for (String syncMemberId : syncMemberIds) {

        this.internal_membershipCacheDeleteBySyncMemberId(syncMemberId);
        
        List<Object> currentBindVarRow = new ArrayList<Object>();
        currentBindVarRow.add(syncMemberId);
        batchBindVars.add(currentBindVarRow);
        
      }
  
      int[] rowDeleteCounts = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
          "delete from grouper_sync_membership where grouper_sync_member_id = ?")
        .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
      
      for (int rowDeleteCount : rowDeleteCounts) {
        count += rowDeleteCount;
      }
      
    }
    return count;

  }
  
  /**
   * 
   * @param syncGroupId
   * @return number deleted
   */
  public int membershipDeleteBySyncGroupId(String syncGroupId, boolean deleteLogs) {
    this.internal_membershipCacheDeleteBySyncGroupId(syncGroupId);
    return new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_membership where grouper_sync_group_id = ?")
      .bindVars(syncGroupId).executeSql();
  }

  /**
   * 
   * @param syncMemberId
   * @return number deleted
   */
  public int membershipDeleteBySyncMemberId(String syncMemberId, boolean deleteLogs) {
    this.internal_membershipCacheDeleteBySyncMemberId(syncMemberId);
    return new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
        "delete from grouper_sync_membership where grouper_sync_member_id = ?")
      .bindVars(syncMemberId).executeSql();
  }

  /**
   * select grouper sync membership by membership id.  note, this does not store the memberships to the database, do that later
   * @param grouperSyncId
   * @param syncGroupIdsAndSyncMemberIdsCollection
   * @param provisionerName
   * @return the membershipId to syncMembership map
   */
  public Map<MultiKey, GcGrouperSyncMembership> membershipRetrieveOrCreateBySyncGroupIdsAndSyncMemberIds(
      Collection<MultiKey> syncGroupIdsAndSyncMemberIdsCollection) {

    Map<MultiKey, GcGrouperSyncMembership> result = this.membershipRetrieveBySyncGroupIdsAndSyncMemberIds(syncGroupIdsAndSyncMemberIdsCollection);
  
    // if done, return
    if (GrouperClientUtils.length(syncGroupIdsAndSyncMemberIdsCollection) == 0 || syncGroupIdsAndSyncMemberIdsCollection.size() == result.size()) {
      return result;
    }
    
    Set<MultiKey> membershipMultiKeysToCreate = new HashSet<MultiKey>(syncGroupIdsAndSyncMemberIdsCollection);
    membershipMultiKeysToCreate.removeAll(result.keySet());
    
    Set<GcGrouperSyncMembership> syncMembershipsToStore = new HashSet<GcGrouperSyncMembership>();
    
    for (MultiKey membershipIdToCreate : membershipMultiKeysToCreate) {
      GcGrouperSyncMembership gcGrouperSyncMembership = 
          this.internal_membershipCreateBySyncGroupIdAndSyncMemberIdHelper((String)membershipIdToCreate.getKey(0), (String)membershipIdToCreate.getKey(1));
      result.put(membershipIdToCreate, gcGrouperSyncMembership);
      syncMembershipsToStore.add(gcGrouperSyncMembership);
    }
        
    int changes = this.internal_membershipStore(syncMembershipsToStore);
    this.gcGrouperSync.addObjectCreatedCount(changes);

    for (GcGrouperSyncMembership gcGrouperSyncMembership : syncMembershipsToStore) {
      this.internal_membershipCacheAdd(gcGrouperSyncMembership);
    }

    return result;
    
  }

  /**
   * 
   * @param gcGrouperSyncMembershipId
   * @return log
   */
  public GcGrouperSyncLog membershipCreateLog(GcGrouperSyncMembership gcGrouperSyncMembership) {
    return this.gcGrouperSync.getGcGrouperSyncLogDao().logCreateByOwnerId(gcGrouperSyncMembership.getId());
  }

  /**
   * 
   * @param gcGrouperSyncMembership
   */
  private void internal_membershipCacheAdd(GcGrouperSyncMembership gcGrouperSyncMembership) {
    if (gcGrouperSyncMembership.getGrouperSyncGroupId() != null && gcGrouperSyncMembership.getGrouperSyncMemberId() != null) {
      this.internalCacheSyncMemberships.put(
          new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId()), gcGrouperSyncMembership);
    }
    if (gcGrouperSyncMembership.getId() != null) { 
      this.internalCacheSyncMembershipsById.put(gcGrouperSyncMembership.getId(), gcGrouperSyncMembership);
    }
  }

  /**
   * 
   * @param gcGrouperSyncMembership
   */
  public void internal_membershipCacheDelete(GcGrouperSyncMembership gcGrouperSyncMembership) {
    if (gcGrouperSyncMembership.getMembershipId() != null) {
      this.internalCacheSyncMemberships.remove(new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId()));
    }
    if (gcGrouperSyncMembership.getId() != null) {
      this.internalCacheSyncMembershipsById.remove(gcGrouperSyncMembership.getId());
    }
    
  }

  /**
   * select grouper sync membership by sync id and membership id
   * @param grouperSyncId
   * @param provisionerName
   * @return the syncs
   */
  public List<GcGrouperSyncMembership> internal_membershipRetrieveFromDbAll() {
    
    // clear the cache
    this.internalCacheSyncMemberships.clear();
    this.internalCacheSyncMembershipsById.clear();
    
    List<GcGrouperSyncMembership> gcGrouperSyncMembershipList = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_membership where grouper_sync_id = ?").addBindVar(this.getGcGrouperSync().getId()).selectList(GcGrouperSyncMembership.class);
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMembershipList) {
      gcGrouperSyncMembership.setGrouperSync(this.getGcGrouperSync());
      this.internal_membershipCacheAdd(gcGrouperSyncMembership);
  
    }
    return gcGrouperSyncMembershipList;
  }

  /**
   * select grouper sync membership by membership id
   * @param connectionName
   * @param syncGroupId
   * @param syncMemberId
   * @return the membership
   */
  public GcGrouperSyncMembership internal_membershipRetrieveFromDbBySyncGroupIdAndSyncMemberId(String syncGroupId, String syncMemberId) {
    
    GcGrouperSyncMembership gcGrouperSyncMembership = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_membership where grouper_sync_group_id = ? and grouper_sync_member_id = ?")
          .addBindVar(syncGroupId).addBindVar(syncMemberId).select(GcGrouperSyncMembership.class);
    if (gcGrouperSyncMembership != null) {
      gcGrouperSyncMembership.setGrouperSync(this.getGcGrouperSync());
      this.internal_membershipCacheAdd(gcGrouperSyncMembership);
    }
    return gcGrouperSyncMembership;
    
  }

  /**
   * select grouper sync membership by sync id and membership id
   * @param grouperSyncId
   * @param grouperMembershipIdsCollection
   * @param provisionerName
   * @return the membershipId to syncMembership map
   */
  public Map<MultiKey, GcGrouperSyncMembership> internal_membershipRetrieveFromDbBySyncGroupIdsAndSyncMemberIds(Collection<MultiKey> syncGroupIdsAndSyncMemberIds) {
    
    Map<MultiKey, GcGrouperSyncMembership> result = new HashMap<MultiKey, GcGrouperSyncMembership>();
    
    if (GrouperClientUtils.length(syncGroupIdsAndSyncMemberIds) == 0) {
      return result;
    }
    
    List<MultiKey> syncGroupIdsAndSyncMemberIdsList = new ArrayList<MultiKey>(syncGroupIdsAndSyncMemberIds);
    
    // two bind vars in each record to retrieve
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect() / 2;
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(syncGroupIdsAndSyncMemberIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<MultiKey> batchOfMembershipIds = GrouperClientUtils.batchList(syncGroupIdsAndSyncMemberIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select * from grouper_sync_membership where ");
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName());
          
      for (int i=0;i<batchOfMembershipIds.size();i++) {
        if (i>0) {
          sql.append(" or ");
        }
        sql.append(" ( grouper_sync_group_id = ? and grouper_sync_member_id = ? ) ");
        MultiKey syncGroupIdAndSyncMemberId = batchOfMembershipIds.get(i);
        gcDbAccess.addBindVar(syncGroupIdAndSyncMemberId.getKey(0));
        gcDbAccess.addBindVar(syncGroupIdAndSyncMemberId.getKey(1));
      }
      
      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcDbAccess.sql(sql.toString()).selectList(GcGrouperSyncMembership.class);
      
      for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperClientUtils.nonNull(gcGrouperSyncMemberships)) {
        result.put(new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId()), gcGrouperSyncMembership);
        gcGrouperSyncMembership.setGrouperSync(this.getGcGrouperSync());
        this.internal_membershipCacheAdd(gcGrouperSyncMembership);
      }
      
    }
    return result;
  }

  /**
   * select grouper sync membership by group sync ids
   * @param syncGroupIds
   * @return the syncMemberships
   */
  public List<GcGrouperSyncMembership> internal_membershipRetrieveFromDbBySyncGroupIds(Collection<String> syncGroupIds) {
    
    List<GcGrouperSyncMembership> result = new ArrayList<GcGrouperSyncMembership>();
    
    if (GrouperClientUtils.length(syncGroupIds) == 0) {
      return result;
    }
    
    List<String> syncGroupIdsList = new ArrayList<String>(syncGroupIds);
    
    // two bind vars in each record to retrieve
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(syncGroupIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfGroupIds = GrouperClientUtils.batchList(syncGroupIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_membership where grouper_sync_group_id in ( " + GrouperClientUtils.appendQuestions(batchOfGroupIds.size()) + ")";

      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName());

      for (String syncGroupId : batchOfGroupIds) {
        gcDbAccess.addBindVar(syncGroupId);
      }
      
      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcDbAccess.sql(sql).selectList(GcGrouperSyncMembership.class);
      
      for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperClientUtils.nonNull(gcGrouperSyncMemberships)) {
        result.add(gcGrouperSyncMembership);
        gcGrouperSyncMembership.setGrouperSync(this.getGcGrouperSync());
        this.internal_membershipCacheAdd(gcGrouperSyncMembership);
      }
      
    }
    return result;
  }

  /**
   * select grouper sync membership by member sync ids
   * @param syncMemberIds
   * @return the syncMemberships
   */
  public List<GcGrouperSyncMembership> internal_membershipRetrieveFromDbBySyncMemberIds(Collection<String> syncMemberIds) {
    
    List<GcGrouperSyncMembership> result = new ArrayList<GcGrouperSyncMembership>();
    
    if (GrouperClientUtils.length(syncMemberIds) == 0) {
      return result;
    }
    
    List<String> syncMemberIdsList = new ArrayList<String>(syncMemberIds);
    
    // two bind vars in each record to retrieve
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(syncMemberIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfMemberIds = GrouperClientUtils.batchList(syncMemberIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_membership where grouper_sync_member_id in ( " + GrouperClientUtils.appendQuestions(batchOfMemberIds.size()) + ")";

      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName());

      for (String syncMemberId : batchOfMemberIds) {
        gcDbAccess.addBindVar(syncMemberId);
      }
      
      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcDbAccess.sql(sql).selectList(GcGrouperSyncMembership.class);
      
      for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperClientUtils.nonNull(gcGrouperSyncMemberships)) {
        result.add(gcGrouperSyncMembership);
        gcGrouperSyncMembership.setGrouperSync(this.getGcGrouperSync());
        this.internal_membershipCacheAdd(gcGrouperSyncMembership);
      }
      
    }
    return result;
  }

  /**
   * select grouper sync membership by gcGrouperSyncMembershipId id
   * @param gcGrouperSyncMembershipId
   * @return the gcGrouperSyncMembership
   */
  public GcGrouperSyncMembership internal_membershipRetrieveFromDbById(String gcGrouperSyncMembershipId) {
    
    GcGrouperSyncMembership gcGrouperSyncMembership = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_membership where id = ?")
          .addBindVar(gcGrouperSyncMembershipId).select(GcGrouperSyncMembership.class);
    if (gcGrouperSyncMembership != null) {
      gcGrouperSyncMembership.setGrouperSync(this.getGcGrouperSync());
      this.internal_membershipCacheAdd(gcGrouperSyncMembership);
    }
    return gcGrouperSyncMembership;
    
  }

  /**
   * select grouper sync membership by sync id and membership id
   * @param grouperSyncId
   * @param syncMembershipIdsCollection
   * @param provisionerName
   * @return the id to syncMembership map
   */
  public Map<String, GcGrouperSyncMembership> internal_membershipRetrieveFromDbByIds(Collection<String> syncMembershipIdsCollection) {
    
    Map<String, GcGrouperSyncMembership> result = new HashMap<String, GcGrouperSyncMembership>();
    
    if (GrouperClientUtils.length(syncMembershipIdsCollection) == 0) {
      return result;
    }
    
    List<String> syncIdsList = new ArrayList<String>(syncMembershipIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(syncIdsList, batchSize);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfSyncIds = GrouperClientUtils.batchList(syncIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_membership where grouper_sync_id = ? and id in ( " 
          + GrouperClientUtils.appendQuestions(batchOfSyncIds.size()) + ")";
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql(sql).addBindVar(this.getGcGrouperSync().getId());
      for (String membershipId : batchOfSyncIds) {
        gcDbAccess.addBindVar(membershipId);
      }
      
      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcDbAccess.selectList(GcGrouperSyncMembership.class);
      
      for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperClientUtils.nonNull(gcGrouperSyncMemberships)) {
        result.put(gcGrouperSyncMembership.getId(), gcGrouperSyncMembership);
        gcGrouperSyncMembership.setGrouperSync(this.getGcGrouperSync());
        this.internal_membershipCacheAdd(gcGrouperSyncMembership);
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
  private boolean membershipRetrievedAllObjectsFromDb = false;
  
  
  
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
   * @return number of memberships stored
   */
  public int internal_membershipStoreAll() {
    return this.internal_membershipStore(this.internalCacheSyncMemberships.values());
  }
  
  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncMemberships
   * @return number of changes
   */
  public int internal_membershipStore(Collection<GcGrouperSyncMembership> gcGrouperSyncMemberships) {
  
    if (GrouperClientUtils.length(gcGrouperSyncMemberships) == 0) {
      return 0;
    }
  
    int batchSize = this.getGcGrouperSync().batchSize();
  
    List<GcGrouperSyncMembership> gcGrouperSyncMembershipsList = new ArrayList<GcGrouperSyncMembership>(gcGrouperSyncMemberships);
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperClientUtils.nonNull(gcGrouperSyncMemberships)) {
      gcGrouperSyncMembership.storePrepare();
    }
  
    int changes = -1;
    
    try {
      changes = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeBatchToDatabase(gcGrouperSyncMembershipsList, batchSize);
    } catch (RuntimeException re) {
      throw re;
    }
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperClientUtils.nonNull(gcGrouperSyncMemberships)) {
      this.internal_membershipCacheAdd(gcGrouperSyncMembership);
    }
    return changes;
  }

  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncMemberships
   */
  public void internal_membershipStore(GcGrouperSyncMembership gcGrouperSyncMembership) {
  
    gcGrouperSyncMembership.storePrepare();
  
    new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeToDatabase(gcGrouperSyncMembership);
  
  }

  /**
   * select grouper sync membership by membership id.  Note: this doesnt store to db yet, you do that at the end
   * @param connectionName
   * @param syncGroupId
   * @param syncMemberId
   * @return the membership
   */
  public GcGrouperSyncMembership membershipRetrieveOrCreateBySyncGroupIdAndSyncMemberId(String syncGroupId, String syncMemberId) {
    GcGrouperSyncMembership gcGrouperSyncMembership = this.membershipRetrieveBySyncGroupIdAndSyncMemberId(syncGroupId, syncMemberId);
    if (gcGrouperSyncMembership == null) {
      gcGrouperSyncMembership = this.membershipCreateBySyncGroupIdAndSyncMemberId(syncGroupId, syncMemberId);
    }
    return gcGrouperSyncMembership;
  }

  /**
   * select grouper syncs membership by group uuids and member uuids, note this assumes the syncGroup and syncMember exist
   * @param groupId
   * @param memberId 
   * @return the membership
   */
  public Map<MultiKey, GcGrouperSyncMembership> membershipRetrieveByGroupIdsAndMemberIds(Collection<MultiKey> groupIdsAndMemberIds) {
    
    Set<String> groupIds = new HashSet<String>();
    Set<String> memberIds = new HashSet<String>();
    
    if (GrouperClientUtils.length(groupIdsAndMemberIds) == 0) {
      return new HashMap<MultiKey, GcGrouperSyncMembership>();
    }

    // we need to get the sync groups and sync members
    for (MultiKey groupIdAndMemberId : groupIdsAndMemberIds) {
      groupIds.add((String)groupIdAndMemberId.getKey(0));
      memberIds.add((String)groupIdAndMemberId.getKey(1));
    }

    Map<String, GcGrouperSyncGroup> groupIdToSyncGroup = this.gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIds);
    Map<String, GcGrouperSyncMember> memberIdToSyncMember = this.gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIds);

    Set<MultiKey> syncGroupIdsSyncMemberIds = new HashSet<MultiKey>();
    
    Map<MultiKey, MultiKey> syncGroupSyncMemberToGroupMemberIds = new HashMap<MultiKey, MultiKey>();
    
    // go through and each and see if we have them
    for (MultiKey groupIdAndMemberId : groupIdsAndMemberIds) {
      String groupId = (String)groupIdAndMemberId.getKey(0);
      String memberId = (String)groupIdAndMemberId.getKey(1);
      GcGrouperSyncGroup gcGrouperSyncGroup = groupIdToSyncGroup.get(groupId);
      GcGrouperSyncMember gcGrouperSyncMember = memberIdToSyncMember.get(memberId);

      if (gcGrouperSyncGroup == null || gcGrouperSyncMember == null 
          || gcGrouperSyncGroup.getId() == null || gcGrouperSyncMember.getId() == null) {
        continue;
      }
      MultiKey groupSyncIdMemberSyncId = new MultiKey(gcGrouperSyncGroup.getId(), gcGrouperSyncMember.getId());
      syncGroupIdsSyncMemberIds.add(groupSyncIdMemberSyncId);
      syncGroupSyncMemberToGroupMemberIds.put(groupSyncIdMemberSyncId, groupIdAndMemberId);
    }

    Map<MultiKey, GcGrouperSyncMembership> results = new HashMap<MultiKey, GcGrouperSyncMembership>();
    
    Set<MultiKey> syncGroupIdsSyncMemberIdsToGetFromDb = new HashSet<MultiKey>();
    
    // try from cache
    for (MultiKey syncGroupIdSyncMemberId : GrouperClientUtils.nonNull(syncGroupIdsSyncMemberIds)) {
      GcGrouperSyncMembership gcGrouperSyncMembership = this.internalCacheSyncMemberships.get(syncGroupIdSyncMemberId);
      if (gcGrouperSyncMembership != null) {
        MultiKey groupIdMemberId = syncGroupSyncMemberToGroupMemberIds.get(syncGroupIdSyncMemberId);
        results.put(groupIdMemberId, gcGrouperSyncMembership);
      } else {
        syncGroupIdsSyncMemberIdsToGetFromDb.add(syncGroupIdSyncMemberId);
      }
    }
    
    // or else get from db
    if (syncGroupIdsSyncMemberIdsToGetFromDb.size() > 0) {
      Map<MultiKey, GcGrouperSyncMembership> syncGroupIdSyncMemberIdToMembershipFromDb = internal_membershipRetrieveFromDbBySyncGroupIdsAndSyncMemberIds(syncGroupIdsSyncMemberIdsToGetFromDb);
      for (MultiKey syncGroupIdSyncMemberId : (GrouperClientUtils.nonNull(syncGroupIdSyncMemberIdToMembershipFromDb).keySet())) {
        MultiKey groupIdMemberId = syncGroupSyncMemberToGroupMemberIds.get(syncGroupIdSyncMemberId);
        results.put(groupIdMemberId, syncGroupIdSyncMemberIdToMembershipFromDb.get(syncGroupIdSyncMemberId));
      }
    }
    
    return results;

  }

  public List<GcGrouperSyncMembership> membershipRetrieveByGroupIds(Set<String> groupIdsToRetrieveMemberships) {
    Set<String> groupIds = new HashSet<String>();
    
    List<GcGrouperSyncMembership> results = new ArrayList<GcGrouperSyncMembership>();
    
    if (GrouperClientUtils.length(groupIdsToRetrieveMemberships) == 0) {
      return results;
    }

    Map<String, GcGrouperSyncGroup> groupIdToSyncGroup = this.gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIds);

    Set<String> groupSyncIds = new HashSet<String>();
    for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperClientUtils.nonNull(groupIdToSyncGroup).values()) {
      groupSyncIds.add(gcGrouperSyncGroup.getId());
    }
    
    return this.internal_membershipRetrieveFromDbBySyncGroupIds(groupSyncIds);
        
  }

  public List<GcGrouperSyncMembership> membershipRetrieveByMemberIds(Set<String> memberIdsToRetrieveMemberships) {
    Set<String> groupIds = new HashSet<String>();
    
    List<GcGrouperSyncMembership> results = new ArrayList<GcGrouperSyncMembership>();
    
    if (GrouperClientUtils.length(memberIdsToRetrieveMemberships) == 0) {
      return results;
    }

    Map<String, GcGrouperSyncMember> memberSyncIdToSyncMember = this.gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(groupIds);

    Set<String> memberSyncIds = new HashSet<String>();
    for (GcGrouperSyncMember gcGrouperSyncMember : GrouperClientUtils.nonNull(memberSyncIdToSyncMember).values()) {
      memberSyncIds.add(gcGrouperSyncMember.getId());
    }
    
    return this.internal_membershipRetrieveFromDbBySyncMemberIds(memberSyncIds);
        
  }

  /**
   * get membership ids with errors after error timestamp
   * @param errorTimestampCheckFrom if null get all
   * @return group ids and member ids
   */
  public List<Object[]> retrieveGroupIdMemberIdsWithErrorsAfterMillis(Timestamp errorTimestampCheckFrom) {
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select gsg.group_id, gsm.member_id from grouper_sync_membership gsms, grouper_sync_group gsg, grouper_sync_member gsm "
            + "where gsms.grouper_sync_id = ? and gsms.grouper_sync_group_id = gsg.id and gsms.grouper_sync_member_id = gsm.id" 
            + (errorTimestampCheckFrom == null ? " and gsms.error_timestamp is not null" : " and gsms.error_timestamp >= ?"))
        .addBindVar(this.getGcGrouperSync().getId());
    if (errorTimestampCheckFrom != null) {
      gcDbAccess.addBindVar(errorTimestampCheckFrom);
    }
    List<Object[]> groupIdMemberIds = gcDbAccess.selectList(Object[].class);
    return groupIdMemberIds;
  }
  
  /**
   * get count of rows per error code
   * @return
   */
  public Map<String, Integer> retrieveErrorCountByCode() {
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select error_code, count(*) from grouper_sync_membership where grouper_sync_id = ? and error_code is not null group by error_code")
        .addBindVar(this.getGcGrouperSync().getId());
    Map<String, Integer> errorCount = gcDbAccess.selectMapMultipleRows(String.class, Integer.class);
    return errorCount;
  }

}
