package edu.internet2.middleware.grouper.sqlCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
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
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(SqlCacheDependency sqlCacheDependency) {
    sqlCacheDependency.storePrepare();
    boolean changed = new GcDbAccess().storeToDatabase(sqlCacheDependency);
    return changed;
  }

  /**
   * 
   * @param connectionName
   * @return number of changes
   */
  public static int store(Collection<SqlCacheDependency> sqlCacheDependencies) {
    if (GrouperUtil.length(sqlCacheDependencies) == 0) {
      return 0;
    }
    for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
      sqlCacheDependency.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    return new GcDbAccess().storeBatchToDatabase(sqlCacheDependencies, batchSize);
  }

  /**
   * select grouper sync by id
   * @param theConnectionName
   * @param id
   * @return the sync
   */
  public static SqlCacheDependency retrieveByInternalId(Long id) {
    SqlCacheDependency sqlCacheDependency = new GcDbAccess()
        .sql("select * from grouper_sql_cache_dependency where internal_id = ?").addBindVar(id).select(SqlCacheDependency.class);
    return sqlCacheDependency;
  }
  

  /**
   * 
   * @param sqlCacheDependency
   */
  public static void delete(SqlCacheDependency sqlCacheDependency) {
    new GcDbAccess().deleteFromDatabase(sqlCacheDependency);
  }

  /**
   * retrieve cache group by group name field name or created
   * @param sqlCacheGroups
   */
  public static SqlCacheDependency retrieveOrCreateBySqlCacheDependency(SqlCacheDependency sqlCacheDependency) {
    if (sqlCacheDependency == null) {
      return null;
    }
    return retrieveOrCreateBySqlCacheDependency(GrouperUtil.toList(sqlCacheDependency)).values().iterator().next();
  }

  /**
   * retrieve cache group by type id, dependency id, and owner id
   * @param sqlCacheDependencies
   * @return owner id, dependent id, type id, and to sql cache dependency
   */
  public static Map<MultiKey, SqlCacheDependency> retrieveOrCreateBySqlCacheDependency(Collection<SqlCacheDependency> sqlCacheDependencies) {
    
    Map<MultiKey, SqlCacheDependency> result = new HashMap<>();

    if (GrouperUtil.length(sqlCacheDependencies) == 0) {
      return result;
    }
    Set<MultiKey> ownerIdsDependentIdsTypeIds = new HashSet<>();
    
    for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
      ownerIdsDependentIdsTypeIds.add(new MultiKey(sqlCacheDependency.getOwnerInternalId(), 
          sqlCacheDependency.getDependentInternalId(), sqlCacheDependency.getDependencyTypeInternalId()));
    }
    
    Map<MultiKey, SqlCacheGroup> existing = 
        retrieveByOwnerIdsDependentIdsTypeIds(ownerIdsDependentIdsTypeIds);
    
    List<SqlCacheGroup> sqlCacheGroupsToCreate = new ArrayList<SqlCacheGroup>();
    
//    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroups) {
//      SqlCacheGroup existingCacheGroup = existingGroupInternalIdsFieldInternalIdsToCacheGroups.get(new MultiKey(sqlCacheGroup.getGroupInternalId(), sqlCacheGroup.getFieldInternalId()));
//      if (existingCacheGroup == null) {
//        sqlCacheGroupsToCreate.add(sqlCacheGroup);
//      } else {
//        result.put(new MultiKey(existingCacheGroup.getGroupInternalId(), existingCacheGroup.getFieldInternalId()), sqlCacheGroup);
//      }
//    }

    if (sqlCacheGroupsToCreate.size() == 0) {
      return result;
    }

    // get ids in one fell swoop
    List<Long> ids = TableIndex.reserveIds(TableIndexType.sqlGroupCache, sqlCacheGroupsToCreate.size());

    for (int i=0; i < sqlCacheGroupsToCreate.size(); i++) {
      SqlCacheGroup sqlCacheGroup = sqlCacheGroupsToCreate.get(i);
      sqlCacheGroup.setTempInternalIdOnDeck(ids.get(i));
      sqlCacheGroup.storePrepare();
      //esult.put(new MultiKey(sqlCacheGroup.getGroupInternalId(), sqlCacheGroup.getFieldInternalId()), sqlCacheGroup);

    }

    //    internal_id int8 NOT NULL, -- internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)
    //    group_internal_id int8 NOT NULL, -- internal integer id for gruops which are cacheable
    //    field_internal_id int8 NOT NULL, -- internal integer id for the field which is the members or privilege which is cached
    //    membership_size int8 NOT NULL, -- approximate number of members of this group, used primarily to optimize batching
    //    membership_size_hst int8 NOT NULL, -- approximate number of rows of HST data for this group, used primarily to optimize batching
    //    created_on timestamp NOT NULL, -- when this row was created (i.e. when this group started to be cached)
    //    enabled_on timestamp NOT NULL, -- when this cache will be ready to use (do not use it while it is being populated)
    //    disabled_on timestamp NULL, -- when this cache should stop being used

    int defaultBatchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.batchSize", 1000);

    new GcDbAccess().storeBatchToDatabase(sqlCacheGroupsToCreate, defaultBatchSize);
    return result;
  }

  public static Map<MultiKey, SqlCacheGroup> retrieveByOwnerIdsDependentIdsTypeIds(
      Set<MultiKey> ownerIdsDependentIdsTypeIds) {
    // TODO Auto-generated method stub
    return null;
  }
  


}
