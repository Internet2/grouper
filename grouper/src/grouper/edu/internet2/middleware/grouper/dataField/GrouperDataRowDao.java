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
 * dao for rows
 * @author mchyzer
 *
 */
public class GrouperDataRowDao {


  public GrouperDataRowDao() {
  }

  public static List<GrouperDataRow> selectAll() {
    return new GcDbAccess().sql("select * from grouper_data_row").selectList(GrouperDataRow.class);

  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataRow.class)).executeSql();
  }

  /**
   * @param grouperDataRow
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataRow grouperDataRow) {
    
    GrouperUtil.assertion(grouperDataRow != null, "grouperDataRow is null");
    
    boolean isInsert = grouperDataRow.getInternalId() == -1;

    grouperDataRow.storePrepare();

    if (!isInsert) {
      boolean changed = new GcDbAccess().storeToDatabase(grouperDataRow);
      return changed;
    }

    RuntimeException runtimeException = null;
    // might be other places saving the same row
    for (int i=0;i<5;i++) {
      boolean created = false;
      try {
        new GcDbAccess().storeToDatabase(grouperDataRow);
        created = true;

        return true;
      } catch (RuntimeException re) {
        if (created) {
          throw re;
        }
        runtimeException = re;
        GrouperUtil.sleep(100 * (i+1));
        GrouperDataRow grouperDataRowNew = selectByConfigId(grouperDataRow.getConfigId());
        if (grouperDataRowNew != null) {
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

  public static GrouperDataRow selectByConfigId(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }
    GrouperDataRow grouperDataRow = new GcDbAccess().sql("select * from grouper_data_row where config_id = ?")
        .addBindVar(configId).select(GrouperDataRow.class);
    return grouperDataRow;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDataRow grouperDataRow) {
    grouperDataRow.storePrepare();
    
    // delete aliases - done
    // delete row assignments - done
    // delete field row assignments - done
    // delete data global assign - no config, no dao
    
    List<GrouperDataAlias> aliases = GrouperDataAliasDao.selectByDataRowInternalId(grouperDataRow.getInternalId());
    for (GrouperDataAlias grouperDataAlias: aliases) {
      GrouperDataAliasDao.delete(grouperDataAlias);
    }
    
    List<GrouperDataRowFieldAssign> dataRowFieldAssigns = GrouperDataRowFieldAssignDao.selectByDataRowInternalId(grouperDataRow.getInternalId());
    for (GrouperDataRowFieldAssign grouperDataRowFieldAssign: dataRowFieldAssigns) {
      GrouperDataRowFieldAssignDao.delete(grouperDataRowFieldAssign);
    }
    
    List<GrouperDataRowAssign> dataRowAssigns = GrouperDataRowAssignDao.selectByDataRowInternalId(grouperDataRow.getInternalId());
    for (GrouperDataRowAssign grouperDataRowAssign: dataRowAssigns) {
      GrouperDataRowAssignDao.delete(grouperDataRowAssign);
    }
    
    List<GrouperDataRowFieldAssignHst> dataRowFieldAssignHsts = GrouperDataRowFieldAssignHstDao.selectByDataRowInternalId(grouperDataRow.getInternalId());
    GrouperDataRowFieldAssignHstDao.delete(dataRowFieldAssignHsts);
    
    List<GrouperDataRowAssignHst> dataRowAssignHsts = GrouperDataRowAssignHstDao.selectByDataRowInternalId(grouperDataRow.getInternalId());
    GrouperDataRowAssignHstDao.delete(dataRowAssignHsts);
    
    new GcDbAccess().deleteFromDatabase(grouperDataRow);
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
   * row cache
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
   * row cache
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
   * @return the row
   */
  public static Long findOrAdd(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }

    Long internalId = configIdToInternalIdCache().get(configId);
    if (internalId == null) {
      GrouperDataRow grouperDataRow = new GrouperDataRow();
      grouperDataRow.setConfigId(configId);
      store(grouperDataRow);
      internalId = grouperDataRow.getInternalId();
      
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
    
    List<GrouperDataRow> rowsToInsert = new ArrayList<>();
    Set<String> configIdsToInsert = new HashSet<>();
    
    for (String configId: configIds) {
      Long internalId = configIdToInternalIdCache().get(configId);
      if (internalId == null) {
        GrouperDataRow grouperDataRow = new GrouperDataRow();
        grouperDataRow.setConfigId(configId);
        grouperDataRow.storePrepare();
        rowsToInsert.add(grouperDataRow);
        configIdsToInsert.add(configId);
      }
    }
    
    if (rowsToInsert.size() == 0) {
      return;
    }
    
    // get ids in one fell swoop
    List<Long> ids = TableIndex.reserveIds(TableIndexType.dataRow, rowsToInsert.size());
    int i = 0;
    for (GrouperDataRow grouperDataRow: rowsToInsert) {
      grouperDataRow.setTempInternalIdOnDeck(ids.get(i));
      i++;
    }
    
    int storeBatchToDatabase = new GcDbAccess().retryBatchStoreFailures(true).storeBatchToDatabase(rowsToInsert, 1000);
    if (storeBatchToDatabase != rowsToInsert.size()) {
      GrouperUtil.sleep(400); // Maybe there are other transactions that are working on the same objects, let's wait for them to finish.
      // Maybe they will insert the missing ones and then we can select 
    }
    selectByTexts(configIdsToInsert); // this is going to populate the cache

  }
  
  public static Set<GrouperDataRow> selectByTexts(Set<String> configIds) {
    if (configIds == null || configIds.size() == 0) {
      return new HashSet<>();
    }
    
    Set<GrouperDataRow> result = new HashSet<>();
    
    List<GrouperDataRow> grouperDataRows = new GcDbAccess().sql("select * from grouper_data_row ")
        .selectMultipleColumnName("config_id")
        .bindVars(new ArrayList<String>(configIds))
        .selectList(GrouperDataRow.class);
    
    result.addAll(grouperDataRows);
    
    for (GrouperDataRow grouperDataRow: result) {
      configIdToInternalIdCache().put(grouperDataRow.getConfigId(), grouperDataRow.getInternalId());
      internalIdToConfigIdCache().put(grouperDataRow.getInternalId(), grouperDataRow.getConfigId());
    }
   
    return result;
  }
  
  public static void delete(List<GrouperDataRow> grouperDataRows) {
    if (GrouperUtil.length(grouperDataRows) == 0) {
      return;
    }
    
    Set<Long> dataRowInternalIds = new HashSet<>();
    
    for (GrouperDataRow grouperDataRow: grouperDataRows) {
      grouperDataRow.storePrepare();
      dataRowInternalIds.add(grouperDataRow.getInternalId());
    }
    
    List<GrouperDataAlias> aliases = GrouperDataAliasDao.selectByDataRowInternalIds(dataRowInternalIds);
    GrouperDataAliasDao.delete(aliases);
    
    List<GrouperDataRowFieldAssign> dataRowFieldAssigns = GrouperDataRowFieldAssignDao.selectByDataRowInternalIds(dataRowInternalIds);
    GrouperDataRowFieldAssignDao.delete(dataRowFieldAssigns);
    
    List<GrouperDataRowAssign> dataRowAssigns = GrouperDataRowAssignDao.selectByDataRowInternalIds(dataRowInternalIds);
    GrouperDataRowAssignDao.delete(dataRowAssigns);
    
    List<GrouperDataRowFieldAssignHst> dataRowFieldAssignHsts = GrouperDataRowFieldAssignHstDao.selectByDataRowInternalIds(dataRowInternalIds);
    GrouperDataRowFieldAssignHstDao.delete(dataRowFieldAssignHsts);
    
    List<GrouperDataRowAssignHst> dataRowAssignHsts = GrouperDataRowAssignHstDao.selectByDataRowInternalIds(dataRowInternalIds);
    GrouperDataRowAssignHstDao.delete(dataRowAssignHsts);
    
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataRows);
    
    //TODO remove all things from cache when they are deleted
  }
  

}
