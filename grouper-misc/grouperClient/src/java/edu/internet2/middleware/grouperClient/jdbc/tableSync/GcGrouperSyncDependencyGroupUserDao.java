package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * note this has a foreign key on grouper_sync so you dont have to delete them when deleting a group sync
 * @author mchyzer
 *
 */
public class GcGrouperSyncDependencyGroupUserDao {

  /**
   * keep an internal cache of user deps by group id and field id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<MultiKey, GcGrouperSyncDependencyGroupUser> internalCacheSyncUserDepsByGroupIdFieldId = new HashMap<MultiKey, GcGrouperSyncDependencyGroupUser>();

  /**
   * keep an internal cache of user deps by group id and field id which are not in the database
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Set<MultiKey> internalCacheSyncUserDepsByGroupIdFieldIdNotFound = new HashSet<MultiKey>();


  public GcGrouperSyncDependencyGroupUserDao() {
  }

  /**
   * delete stuff with batches by id indexes
   * @param idIndexes
   * @return the number of records deleted
   */
  public int internal_dependencyGroupUserDeleteBatchByIdIndexes(Collection<Long> idIndexes) {
  
    int count = 0;
    if (GrouperClientUtils.length(idIndexes) > 0) {

      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
          
      for (Long idIndex : idIndexes) {
        List<Object> currentBindVarRow = new ArrayList<Object>();
        currentBindVarRow.add(idIndex);
        batchBindVars.add(currentBindVarRow);
        
      }
  
      int[] rowDeleteCounts = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
          "delete from grouper_sync_dep_group_user where id_index = ?")
        .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
      
      for (int rowDeleteCount : rowDeleteCounts) {
        count += rowDeleteCount;
      }
      
      for (GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser : new ArrayList<GcGrouperSyncDependencyGroupUser>(internalCacheSyncUserDepsByGroupIdFieldId.values())) {
        if (idIndexes.contains(gcGrouperSyncDependencyGroupUser.getIdIndex())) {
          MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupUser.getGroupId(), gcGrouperSyncDependencyGroupUser.getFieldId());
          internalCacheSyncUserDepsByGroupIdFieldId.remove(groupIdFieldId);
          internalCacheSyncUserDepsByGroupIdFieldIdNotFound.add(groupIdFieldId);
        }
      }
      
    }
    return count;
  }

  public static int internalTestingRetrieveAllCount = 0;
  
  public static int internalTestingStoreCount = 0;

  /**
   * select grouper sync dependency group users by sync id
   * @return the dependencies
   */
  public List<GcGrouperSyncDependencyGroupUser> internal_dependencyGroupUserRetrieveFromDbAll() {
    internalTestingRetrieveAllCount++;
    List<GcGrouperSyncDependencyGroupUser> gcGrouperSyncDependencyGroupUserList = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_dep_group_user where grouper_sync_id = ?").addBindVar(this.getGcGrouperSync().getId()).selectList(GcGrouperSyncDependencyGroupUser.class);
    
    for (GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser : gcGrouperSyncDependencyGroupUserList) {
      MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupUser.getGroupId(), gcGrouperSyncDependencyGroupUser.getFieldId());
      internalCacheSyncUserDepsByGroupIdFieldId.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
      internalCacheSyncUserDepsByGroupIdFieldIdNotFound.remove(groupIdFieldId);
    }

    return gcGrouperSyncDependencyGroupUserList;
  }

  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncDependencyGroupUser
   */
  public void internal_dependencyGroupUserStore(GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser) {
  
    gcGrouperSyncDependencyGroupUser.storePrepare();
    gcGrouperSyncDependencyGroupUser.setGrouperSync(this.gcGrouperSync);
    
    new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeToDatabase(gcGrouperSyncDependencyGroupUser);
    
    MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupUser.getGroupId(), gcGrouperSyncDependencyGroupUser.getFieldId());
    internalCacheSyncUserDepsByGroupIdFieldId.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
    internalCacheSyncUserDepsByGroupIdFieldIdNotFound.remove(groupIdFieldId);

  
  }

  /**
   * delete all for this provisioner
   * @return rows deleted
   */
  public int deleteAll() {

    List<GcGrouperSyncDependencyGroupUser> internal_dependencyGroupUserRetrieveFromDbAll = internal_dependencyGroupUserRetrieveFromDbAll();
    
    if (GrouperClientUtils.length(internal_dependencyGroupUserRetrieveFromDbAll) == 0) {
      return 0;
    }
    
    Set<Long> depIds = new HashSet<Long>();
    
    for (GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser : internal_dependencyGroupUserRetrieveFromDbAll) {
      
      depIds.add(gcGrouperSyncDependencyGroupUser.getIdIndex());
      
    }
  
    internal_dependencyGroupUserDeleteBatchByIdIndexes(depIds);
    
    return depIds.size();
  
  }

  public static int internalTestingRetrieveByGroupIdFieldIdCount = 0;

  /**
   * select grouper sync dependency group users by provisionable group ids
   * @param groupIdsFieldIdsCollection
   * @return the provisionableGroupId to syncDependencyGroupUsers map
   */
  public Map<MultiKey, GcGrouperSyncDependencyGroupUser> dependencyGroupUserRetrieveFromDbOrCacheByGroupIdsFieldIds(Collection<MultiKey> groupIdsFieldIdsCollection) {
    
    Map<MultiKey, GcGrouperSyncDependencyGroupUser> result = new HashMap<>();
    
    if (GrouperClientUtils.length(groupIdsFieldIdsCollection) == 0) {
      return result;
    }

    Set<MultiKey> groupIdsFieldIdsSet = new HashSet<MultiKey>(groupIdsFieldIdsCollection);
    Iterator<MultiKey> groupIdFieldIdIterator = groupIdsFieldIdsSet.iterator();
    while (groupIdFieldIdIterator.hasNext()) {
      MultiKey groupIdFieldId = groupIdFieldIdIterator.next();
      if (internalCacheSyncUserDepsByGroupIdFieldId.containsKey(groupIdFieldId)) {
        result.put(groupIdFieldId, internalCacheSyncUserDepsByGroupIdFieldId.get(groupIdFieldId));
        groupIdFieldIdIterator.remove();
      } else if (internalCacheSyncUserDepsByGroupIdFieldIdNotFound.contains(groupIdFieldId)) {
        groupIdFieldIdIterator.remove();
      }
    }

    if (groupIdsFieldIdsSet.size() > 0) {
      result.putAll(internal_dependencyGroupUserRetrieveFromDbByGroupIdsFieldIds(groupIdsFieldIdsSet));
    }
    return result;
    
  }

  /**
   * select grouper sync dependency group users by provisionable group ids
   * @param groupIdsFieldIdsCollection
   * @return the provisionableGroupId to syncDependencyGroupUsers map
   */
  public Map<MultiKey, GcGrouperSyncDependencyGroupUser> internal_dependencyGroupUserRetrieveFromDbByGroupIdsFieldIds(Collection<MultiKey> groupIdsFieldIdsCollection) {
    
    internalTestingRetrieveByGroupIdFieldIdCount++;
    
    Map<MultiKey, GcGrouperSyncDependencyGroupUser> result = new HashMap<>();
    
    if (GrouperClientUtils.length(groupIdsFieldIdsCollection) == 0) {
      return result;
    }

    List<MultiKey> groupIdsFieldIdsList = new ArrayList<MultiKey>(groupIdsFieldIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect() / 2;
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupIdsFieldIdsList, batchSize, false);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<MultiKey> batchOfGroupIdsFieldIds = GrouperClientUtils.batchList(groupIdsFieldIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select * from grouper_sync_dep_group_user where ");
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName());
      
      boolean first = true;
      for (MultiKey groupIdFieldId : batchOfGroupIdsFieldIds) {
        String groupId = (String)groupIdFieldId.getKey(0);
        String fieldId = (String)groupIdFieldId.getKey(1);
        if (!first) {
          sql.append(" or ");
        }
        
        sql.append(" ( group_id = ? and field_id = ? ) ");
        gcDbAccess.addBindVar(groupId);
        gcDbAccess.addBindVar(fieldId);
        first = false;
      }
      
      List<GcGrouperSyncDependencyGroupUser> gcGrouperSyncDependencyGroupUsers = gcDbAccess.sql(sql.toString()).selectList(GcGrouperSyncDependencyGroupUser.class);
      
      for (GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser : GrouperClientUtils.nonNull(gcGrouperSyncDependencyGroupUsers)) {
        MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupUser.getGroupId(), gcGrouperSyncDependencyGroupUser.getFieldId());
        result.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
      }
        
    }
    for (MultiKey groupIdFieldId : groupIdsFieldIdsCollection) {
      GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser = result.get(groupIdFieldId);
      if (gcGrouperSyncDependencyGroupUser == null) {
        internalCacheSyncUserDepsByGroupIdFieldIdNotFound.add(groupIdFieldId);
      } else {
        internalCacheSyncUserDepsByGroupIdFieldId.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
      }
    }
    return result;
  }

  /**
   * 
   */
  private GcGrouperSync gcGrouperSync;
  
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

