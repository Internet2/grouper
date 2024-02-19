package edu.internet2.middleware.grouperClient.jdbc.tableSync;

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
 * note this has a foreign key on grouper_sync so you dont have to delete them when deleting a group sync
 * @author mchyzer
 *
 */
public class GcGrouperSyncDependencyGroupGroupDao {

  /**
   * keep an internal cache of group deps by group id and field id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<MultiKey, Set<GcGrouperSyncDependencyGroupGroup>> internalCacheSyncGroupDepsByGroupIdFieldId = new HashMap<MultiKey, Set<GcGrouperSyncDependencyGroupGroup>>();

  /**
   * keep an internal cache of group deps by group id and field id
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Map<String, Set<GcGrouperSyncDependencyGroupGroup>> internalCacheSyncGroupDepsByProvisionableGroupId = new HashMap<String, Set<GcGrouperSyncDependencyGroupGroup>>();

  /**
   * keep an internal cache of group deps by group id and field id which are not in the database
   */
  @GcPersistableField(persist = GcPersist.dontPersist)
  private Set<MultiKey> internalCacheSyncGroupDepsByGroupIdFieldIdNotFound = new HashSet<MultiKey>();


  public GcGrouperSyncDependencyGroupGroupDao() {
  }

  /**
   * delete stuff with batches by id indexes
   * @param idIndexes
   * @return the number of records deleted
   */
  public int internal_dependencyGroupGroupDeleteBatchByIdIndexes(Collection<Long> idIndexes) {
  
    int count = 0;
    if (GrouperClientUtils.length(idIndexes) > 0) {

      List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
          
      for (Long idIndex : idIndexes) {
        List<Object> currentBindVarRow = new ArrayList<Object>();
        currentBindVarRow.add(idIndex);
        batchBindVars.add(currentBindVarRow);
        
      }
  
      int[] rowDeleteCounts = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).sql(
          "delete from grouper_sync_dep_group_group where id_index = ?")
        .batchBindVars(batchBindVars).batchSize(this.getGcGrouperSync().batchSize()).executeBatchSql();
      
      for (int rowDeleteCount : rowDeleteCounts) {
        count += rowDeleteCount;
      }

      // TODO
//      for (GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup : new ArrayList<GcGrouperSyncDependencyGroupGroup>(internalCacheSyncGroupDepsByGroupIdFieldId.values())) {
//        if (idIndexes.contains(gcGrouperSyncDependencyGroupGroup.getIdIndex())) {
//          MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupGroup.getGroupId(), gcGrouperSyncDependencyGroupGroup.getFieldId());
//          internalCacheSyncGroupDepsByGroupIdFieldId.remove(groupIdFieldId);
//          internalCacheSyncGroupDepsByGroupIdFieldIdNotFound.add(groupIdFieldId);
//        }
//      }

    }
    return count;
  }

  /**
   * store batch, generally call this from store all objects from GcGrouperSync
   * @param gcGrouperSyncDependencyGroupGroup
   */
  public void internal_dependencyGroupGroupStore(GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup) {
  
    internalTestingStoreCount++;
    
    gcGrouperSyncDependencyGroupGroup.storePrepare();
    gcGrouperSyncDependencyGroupGroup.setGrouperSync(this.gcGrouperSync);
    
    new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName()).storeToDatabase(gcGrouperSyncDependencyGroupGroup);

    // TODO
//    MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupUser.getGroupId(), gcGrouperSyncDependencyGroupUser.getFieldId());
//    internalCacheSyncUserDepsByGroupIdFieldId.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
//    internalCacheSyncUserDepsByGroupIdFieldIdNotFound.remove(groupIdFieldId);
//

  
  }
  
