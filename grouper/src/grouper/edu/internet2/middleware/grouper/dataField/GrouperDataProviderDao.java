package edu.internet2.middleware.grouper.dataField;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * dao for data field configs
 * @author mchyzer
 *
 */
public class GrouperDataProviderDao {


  public GrouperDataProviderDao() {
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataProvider.class)).executeSql();
  }

  /**
   * @param grouperDataLoaderConfig
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataProvider grouperDataLoaderConfig) {
    
    GrouperUtil.assertion(grouperDataLoaderConfig != null, "grouperDataLoaderConfig is null");
    
    boolean isInsert = grouperDataLoaderConfig.getInternalId() == -1;

    grouperDataLoaderConfig.storePrepare();

    if (!isInsert) {
      boolean changed = new GcDbAccess().storeToDatabase(grouperDataLoaderConfig);
      return changed;
    }

    RuntimeException runtimeException = null;
    // might be other places saving the same config
    for (int i=0;i<5;i++) {
      try {
        new GcDbAccess().storeToDatabase(grouperDataLoaderConfig);
        return true;
      } catch (RuntimeException re) {
        runtimeException = re;
        GrouperUtil.sleep(100 * (i+1));
        GrouperDataProvider grouperDataLoaderConfigNew = selectByText(grouperDataLoaderConfig.getConfigId());
        if (grouperDataLoaderConfigNew != null) {
          return false;
        }
        if (i==4) {
          throw re;
        }
      }
    }
    // this should never happen :)
    throw runtimeException;
  }  

  public static GrouperDataProvider selectByText(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }
    GrouperDataProvider grouperDataLoaderConfig = new GcDbAccess().sql("select * from grouper_data_provider where config_id = ?")
        .addBindVar(configId).select(GrouperDataProvider.class);
    return grouperDataLoaderConfig;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDataProvider grouperDataLoaderConfig) {
    grouperDataLoaderConfig.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataLoaderConfig);
  }

  /**
   * cache, use the method to get this
   */
  private static ExpirableCache<String, Long> configIdToInternalIdCache = null;
  
  /**
   * max terms in memory
   */
  private static int maxTermsInMemoryCache = 50000;
  
  /**
   * config cache
   * @return the cache
   */
  private static ExpirableCache<String, Long> configIdToInternalIdCache() {
    if (configIdToInternalIdCache == null) {
      configIdToInternalIdCache = new ExpirableCache<String, Long>(60);
    }
    return configIdToInternalIdCache;
  }
  
  /**
   * cache, use the method to get this
   */
  private static ExpirableCache<Long, String> internalIdToConfigIdCache = null;
  
  /**
   * config cache
   * @return the cache
   */
  private static ExpirableCache<Long, String> internalIdToConfigIdCache() {
    if (internalIdToConfigIdCache == null) {
      internalIdToConfigIdCache = new ExpirableCache<Long, String>(60);
    }
    return internalIdToConfigIdCache;
  }
  
  /**
   * @param configId
   * @return the config
   */
  public static Long findOrAdd(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }

    Long internalId = configIdToInternalIdCache().get(configId);
    if (internalId == null) {
      GrouperDataProvider grouperDataLoaderConfig = new GrouperDataProvider();
      grouperDataLoaderConfig.setConfigId(configId);
      store(grouperDataLoaderConfig);
      internalId = grouperDataLoaderConfig.getInternalId();
      
      if (configIdToInternalIdCache().size(false) < maxTermsInMemoryCache) {
        configIdToInternalIdCache().put(configId, internalId);
        internalIdToConfigIdCache().put(internalId, configId);
      }
    }
    return internalId;
  }
  

}
