package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
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
  // TODO rename
  public static GrouperDataProvider selectByText(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }
    GrouperDataProvider grouperDataLoaderConfig = new GcDbAccess().sql("select * from grouper_data_provider where config_id = ?")
        .addBindVar(configId).select(GrouperDataProvider.class);
    return grouperDataLoaderConfig;
  }
  
  public static Set<GrouperDataProvider> selectByTexts(Set<String> configIds) {
    if (configIds == null || configIds.size() == 0) {
      return new HashSet<>();
    }
    
    Set<GrouperDataProvider> result = new HashSet<>();
    
    List<GrouperDataProvider> grouperDataFields = new GcDbAccess().sql("select * from grouper_data_provider ")
        .selectMultipleColumnName("config_id")
        .bindVars(new ArrayList<String>(configIds))
        .selectList(GrouperDataProvider.class);
    
    result.addAll(grouperDataFields);
    
    for (GrouperDataProvider grouperDataProvider: result) {
      configIdToInternalIdCache().put(grouperDataProvider.getConfigId(), grouperDataProvider.getInternalId());
      internalIdToConfigIdCache().put(grouperDataProvider.getInternalId(), grouperDataProvider.getConfigId());
    }
   
    return result;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDataProvider grouperDataProvider) {
    List<GrouperDataProvider> grouperDataProviders = new ArrayList<>();
    grouperDataProviders.add(grouperDataProvider); 
    delete(grouperDataProviders);
  }
  
  public static void delete(List<GrouperDataProvider> grouperDataProviders) {
    if (GrouperUtil.length(grouperDataProviders) == 0) {
      return;
    }
    
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    grouperDataEngine.loadFieldsAndRows(grouperConfig);
    
    List<GrouperDataField> grouperDataFieldsInDb = GrouperUtil.nonNull(GrouperDataFieldDao.selectAll());
    Map<Long, GrouperDataField> internalIdToGrouperDataFieldInDb = new HashMap<Long, GrouperDataField>();
    for (GrouperDataField grouperDataField : grouperDataFieldsInDb) {
      internalIdToGrouperDataFieldInDb.put(grouperDataField.getInternalId(), grouperDataField);
    }
    
    List<GrouperDataRow> grouperDataRowsInDb = GrouperUtil.nonNull(GrouperDataRowDao.selectAll());
    Map<Long, GrouperDataRow> internalIdToGrouperDataRowInDb = new HashMap<Long, GrouperDataRow>();
    for (GrouperDataRow grouperDataRow : grouperDataRowsInDb) {
      internalIdToGrouperDataRowInDb.put(grouperDataRow.getInternalId(), grouperDataRow);
    }

    Set<Long> dataProviderInternalIds = new HashSet<>();

    for (GrouperDataProvider grouperDataProvider: grouperDataProviders) {
      grouperDataProvider.storePrepare();
      dataProviderInternalIds.add(grouperDataProvider.getInternalId());
    }

    List<GrouperDataGlobalAssign> grouperDataGlobalAssingsToDelete = GrouperDataGlobalAssignDao.selectByDataProviderInternalIds(dataProviderInternalIds);
    List<GrouperDataFieldAssign> grouperDataFieldAssignsToDelete = GrouperDataFieldAssignDao.selectByDataProviderInternalIds(dataProviderInternalIds);
    List<GrouperDataRowAssign> grouperDataRowAssignsToDelete = GrouperDataRowAssignDao.selectByDataProviderInternalIds(dataProviderInternalIds);
    Set<Long> dataRowAssignInternalIds = new HashSet<>();
    for (GrouperDataRowAssign dataRowAssign : grouperDataRowAssignsToDelete) {
      dataRowAssignInternalIds.add(dataRowAssign.getInternalId());
    }

    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssignsToDelete = GrouperDataRowFieldAssignDao.selectByDataRowAssignInternalIds(dataRowAssignInternalIds);
    Map<Long, List<GrouperDataRowFieldAssign>> dataRowAssignInternalIdToDataRowFieldAssigns = new HashMap<>();
    for (GrouperDataRowFieldAssign dataRowFieldAssign : grouperDataRowFieldAssignsToDelete) {
      if (dataRowAssignInternalIdToDataRowFieldAssigns.get(dataRowFieldAssign.getDataRowAssignInternalId()) == null) {
        dataRowAssignInternalIdToDataRowFieldAssigns.put(dataRowFieldAssign.getDataRowAssignInternalId(), new ArrayList<>());
      }

      dataRowAssignInternalIdToDataRowFieldAssigns.get(dataRowFieldAssign.getDataRowAssignInternalId()).add(dataRowFieldAssign);
    }

    // add history if configured
    List<GrouperDataFieldAssignHst> grouperDataFieldAssignHstsToInsert = new ArrayList<>();
    List<GrouperDataRowAssignHst> grouperDataRowAssignHstsToInsert = new ArrayList<>();
    List<GrouperDataRowFieldAssignHst> grouperDataRowFieldAssignHstsToInsert = new ArrayList<>();

    for (GrouperDataFieldAssign grouperDataFieldAssignToDelete : grouperDataFieldAssignsToDelete) {
      GrouperDataField dataField = internalIdToGrouperDataFieldInDb.get(grouperDataFieldAssignToDelete.getDataFieldInternalId());
      GrouperDataFieldConfig dataFieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(dataField.getConfigId());

      if (dataFieldConfig.isFieldDataStorePit()) {
        GrouperDataFieldAssignHst grouperDataFieldAssignHst = new GrouperDataFieldAssignHst();
        grouperDataFieldAssignHst.setDataFieldInternalId(grouperDataFieldAssignToDelete.getDataFieldInternalId());
        grouperDataFieldAssignHst.setMemberInternalId(grouperDataFieldAssignToDelete.getMemberInternalId());
        grouperDataFieldAssignHst.setValueInteger(grouperDataFieldAssignToDelete.getValueInteger());
        grouperDataFieldAssignHst.setValueDictionaryInternalId(grouperDataFieldAssignToDelete.getValueDictionaryInternalId());
        grouperDataFieldAssignHst.setStartTime(grouperDataFieldAssignToDelete.getCreatedOn().getTime() * 1000L);
        grouperDataFieldAssignHst.setEndTime(System.currentTimeMillis() * 1000L);
        grouperDataFieldAssignHstsToInsert.add(grouperDataFieldAssignHst);
      }
    }

    for (GrouperDataRowAssign grouperDataRowAssignToDelete : grouperDataRowAssignsToDelete) {
      GrouperDataRow dataRow = internalIdToGrouperDataRowInDb.get(grouperDataRowAssignToDelete.getDataRowInternalId());
      GrouperDataRowConfig dataRowConfig = grouperDataEngine.getRowConfigByConfigId().get(dataRow.getConfigId());

      if (dataRowConfig.isRowDataStorePit()) {
        Long endTime = System.currentTimeMillis() * 1000L;
        Long startTime = grouperDataRowAssignToDelete.getLastUpdated();
        if (startTime == null) {
          startTime = grouperDataRowAssignToDelete.getCreatedOn().getTime() * 1000L;
        }

        GrouperDataRowAssignHst grouperDataRowAssignHst = new GrouperDataRowAssignHst();
        grouperDataRowAssignHst.setMemberInternalId(grouperDataRowAssignToDelete.getMemberInternalId());
        grouperDataRowAssignHst.setDataRowInternalId(grouperDataRowAssignToDelete.getDataRowInternalId());
        grouperDataRowAssignHst.setDataRowAssignInternalId(grouperDataRowAssignToDelete.getInternalId());
        grouperDataRowAssignHst.setStartTime(startTime);
        grouperDataRowAssignHst.setEndTime(endTime);
        grouperDataRowAssignHstsToInsert.add(grouperDataRowAssignHst);

        for (GrouperDataRowFieldAssign grouperDataRowFieldAssign : GrouperUtil.nonNull(dataRowAssignInternalIdToDataRowFieldAssigns.get(grouperDataRowAssignToDelete.getInternalId()))) {          
          GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst = new GrouperDataRowFieldAssignHst();
          grouperDataRowFieldAssignHst.setDataRowAssignHst(grouperDataRowAssignHst);
          grouperDataRowFieldAssignHst.setDataFieldInternalId(grouperDataRowFieldAssign.getDataFieldInternalId());
          grouperDataRowFieldAssignHst.setValueInteger(grouperDataRowFieldAssign.getValueInteger());
          grouperDataRowFieldAssignHst.setValueDictionaryInternalId(grouperDataRowFieldAssign.getValueDictionaryInternalId());
          grouperDataRowFieldAssignHstsToInsert.add(grouperDataRowFieldAssignHst);
        }
      }
    }

    GrouperDataGlobalAssignDao.delete(grouperDataGlobalAssingsToDelete);

    GrouperDataFieldAssignHstDao.store(grouperDataFieldAssignHstsToInsert);
    GrouperDataFieldAssignDao.delete(grouperDataFieldAssignsToDelete);

    GrouperDataRowAssignHstDao.store(grouperDataRowAssignHstsToInsert);
    GrouperDataRowFieldAssignHstDao.store(grouperDataRowFieldAssignHstsToInsert);

    GrouperDataRowFieldAssignDao.delete(grouperDataRowFieldAssignsToDelete);
    GrouperDataRowAssignDao.delete(grouperDataRowAssignsToDelete);
    
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataProviders);

    //TODO remove all things from cache when they are deleted
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
  
  /**
   * @param configIds
   * @return
   */
  public static void insertMissingConfigIds(Set<String> configIds) {
    if (CollectionUtils.isEmpty(configIds)) {
      return;
    }
    
    selectByTexts(configIds); // this will populate the cache
    
    List<GrouperDataProvider> fieldsToInsert = new ArrayList<>();
    Set<String> configIdsToInsert = new HashSet<>();
    
    for (String configId: configIds) {
      Long internalId = configIdToInternalIdCache().get(configId);
      if (internalId == null) {
        GrouperDataProvider grouperDataProvider = new GrouperDataProvider();
        grouperDataProvider.setConfigId(configId);
        grouperDataProvider.storePrepare();
        fieldsToInsert.add(grouperDataProvider);
        configIdsToInsert.add(configId);
      }
    }
    
    if (fieldsToInsert.size() == 0) {
      return;
    }
    
    //get ids in one fell swoop
    List<Long> ids = TableIndex.reserveIds(TableIndexType.dataLoaderConfig, fieldsToInsert.size());
    int i = 0;
    for (GrouperDataProvider grouperDataProvider: fieldsToInsert) {
      grouperDataProvider.setTempInternalIdOnDeck(ids.get(i));
      i++;
    }
    
    int storeBatchToDatabase = new GcDbAccess().retryBatchStoreFailures(true).storeBatchToDatabase(fieldsToInsert, 1000);
    if (storeBatchToDatabase != fieldsToInsert.size()) {
      GrouperUtil.sleep(400); // Maybe there are other transactions that are working on the same objects, let's wait for them to finish.
      // Maybe they will insert the missing ones and then we can select 
    }
    selectByTexts(configIdsToInsert); // this is going to populate the cache

  }
  
  public static List<GrouperDataProvider> selectAll() {
    return new GcDbAccess().sql("select * from grouper_data_provider").selectList(GrouperDataProvider.class);

  }


}