//  /**
//   * select grouper sync dependency group users by provisionable group ids
//   * @param groupIdsFieldIdsCollection
//   * @return the provisionableGroupId to syncDependencyGroupUsers map
//   */
//  public Map<MultiKey, GcGrouperSyncDependencyGroupUser> dependencyGroupUserRetrieveFromDbOrCacheByGroupIdsFieldIds(Collection<MultiKey> groupIdsFieldIdsCollection) {
//    
//    Map<MultiKey, GcGrouperSyncDependencyGroupUser> result = new HashMap<>();
//    
//    if (GrouperClientUtils.length(groupIdsFieldIdsCollection) == 0) {
//      return result;
//    }
//
//    Set<MultiKey> groupIdsFieldIdsSet = new HashSet<MultiKey>(groupIdsFieldIdsCollection);
//    Iterator<MultiKey> groupIdFieldIdIterator = groupIdsFieldIdsSet.iterator();
//    while (groupIdFieldIdIterator.hasNext()) {
//      MultiKey groupIdFieldId = groupIdFieldIdIterator.next();
//      if (internalCacheSyncUserDepsByGroupIdFieldId.containsKey(groupIdFieldId)) {
//        result.put(groupIdFieldId, internalCacheSyncUserDepsByGroupIdFieldId.get(groupIdFieldId));
//        groupIdFieldIdIterator.remove();
//      } else if (internalCacheSyncUserDepsByGroupIdFieldIdNotFound.contains(groupIdFieldId)) {
//        groupIdFieldIdIterator.remove();
//      }
//    }
//
//    if (groupIdsFieldIdsSet.size() > 0) {
//      result.putAll(internal_dependencyGroupUserRetrieveFromDbByGroupIdsFieldIds(groupIdsFieldIdsSet));
//    }
//    return result;
//    
//  }


  /**
   * select grouper sync dependency group groups by sync id
   * @return the dependencies
   */
  public List<GcGrouperSyncDependencyGroupGroup> internal_dependencyGroupGroupRetrieveFromDbAll() {
    internalTestingRetrieveAllCount++;
    List<GcGrouperSyncDependencyGroupGroup> gcGrouperSyncDependencyGroupGroupList = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
        .sql("select * from grouper_sync_dep_group_group where grouper_sync_id = ?").addBindVar(this.getGcGrouperSync().getId()).selectList(GcGrouperSyncDependencyGroupGroup.class);

    // TODO
//    for (GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser : gcGrouperSyncDependencyGroupUserList) {
//      MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupUser.getGroupId(), gcGrouperSyncDependencyGroupUser.getFieldId());
//      internalCacheSyncUserDepsByGroupIdFieldId.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
//      internalCacheSyncUserDepsByGroupIdFieldIdNotFound.remove(groupIdFieldId);
//    }

    return gcGrouperSyncDependencyGroupGroupList;
  }

  /**
   * select grouper sync dependency group groups by provisionable group ids
   * @param provisionableGroupIdsCollection
   * @return the provisionableGroupId to syncDependencyGroupGroups map
   */
  public Map<String, Set<GcGrouperSyncDependencyGroupGroup>> internal_dependencyGroupGroupRetrieveFromDbByProvisionableGroupIds(Collection<String> provisionableGroupIdsCollection) {
    
    Map<String, Set<GcGrouperSyncDependencyGroupGroup>> result = new HashMap<>();
    
    if (GrouperClientUtils.length(provisionableGroupIdsCollection) == 0) {
      return result;
    }
    
    List<String> provisionableGroupIdsList = new ArrayList<String>(provisionableGroupIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(provisionableGroupIdsList, batchSize, false);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<String> batchOfProvisionableGroupIds = GrouperClientUtils.batchList(provisionableGroupIdsList, batchSize, batchIndex);
      
      String sql = "select * from grouper_sync_dep_group_group where provisionable_group_id in ( " 
          + GrouperClientUtils.appendQuestions(batchOfProvisionableGroupIds.size()) + ")";
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
          .sql(sql);
      for (String provisionableGroupId : batchOfProvisionableGroupIds) {
        gcDbAccess.addBindVar(provisionableGroupId);
      }
      
      List<GcGrouperSyncDependencyGroupGroup> gcGrouperSyncDependencyGroupGroups = gcDbAccess.selectList(GcGrouperSyncDependencyGroupGroup.class);
      
      for (GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup : GrouperClientUtils.nonNull(gcGrouperSyncDependencyGroupGroups)) {
        
        Set<GcGrouperSyncDependencyGroupGroup> dependencies = result.get(gcGrouperSyncDependencyGroupGroup.getProvisionableGroupId());
        if (dependencies == null) {
          dependencies = new HashSet<>();
          result.put(gcGrouperSyncDependencyGroupGroup.getProvisionableGroupId(), dependencies);
        }
        
        dependencies.add(gcGrouperSyncDependencyGroupGroup);
      }
        
    }
    // TODO
//    for (MultiKey groupIdFieldId : groupIdsFieldIdsCollection) {
//      GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser = result.get(groupIdFieldId);
//      if (gcGrouperSyncDependencyGroupUser == null) {
//        internalCacheSyncUserDepsByGroupIdFieldIdNotFound.add(groupIdFieldId);
//      } else {
//        internalCacheSyncUserDepsByGroupIdFieldId.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
//      }
//    }

    return result;
  }

  /**
   * 
   */
  private GcGrouperSync gcGrouperSync;
  public static int internalTestingRetrieveAllCount = 0;
  public static int internalTestingRetrieveByGroupIdFieldIdCount = 0;
  public static int internalTestingRetrieveByGroupIdFieldIdProvisionableGroupIdCount = 0;
  public static int internalTestingStoreCount = 0;
  
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
   * select grouper sync dependency group groups by provisionable group ids
   * @param provisionableGroupIdsCollection
   * @return the provisionableGroupId to syncDependencyGroupGroups map
   */
  public Map<MultiKey, GcGrouperSyncDependencyGroupGroup> internal_dependencyGroupGroupRetrieveFromDbByGroupIdsFieldIds(Collection<MultiKey> groupIdsFieldIdsCollection) {

    internalTestingRetrieveByGroupIdFieldIdCount++;
    
    Map<MultiKey, GcGrouperSyncDependencyGroupGroup> result = new HashMap<>();
    
    if (GrouperClientUtils.length(groupIdsFieldIdsCollection) == 0) {
      return result;
    }
    
    List<MultiKey> groupIdsFieldIdsList = new ArrayList<MultiKey>(groupIdsFieldIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect() / 2;
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupIdsFieldIdsList, batchSize, false);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<MultiKey> batchOfGroupIdsFieldIds = GrouperClientUtils.batchList(groupIdsFieldIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select * from grouper_sync_dep_group_group where ");
      
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
      
      List<GcGrouperSyncDependencyGroupGroup> gcGrouperSyncDependencyGroupGroups = gcDbAccess.sql(sql.toString()).selectList(GcGrouperSyncDependencyGroupGroup.class);
      
      for (GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup : GrouperClientUtils.nonNull(gcGrouperSyncDependencyGroupGroups)) {
        MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupGroup.getGroupId(), gcGrouperSyncDependencyGroupGroup.getFieldId());
        result.put(groupIdFieldId, gcGrouperSyncDependencyGroupGroup);
      }
        
    }
    // TODO
