package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * dao for field
 * @author mchyzer
 *
 */
public class GrouperDataFieldDao {


  public GrouperDataFieldDao() {
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataField.class)).executeSql();
  }

  /**
   * @param grouperDataField
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataField grouperDataField) {
    
    GrouperUtil.assertion(grouperDataField != null, "grouperDataField is null");
    
    boolean isInsert = grouperDataField.getInternalId() == -1;

    grouperDataField.storePrepare();

    if (!isInsert) {
      boolean changed = new GcDbAccess().storeToDatabase(grouperDataField);
      return changed;
    }

    RuntimeException runtimeException = null;
    // might be other places saving the same field
    for (int i=0;i<5;i++) {
      boolean created = false;
      try {
        new GcDbAccess().storeToDatabase(grouperDataField);
        created = true;
        
        return true;
      } catch (RuntimeException re) {
        if (created) {
          throw re;
        }
        runtimeException = re;
        GrouperUtil.sleep(100 * (i+1));
        GrouperDataField grouperDataFieldNew = selectByText(grouperDataField.getConfigId());
        if (grouperDataFieldNew != null) {
          grouperDataField.setInternalId(grouperDataFieldNew.getInternalId());
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

  public static GrouperDataField selectByText(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }
    GrouperDataField grouperDataField = new GcDbAccess().sql("select * from grouper_data_field where config_id = ?")
        .addBindVar(configId).select(GrouperDataField.class);
    
    if (grouperDataField != null) {
      configIdToInternalIdCache().put(configId, grouperDataField.getInternalId());
      internalIdToConfigIdCache().put(grouperDataField.getInternalId(), configId);
    }
    return grouperDataField;
  }
  
  public static Set<GrouperDataField> selectByTexts(Set<String> configIds) {
    if (configIds == null || configIds.size() == 0) {
      return new HashSet<>();
    }
    
    Set<GrouperDataField> result = new HashSet<>();
    
    List<GrouperDataField> grouperDataFields = new GcDbAccess().sql("select * from grouper_data_field ")
        .selectMultipleColumnName("config_id")
        .bindVars(new ArrayList<String>(configIds))
        .selectList(GrouperDataField.class);
    
    result.addAll(grouperDataFields);
    
    for (GrouperDataField grouperDataField: result) {
      configIdToInternalIdCache().put(grouperDataField.getConfigId(), grouperDataField.getInternalId());
      internalIdToConfigIdCache().put(grouperDataField.getInternalId(), grouperDataField.getConfigId());
    }
   
    return result;
  }
  
  public static void delete(List<GrouperDataField> grouperDataFields) {
    if (GrouperUtil.length(grouperDataFields) == 0) {
      return;
    }
    
    Set<Long> dataFieldInternalIds = new HashSet<>();
    
    for (GrouperDataField grouperDataField: grouperDataFields) {
      grouperDataField.storePrepare();
      dataFieldInternalIds.add(grouperDataField.getInternalId());
    }
    
    List<GrouperDataAlias> aliases = GrouperDataAliasDao.selectByDataFieldInternalIds(dataFieldInternalIds);
    GrouperDataAliasDao.delete(aliases);
    
    List<GrouperDataFieldAssign> dataFieldAssigns = GrouperDataFieldAssignDao.selectByDataFieldInternalIds(dataFieldInternalIds);
    GrouperDataFieldAssignDao.delete(dataFieldAssigns);
    
    List<GrouperDataRowFieldAssign> dataRowFieldAssigns = GrouperDataRowFieldAssignDao.selectByDataFieldInternalIds(dataFieldInternalIds);
    GrouperDataRowFieldAssignDao.delete(dataRowFieldAssigns);
    
    List<GrouperDataGlobalAssign> dataGlobalAssings = GrouperDataGlobalAssignDao.selectByDataFieldInternalIds(dataFieldInternalIds);
    GrouperDataGlobalAssignDao.delete(dataGlobalAssings);
    
    List<GrouperDataFieldAssignHst> dataFieldAssignHsts = GrouperDataFieldAssignHstDao.selectByDataFieldInternalIds(dataFieldInternalIds);
    GrouperDataFieldAssignHstDao.delete(dataFieldAssignHsts);
    
    List<GrouperDataRowFieldAssignHst> dataRowFieldAssignHsts = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalIds(dataFieldInternalIds);
    GrouperDataRowFieldAssignHstDao.delete(dataRowFieldAssignHsts);
    
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataFields);
    
    //TODO remove all things from cache when they are deleted
  }
  
  /**
   * 
   * @param grouperDataField
   */
  public static void delete(GrouperDataField grouperDataField) {
    grouperDataField.storePrepare();
    
    List<GrouperDataAlias> aliases = GrouperDataAliasDao.selectByDataFieldInternalId(grouperDataField.getInternalId());
    GrouperDataAliasDao.delete(aliases);

    List<GrouperDataFieldAssign> dataFieldAssigns = GrouperDataFieldAssignDao.selectByDataFieldInternalId(grouperDataField.getInternalId());
    GrouperDataFieldAssignDao.delete(dataFieldAssigns);
    
    List<GrouperDataRowFieldAssign> dataRowFieldAssigns = GrouperDataRowFieldAssignDao.selectByDataFieldInternalId(grouperDataField.getInternalId());
    GrouperDataRowFieldAssignDao.delete(dataRowFieldAssigns);
    
    List<GrouperDataGlobalAssign> dataGlobalAssings = GrouperDataGlobalAssignDao.selectByDataFieldInternalId(grouperDataField.getInternalId());
    GrouperDataGlobalAssignDao.delete(dataGlobalAssings);
    
    List<GrouperDataFieldAssignHst> dataFieldAssignHsts = GrouperDataFieldAssignHstDao.selectByDataFieldInternalId(grouperDataField.getInternalId());
    GrouperDataFieldAssignHstDao.delete(dataFieldAssignHsts);
    
    List<GrouperDataRowFieldAssignHst> dataRowFieldAssignHsts = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(grouperDataField.getInternalId());
    GrouperDataRowFieldAssignHstDao.delete(dataRowFieldAssignHsts);
    
    new GcDbAccess().deleteFromDatabase(grouperDataField);
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
   * field cache
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
   * field cache
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
   * @return the field
   */
  public static Long findOrAdd(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }

    Long internalId = configIdToInternalIdCache().get(configId);
    if (internalId == null) {
      GrouperDataField grouperDataField = new GrouperDataField();
      grouperDataField.setConfigId(configId);
      store(grouperDataField);
      internalId = grouperDataField.getInternalId();
      
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
    
    List<GrouperDataField> fieldsToInsert = new ArrayList<>();
    Set<String> configIdsToInsert = new HashSet<>();
    
    for (String configId: configIds) {
      Long internalId = configIdToInternalIdCache().get(configId);
      if (internalId == null) {
        GrouperDataField grouperDataField = new GrouperDataField();
        grouperDataField.setConfigId(configId);
        grouperDataField.storePrepare();
        fieldsToInsert.add(grouperDataField);
        configIdsToInsert.add(configId);
      }
    }
    
    if (fieldsToInsert.size() == 0) {
      return;
    }
    
    // get ids in one fell swoop
    List<Long> ids = TableIndex.reserveIds(TableIndexType.dataField, fieldsToInsert.size());
    int i = 0;
    for (GrouperDataField grouperDataField: fieldsToInsert) {
      grouperDataField.setTempInternalIdOnDeck(ids.get(i));
      i++;
    }
    
    int storeBatchToDatabase = new GcDbAccess().retryBatchStoreFailures(true).storeBatchToDatabase(fieldsToInsert, 1000);
    if (storeBatchToDatabase != fieldsToInsert.size()) {
      GrouperUtil.sleep(400); // Maybe there are other transactions that are working on the same objects, let's wait for them to finish.
      // Maybe they will insert the missing ones and then we can select 
    }
    selectByTexts(configIdsToInsert); // this is going to populate the cache

  }

  public static List<GrouperDataField> selectAll() {
    return new GcDbAccess().sql("select * from grouper_data_field").selectList(GrouperDataField.class);

  }
  

}
