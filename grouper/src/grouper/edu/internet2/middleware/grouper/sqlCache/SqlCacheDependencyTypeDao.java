package edu.internet2.middleware.grouper.sqlCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

/**
 * dao for sql cache dependency types
 * @author mchyzer
 */
public class SqlCacheDependencyTypeDao {

  public static final String NAME_MSHIP_HISTORY_ABAC = "mshipHistory_abac";
  public static final String NAME_ABAC_GROUP = "abac_group";
  public static final String NAME_ABAC_ROW = "abac_row";
  public static final String NAME_ABAC_ATTRIBUTE = "abac_attribute";

  private static class SqlCacheDependencyTypeCache {

    private List<SqlCacheDependencyType> types = new ArrayList<>();

    private Map<String, SqlCacheDependencyType> nameToType = new java.util.HashMap<>();

    private Map<Long, SqlCacheDependencyType> internalIdToType = new java.util.HashMap<>();

    private Map<String, List<SqlCacheDependencyType>> dependencyCategoryToTypes = new java.util.HashMap<>();

  }

  public SqlCacheDependencyTypeDao() {
  }

  /**
   * expirable cache for all dependency types
   * the caches have a string type: nameToType
   */
  private static ExpirableCache<Boolean, SqlCacheDependencyTypeCache> sqlCacheDependencyTypeExpirableCache = new ExpirableCache<>(5);
  
  /**
   * expirable cache for all dependency types
   * the caches have a string type: nameToType, internalIdToType, dependencyCategoryToTypes (returns list)
   * @param cacheName e.g. nameToType
   * @return the cache map
   */
  private static SqlCacheDependencyTypeCache sqlCacheDependencyTypeCache() {
    SqlCacheDependencyTypeCache sqlCacheDependencyTypeCache = sqlCacheDependencyTypeExpirableCache.get(Boolean.TRUE);
    if (sqlCacheDependencyTypeCache == null) {
      synchronized(sqlCacheDependencyTypeExpirableCache) {
        sqlCacheDependencyTypeCache = sqlCacheDependencyTypeExpirableCache.get(Boolean.TRUE);
        if (sqlCacheDependencyTypeCache == null) {
          
          sqlCacheDependencyTypeCache = new SqlCacheDependencyTypeCache();
          
          sqlCacheDependencyTypeCache.types = retrieveAllFromDb();
          
          for (SqlCacheDependencyType sqlCacheDependencyType : GrouperUtil.nonNull(sqlCacheDependencyTypeCache.types)) {
            sqlCacheDependencyTypeCache.nameToType.put(sqlCacheDependencyType.getName(), sqlCacheDependencyType);
            
          }
          
          for (SqlCacheDependencyType sqlCacheDependencyType : GrouperUtil.nonNull(sqlCacheDependencyTypeCache.types)) {
            sqlCacheDependencyTypeCache.internalIdToType.put(sqlCacheDependencyType.getInternalId(),
                sqlCacheDependencyType);
          }
          
          for (SqlCacheDependencyType sqlCacheDependencyType : GrouperUtil.nonNull(sqlCacheDependencyTypeCache.types)) {
            List<SqlCacheDependencyType> sqlCacheDependencyTypesForCategory = sqlCacheDependencyTypeCache.dependencyCategoryToTypes
                .get(sqlCacheDependencyType.getDependencyCategory());
            if (sqlCacheDependencyTypesForCategory == null) {
              sqlCacheDependencyTypesForCategory = new java.util.ArrayList<>();
              sqlCacheDependencyTypeCache.dependencyCategoryToTypes.put(
                  sqlCacheDependencyType.getDependencyCategory(),
                  sqlCacheDependencyTypesForCategory);
            }
            sqlCacheDependencyTypesForCategory.add(sqlCacheDependencyType);
          }
          
          sqlCacheDependencyTypeExpirableCache.put(Boolean.TRUE, sqlCacheDependencyTypeCache);
        }
      }        
    }
    return sqlCacheDependencyTypeCache;
  }
  