//  /**
//   * select grouper sync dependency group users by group ids
//   * @param groupIdsCollection
//   * @return the provisionableGroupId to syncDependencyGroupUsers set
//   */
//  public Map<String, Set<GcGrouperSyncDependencyGroupUser>> internal_dependencyGroupUserRetrieveFromDbByGroupIds(Collection<String> groupIdsCollection) {
//    
//    Map<String, Set<GcGrouperSyncDependencyGroupUser>> result = new HashMap<>();
//    
//    if (GrouperClientUtils.length(groupIdsCollection) == 0) {
//      return result;
//    }
//    
//    List<String> groupIdsList = new ArrayList<String>(groupIdsCollection);
//    
//    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect() / 2;
//    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupIdsCollection, batchSize, false);
//    
//    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
//      
//      List<String> batchOfGroupIds = GrouperClientUtils.batchList(groupIdsList, batchSize, batchIndex);
//      
//      String sql = "select * from grouper_sync_dep_group_user where group_id in ( " 
//          + GrouperClientUtils.appendQuestions(batchOfGroupIds.size()) + ")";
//      
//      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
//          .sql(sql);
//      for (String groupId : batchOfGroupIds) {
//        gcDbAccess.addBindVar(groupId);
//      }
//
//      List<GcGrouperSyncDependencyGroupUser> gcGrouperSyncDependencyGroupUsers = gcDbAccess.selectList(GcGrouperSyncDependencyGroupUser.class);
//      
//      for (GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser : GrouperClientUtils.nonNull(gcGrouperSyncDependencyGroupUsers)) {
//        
//        Set<GcGrouperSyncDependencyGroupUser> dependencies = result.get(gcGrouperSyncDependencyGroupUser.getGroupId());
//        if (dependencies == null) {
//          dependencies = new HashSet<>();
//          result.put(gcGrouperSyncDependencyGroupUser.getGroupId(), dependencies);
//        }
//        
//        dependencies.add(gcGrouperSyncDependencyGroupUser);
//
//      }
//        
//    }
//    return result;
//  }

}