//    for (MultiKey groupIdFieldId : groupIdsFieldIdsCollection) {
//      GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser = result.get(groupIdFieldId);
//      if (gcGrouperSyncDependencyGroupUser == null) {
//        internalCacheSyncUserDepsByGroupIdFieldIdNotFound.add(groupIdFieldId);
//      } else {
//        internalCacheSyncUserDepsByGroupIdFieldId.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
//      }
//    }

    return result;
  }

  /**
   * select grouper sync dependency group groups by provisionable group ids
   * @param provisionableGroupIdsCollection
   * @return the provisionableGroupId to syncDependencyGroupGroups map
   */
  public Map<MultiKey, GcGrouperSyncDependencyGroupGroup> internal_dependencyGroupGroupRetrieveFromDbByGroupIdsFieldIdsProvisionableGroupIds(Collection<MultiKey> groupIdsFieldIdsProvisionableGroupIdsCollection) {

    internalTestingRetrieveByGroupIdFieldIdProvisionableGroupIdCount++;
    
    Map<MultiKey, GcGrouperSyncDependencyGroupGroup> result = new HashMap<>();
    
    if (GrouperClientUtils.length(groupIdsFieldIdsProvisionableGroupIdsCollection) == 0) {
      return result;
    }
    
    List<MultiKey> groupIdsFieldIdsProvisionableGroupIdsList = new ArrayList<MultiKey>(groupIdsFieldIdsProvisionableGroupIdsCollection);
    
    int batchSize = this.getGcGrouperSync().maxBindVarsInSelect() / 3;
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupIdsFieldIdsProvisionableGroupIdsList, batchSize, false);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<MultiKey> batchOfGroupIdsFieldIdsProvisionableGroupIds = GrouperClientUtils.batchList(groupIdsFieldIdsProvisionableGroupIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select * from grouper_sync_dep_group_group where ");
      
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName());
      
      boolean first = true;
      for (MultiKey groupIdFieldId : batchOfGroupIdsFieldIdsProvisionableGroupIds) {
        String groupId = (String)groupIdFieldId.getKey(0);
        String fieldId = (String)groupIdFieldId.getKey(1);
        String provisionableGroupId = (String)groupIdFieldId.getKey(2);
        if (!first) {
          sql.append(" or ");
        }
        
        sql.append(" ( group_id = ? and field_id = ? and provisionable_group_id = ? ) ");
        gcDbAccess.addBindVar(groupId);
        gcDbAccess.addBindVar(fieldId);
        gcDbAccess.addBindVar(provisionableGroupId);
        first = false;
      }
      
      List<GcGrouperSyncDependencyGroupGroup> gcGrouperSyncDependencyGroupGroups = gcDbAccess.sql(sql.toString()).selectList(GcGrouperSyncDependencyGroupGroup.class);
      
      for (GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup : GrouperClientUtils.nonNull(gcGrouperSyncDependencyGroupGroups)) {
        MultiKey groupIdFieldIdProvisionableGroupId = new MultiKey(gcGrouperSyncDependencyGroupGroup.getGroupId(), gcGrouperSyncDependencyGroupGroup.getFieldId(), 
            gcGrouperSyncDependencyGroupGroup.getProvisionableGroupId());
        result.put(groupIdFieldIdProvisionableGroupId, gcGrouperSyncDependencyGroupGroup);
      }
        
    }
    // TODO