  /**
   * 
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(SqlCacheDependencyType sqlCacheDependencyType) {
    sqlCacheDependencyType.storePrepare();
    boolean changed = new GcDbAccess().storeToDatabase(sqlCacheDependencyType);
    return changed;
  }
  
  /**
   * @return number of changes
   */
  public static int store(Collection<SqlCacheDependencyType> sqlCacheDependencyTypes) {
    if (GrouperUtil.length(sqlCacheDependencyTypes) == 0) {
      return 0;
    }
    for (SqlCacheDependencyType sqlCacheDependencyType : sqlCacheDependencyTypes) {
      sqlCacheDependencyType.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    return new GcDbAccess().storeBatchToDatabase(sqlCacheDependencyTypes, batchSize);
  }

  /**
   * select by id
   * @param id
   * @return the sql cache dependency type
   */
  public static SqlCacheDependencyType retrieveByInternalId(Long id) {
    SqlCacheDependencyType sqlCacheDependencyType = sqlCacheDependencyTypeCache().internalIdToType.get(id);
    return sqlCacheDependencyType;
  }
  
  /**
   * select by dependency category
   * @param dependencyCategory
   * @return the sql cache dependency types
   */
  public static List<SqlCacheDependencyType> retrieveByDependencyCategory(String dependencyCategory) {
    return sqlCacheDependencyTypeCache().dependencyCategoryToTypes.get(dependencyCategory);
  }
  
  /**
   * select by dependency category
   * @return the sql cache dependency types
   */
  private static List<SqlCacheDependencyType> retrieveAllFromDb() {
    List<SqlCacheDependencyType> sqlCacheDependencyTypes = new GcDbAccess()
        .sql("select * from grouper_sql_cache_depend_type").selectList(SqlCacheDependencyType.class);
    return sqlCacheDependencyTypes;
  }

  /**
   * get all types
   * @return the sql cache dependency types
   */
  private static List<SqlCacheDependencyType> retrieveAll() {
    return sqlCacheDependencyTypeCache().types;
  }

  /**
   * select by dependency category and name
   * @param dependencyCategory
   * @param name
   * @return the sql cache dependency type
   */
  public static SqlCacheDependencyType retrieveByDependencyCategoryAndName(String dependencyCategory, String name) {
    return retrieveByName(name);
  }
  

  public static SqlCacheDependencyType retrieveByName(String name) {
    return sqlCacheDependencyTypeCache().nameToType.get(name);
  }

  /**
   * 
   * @param sqlCacheDependencyType
   */
  public static void delete(SqlCacheDependencyType sqlCacheDependencyType) {
    
    new GcDbAccess().deleteFromDatabase(sqlCacheDependencyType);
  }
  
  /**
   * note: names should be unique
   */
  public static void addDefaultSqlCacheDependencyTypesIfNecessary() {
    {
      List<SqlCacheDependencyType> sqlCacheDependencyTypes = null;
      try {
        sqlCacheDependencyTypes = retrieveAllFromDb();
      } catch (Exception e) {
        // table doesnt exist
        return;
      }
      Set<String> names = new HashSet<String>();
      for (SqlCacheDependencyType sqlCacheDependencyType : sqlCacheDependencyTypes) {
        names.add(sqlCacheDependencyType.getName());
      }
      
      Set<SqlCacheDependencyType> sqlCacheDependencyTypesToStore = new LinkedHashSet<>();

      if (!names.contains("mshipHistory_viaAttribute")) {
        SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
        sqlCacheDependencyType.setDependencyCategory("mshipHistory");
        sqlCacheDependencyType.setName("mshipHistory_viaAttribute");
        sqlCacheDependencyType.setDescription("Dependency to keep track of sql cache membership history for objects assigned via attribute");
        sqlCacheDependencyTypesToStore.add(sqlCacheDependencyType);
      }
      
      if (!names.contains("mshipHistory_recentMships")) {
        SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
        sqlCacheDependencyType.setDependencyCategory("mshipHistory");
        sqlCacheDependencyType.setName("mshipHistory_recentMships");
        sqlCacheDependencyType.setDescription("Dependency to keep track of sql cache membership history for objects used with recent memberships");
        sqlCacheDependencyTypesToStore.add(sqlCacheDependencyType);
      }
      
      if (!names.contains(NAME_MSHIP_HISTORY_ABAC)) {
        SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
        sqlCacheDependencyType.setDependencyCategory("mshipHistory");
        sqlCacheDependencyType.setName(NAME_MSHIP_HISTORY_ABAC);
        sqlCacheDependencyType.setDescription("Dependency to keep track of sql cache membership history for objects used with ABAC");
        sqlCacheDependencyTypesToStore.add(sqlCacheDependencyType);
      }
      
      if (!names.contains(NAME_ABAC_ATTRIBUTE)) {
        SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
        sqlCacheDependencyType.setDependencyCategory("abac");
        sqlCacheDependencyType.setName(NAME_ABAC_ATTRIBUTE);
        sqlCacheDependencyType.setDescription("Dependency to keep track of attributes which affect abac scripted groups");
        sqlCacheDependencyTypesToStore.add(sqlCacheDependencyType);
      }
      
      if (!names.contains(NAME_ABAC_ROW)) {
        SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
        sqlCacheDependencyType.setDependencyCategory("abac");
        sqlCacheDependencyType.setName(NAME_ABAC_ROW);
        sqlCacheDependencyType.setDescription("Dependency to keep track of rows which affect abac scripted groups");
        sqlCacheDependencyTypesToStore.add(sqlCacheDependencyType);
      }
      
      if (!names.contains(NAME_ABAC_GROUP)) {
        SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
        sqlCacheDependencyType.setDependencyCategory("abac");
        sqlCacheDependencyType.setName(NAME_ABAC_GROUP);
        sqlCacheDependencyType.setDescription("Dependency to keep track of groups which affect abac scripted groups");
        sqlCacheDependencyTypesToStore.add(sqlCacheDependencyType);
      }
      
      if (sqlCacheDependencyTypesToStore.size() > 0) {
        SqlCacheDependencyTypeDao.store(sqlCacheDependencyTypesToStore);
        sqlCacheDependencyTypeExpirableCache.clear();
      }
    }
  }
}
