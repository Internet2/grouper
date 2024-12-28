package edu.internet2.middleware.grouper.sqlCache;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

/**
 * dao for sql cache dependency types
 * @author mchyzer
 *
 */
public class SqlCacheDependencyTypeDao {


  public SqlCacheDependencyTypeDao() {
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
    SqlCacheDependencyType sqlCacheDependencyType = new GcDbAccess()
        .sql("select * from grouper_sql_cache_depend_type where internal_id = ?").addBindVar(id).select(SqlCacheDependencyType.class);
    return sqlCacheDependencyType;
  }
  
  /**
   * select by dependency category
   * @param dependencyCategory
   * @return the sql cache dependency types
   */
  public static List<SqlCacheDependencyType> retrieveByDependencyCategory(String dependencyCategory) {
    List<SqlCacheDependencyType> sqlCacheDependencyTypes = new GcDbAccess()
        .sql("select * from grouper_sql_cache_depend_type where dependency_category = ?").addBindVar(dependencyCategory).selectList(SqlCacheDependencyType.class);
    return sqlCacheDependencyTypes;
  }
  
  /**
   * select by dependency category
   * @return the sql cache dependency types
   */
  public static List<SqlCacheDependencyType> retrieveAll() {
    List<SqlCacheDependencyType> sqlCacheDependencyTypes = new GcDbAccess()
        .sql("select * from grouper_sql_cache_depend_type").selectList(SqlCacheDependencyType.class);
    return sqlCacheDependencyTypes;
  }

  /**
   * select by dependency category and name
   * @param dependencyCategory
   * @param name
   * @return the sql cache dependency type
   */
  public static SqlCacheDependencyType retrieveByDependencyCategoryAndName(String dependencyCategory, String name) {
    SqlCacheDependencyType sqlCacheDependencyType = new GcDbAccess()
        .sql("select * from grouper_sql_cache_depend_type where dependency_category = ? and name = ?")
        .addBindVar(dependencyCategory).addBindVar(name)
        .select(SqlCacheDependencyType.class);
    
    return sqlCacheDependencyType;
  }
  

  /**
   * 
   * @param sqlCacheDependencyType
   */
  public static void delete(SqlCacheDependencyType sqlCacheDependencyType) {
    
    new GcDbAccess().deleteFromDatabase(sqlCacheDependencyType);
  }
  
  public static void addDefaultSqlCacheDependencyTypesIfNecessary() {
    {
      List<SqlCacheDependencyType> sqlCacheDependencyTypes = null;
      try {
        sqlCacheDependencyTypes = retrieveAll();
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
      
      if (!names.contains("mshipHistory_abac")) {
        SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
        sqlCacheDependencyType.setDependencyCategory("mshipHistory");
        sqlCacheDependencyType.setName("mshipHistory_abac");
        sqlCacheDependencyType.setDescription("Dependency to keep track of sql cache membership history for objects used with ABAC");
        sqlCacheDependencyTypesToStore.add(sqlCacheDependencyType);
      }
      
      if (!names.contains("abac_attribute")) {
        SqlCacheDependencyType sqlCacheDependencyType = new SqlCacheDependencyType();
        sqlCacheDependencyType.setDependencyCategory("abac");
        sqlCacheDependencyType.setName("abac_attribute");
        sqlCacheDependencyType.setDescription("Dependency to keep track of memberships which affect abac scripted groups");
        sqlCacheDependencyTypesToStore.add(sqlCacheDependencyType);
      }
      
      if (sqlCacheDependencyTypesToStore.size() > 0) {
        SqlCacheDependencyTypeDao.store(sqlCacheDependencyTypesToStore);
      }
    }
  }
}