//    for (MultiKey groupIdFieldId : groupIdsFieldIdsCollection) {
//      GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser = result.get(groupIdFieldId);
//      if (gcGrouperSyncDependencyGroupUser == null) {
//        internalCacheSyncUserDepsByGroupIdFieldIdNotFound.add(groupIdFieldId);
//      } else {
//        internalCacheSyncUserDepsByGroupIdFieldId.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser);
//      }
//    }

    return result;
  }

  /**
   * delete all for this provisioner
   * @return rows deleted
   */
  public int deleteAll() {

    List<GcGrouperSyncDependencyGroupGroup> internal_dependencyGroupGroupRetrieveFromDbAll = internal_dependencyGroupGroupRetrieveFromDbAll();
    
    if (GrouperClientUtils.length(internal_dependencyGroupGroupRetrieveFromDbAll) == 0) {
      return 0;
    }
    
    Set<Long> depIds = new HashSet<Long>();
    
    for (GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup : internal_dependencyGroupGroupRetrieveFromDbAll) {
      
      depIds.add(gcGrouperSyncDependencyGroupGroup.getIdIndex());
      
    }
  
    internal_dependencyGroupGroupDeleteBatchByIdIndexes(depIds);
    
    return depIds.size();
  
  }

//  /**
//   * select grouper sync dependency group groups by group ids
//   * @param groupIdsCollection
//   * @return the provisionableGroupId to syncDependencyGroupGroups set
//   */
//  public Map<String, Set<GcGrouperSyncDependencyGroupGroup>> internal_dependencyGroupGroupRetrieveFromDbByGroupIds(Collection<String> groupIdsCollection) {
//    
//    Map<String, Set<GcGrouperSyncDependencyGroupGroup>> result = new HashMap<>();
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
//      String sql = "select * from grouper_sync_dep_group_group where group_id in ( " 
//          + GrouperClientUtils.appendQuestions(batchOfGroupIds.size()) + ")";
//      
//      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(this.getGcGrouperSync().getConnectionName())
//          .sql(sql);
//      for (String groupId : batchOfGroupIds) {
//        gcDbAccess.addBindVar(groupId);
//      }
//
//      List<GcGrouperSyncDependencyGroupGroup> gcGrouperSyncDependencyGroupGroups = gcDbAccess.selectList(GcGrouperSyncDependencyGroupGroup.class);
//      
//      for (GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup : GrouperClientUtils.nonNull(gcGrouperSyncDependencyGroupGroups)) {
//        
//        Set<GcGrouperSyncDependencyGroupGroup> dependencies = result.get(gcGrouperSyncDependencyGroupGroup.getGroupId());
//        if (dependencies == null) {
//          dependencies = new HashSet<>();
//          result.put(gcGrouperSyncDependencyGroupGroup.getGroupId(), dependencies);
//        }
//        
//        dependencies.add(gcGrouperSyncDependencyGroupGroup);
//
//      }
//        
//    }
//    return result;
//  }

}
