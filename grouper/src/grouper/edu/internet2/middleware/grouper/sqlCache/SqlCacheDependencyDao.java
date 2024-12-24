package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

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
}
