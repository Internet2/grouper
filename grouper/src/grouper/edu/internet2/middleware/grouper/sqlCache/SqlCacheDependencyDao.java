package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * dao for sql cache dependencies
 * @author mchyzer
 *
 */
public class SqlCacheDependencyDao {


  public SqlCacheDependencyDao() {
  }

  /**
   * 
   * @return true if changed
   */
  public static boolean store(SqlCacheDependency sqlCacheDependency) {
    sqlCacheDependency.storePrepare();
    boolean changed = new GcDbAccess().storeToDatabase(sqlCacheDependency);
    return changed;
  }

  /**
   * @return number of changes
   */
  public static int store(Collection<SqlCacheDependency> sqlCacheDependencies, Connection connection, boolean isInsert, boolean retryBatchStoreFailures, boolean ignoreRetriedBatchStoreFailures) {
    if (GrouperUtil.length(sqlCacheDependencies) == 0) {
      return 0;
    }
    for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
      sqlCacheDependency.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);    
    return new GcDbAccess().connection(connection)
        .isInsert(isInsert)
        .retryBatchStoreFailures(retryBatchStoreFailures)
        .ignoreRetriedBatchStoreFailures(ignoreRetriedBatchStoreFailures)
        .storeBatchToDatabase(sqlCacheDependencies, batchSize);
  }

  /**
   * select grouper sync by id
   * @param id
   * @return the sql cache dependency
   */
  public static SqlCacheDependency retrieveByInternalId(Long id) {
    SqlCacheDependency sqlCacheDependency = new GcDbAccess()
        .sql("select * from grouper_sql_cache_dependency where internal_id = ?").addBindVar(id).select(SqlCacheDependency.class);
    return sqlCacheDependency;
  }
  
  /**
   * select all the dependencies that this thing depends on
   * @param dependentInternalId
   * @return the sql cache dependency
   */
  public static List<SqlCacheDependency> retrieveAllByDependentId(Long dependentInternalId) {
    List<SqlCacheDependency> sqlCacheDependencies = new GcDbAccess()
        .sql("select * from grouper_sql_cache_dependency where dependent_internal_id = ?").addBindVar(dependentInternalId).selectList(SqlCacheDependency.class);
    return sqlCacheDependencies;
  }

  /**
   * select cache dependency by dependency type, owner, and dependent
   * @param depTypeInternalId
   * @param ownerInternalId
   * @param dependentInternalId
   * @return the sql cache dependency
   */
  public static SqlCacheDependency retrieveByDepTypeInternalIdOwnerInternalIdDependentInternalId(Long depTypeInternalId, Long ownerInternalId, Long dependentInternalId) {
    SqlCacheDependency sqlCacheDependency = new GcDbAccess()
        .sql("select * from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?")
        .addBindVar(depTypeInternalId)
        .addBindVar(ownerInternalId)
        .addBindVar(dependentInternalId)
        .select(SqlCacheDependency.class);
    return sqlCacheDependency;
  }
  
  /**
   * select cache dependencies by dependency type and multi key of owner and dependent internal ids
   * @param depTypeInternalId
   * @param ownerInternalIdsDependentInternalIds
   * @return cache dependencies
   */
  public static Map<MultiKey, SqlCacheDependency> retrieveByDepTypeInternalIdAndOwnerInternalIdsDependentInternalIds(Long depTypeInternalId, Collection<MultiKey> ownerInternalIdsDependentInternalIds) {
    
    Map<MultiKey, SqlCacheDependency> result = new HashMap<>();

    if (GrouperUtil.length(ownerInternalIdsDependentInternalIds) == 0) {
      return result;
    }

    List<MultiKey> ownerInternalIdsDependentInternalIdsList = new ArrayList<>(ownerInternalIdsDependentInternalIds);
    
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900) / 2;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(ownerInternalIdsDependentInternalIdsList), batchSize, false);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<MultiKey> batchOfOwnerInternalIdsDependentInternalIdsList = GrouperClientUtils.batchList(ownerInternalIdsDependentInternalIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select * from grouper_sql_cache_dependency where dep_type_internal_id = ? and (");
      
      GcDbAccess gcDbAccess = new GcDbAccess();
      gcDbAccess.addBindVar(depTypeInternalId);
      
      for (int i=0;i<batchOfOwnerInternalIdsDependentInternalIdsList.size();i++) {
        if (i>0) {
          sql.append(" or ");
        }
        sql.append(" ( owner_internal_id = ? and dependent_internal_id = ? ) ");
        MultiKey ownerInternalIdDependentInternalId = batchOfOwnerInternalIdsDependentInternalIdsList.get(i);
        gcDbAccess.addBindVar(ownerInternalIdDependentInternalId.getKey(0));
        gcDbAccess.addBindVar(ownerInternalIdDependentInternalId.getKey(1));
      }
      
      sql.append(")");
      
      List<SqlCacheDependency> sqlCacheDependencies = gcDbAccess.sql(sql.toString()).selectList(SqlCacheDependency.class);
      
      for (SqlCacheDependency sqlCacheDependency : GrouperClientUtils.nonNull(sqlCacheDependencies)) {
        result.put(new MultiKey(sqlCacheDependency.getOwnerInternalId(), sqlCacheDependency.getDependentInternalId()), sqlCacheDependency);
      }
      
    }
    return result;
  }
  
  /**
   * select by dependency type internal id
   * @param dependencyTypeInternalId
   * @return the sql cache dependencies
   */
  public static List<SqlCacheDependency> retrieveByDependencyTypeInternalId(Long dependencyTypeInternalId) {
    List<SqlCacheDependency> sqlCacheDependencies = new GcDbAccess()
        .sql("select * from grouper_sql_cache_dependency where dep_type_internal_id = ?").addBindVar(dependencyTypeInternalId).selectList(SqlCacheDependency.class);
    return sqlCacheDependencies;
  }

  /**
   * 
   * @param sqlCacheDependency
   */
  public static void delete(SqlCacheDependency sqlCacheDependency) {
    new GcDbAccess().deleteFromDatabase(sqlCacheDependency);
  }

  /**
   * owner is the group mentioned in the abac script.  dependant is the overall scripted group.
   * select dependant groups by dependency type and owner internal ids
   * @param depTypeInternalId
   * @param ownerGroupCacheInternalIds
   * @return a map of the owners, to the internal ids of the things dependent on the owner (e.g. groups that have the scripts)
   */
  public static Map<Long, Set<Long>> retrieveByDepTypeInternalIdAndOwnerCacheInternalIds(Long depTypeInternalId, Collection<Long> ownerGroupCacheInternalIds) {
    
    Map<Long, Set<Long>> result = new HashMap<>();
  
    if (GrouperUtil.length(ownerGroupCacheInternalIds) == 0) {
      return result;
    }
  
    // two bind vars in each record to retrieve
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(ownerGroupCacheInternalIds), batchSize, false);
    
    List<Long> ownerGroupCacheInternalIdsList = new ArrayList<>(ownerGroupCacheInternalIds);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<Long> batchOfOwnerGroupCacheInternalIdsList = GrouperClientUtils.batchList(ownerGroupCacheInternalIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("""
          select distinct gscd.* 
          from grouper_sql_cache_dependency gscd 
          where gscd.dep_type_internal_id = ?
          and gscd.owner_internal_id in (
          """);
      sql.append(GrouperClientUtils.appendQuestions(batchOfOwnerGroupCacheInternalIdsList.size()));
      sql.append(")");
      
      GcDbAccess gcDbAccess = new GcDbAccess().addBindVar(depTypeInternalId);
      for (Long ownerGroupCacheInternalId : batchOfOwnerGroupCacheInternalIdsList) {
        gcDbAccess.addBindVar(ownerGroupCacheInternalId);
      }
      
      List<SqlCacheDependency> sqlCacheDependencies = gcDbAccess.sql(sql.toString()).selectList(SqlCacheDependency.class);
      
      for (SqlCacheDependency sqlCacheDependency : GrouperClientUtils.nonNull(sqlCacheDependencies)) {
        
        Set<Long> internalIdsDependent = result.get(sqlCacheDependency.getOwnerInternalId());
        
        if (internalIdsDependent == null) {
          internalIdsDependent = new java.util.HashSet<Long>();
          result.put(sqlCacheDependency.getOwnerInternalId(), internalIdsDependent);
        }
        
        internalIdsDependent.add(sqlCacheDependency.getDependentInternalId());
        
      }
      
    }
    return result;
  }

  /**
   * owner is the group mentioned in the abac script.  dependant is the overall scripted group.
   * select dependant groups by dependency type and owner internal ids
   * @param depTypeInternalId
   * @param dependentGroupCacheInternalIds
   * @return a map of the owners, to the internal ids of the things dependent on the owner (e.g. groups that have the scripts)
   */
  public static List<SqlCacheDependency> retrieveByDepTypeInternalIdAndDependentCacheInternalIds(Long depTypeInternalId, Collection<Long> dependentGroupCacheInternalIds) {
    
    // result
    List<SqlCacheDependency> result = new ArrayList<>();
  
    if (GrouperUtil.length(dependentGroupCacheInternalIds) == 0) {
      return result;
    }
  
    // two bind vars in each record to retrieve
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(dependentGroupCacheInternalIds), batchSize, false);
    
    List<Long> ownerGroupCacheInternalIdsList = new ArrayList<>(dependentGroupCacheInternalIds);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<Long> batchOfOwnerGroupCacheInternalIdsList = GrouperClientUtils.batchList(ownerGroupCacheInternalIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("""
          select distinct gscd.* 
          from grouper_sql_cache_dependency gscd 
          where gscd.dep_type_internal_id = ?
          and gscd.dependent_internal_id in (
          """);
      sql.append(GrouperClientUtils.appendQuestions(batchOfOwnerGroupCacheInternalIdsList.size()));
      sql.append(")");
      
      GcDbAccess gcDbAccess = new GcDbAccess().addBindVar(depTypeInternalId);
      for (Long ownerGroupCacheInternalId : batchOfOwnerGroupCacheInternalIdsList) {
        gcDbAccess.addBindVar(ownerGroupCacheInternalId);
      }
      
      List<SqlCacheDependency> sqlCacheDependencies = gcDbAccess.sql(sql.toString()).selectList(SqlCacheDependency.class);
      
      result.addAll(sqlCacheDependencies);
      
    }
    return result;
  }

}
