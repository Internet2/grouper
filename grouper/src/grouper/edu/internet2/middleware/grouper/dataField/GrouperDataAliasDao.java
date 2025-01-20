package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.Collection;
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
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * dao for data aliases
 * @author mchyzer
 *
 */
public class GrouperDataAliasDao {


  public GrouperDataAliasDao() {
  }

  public static List<GrouperDataAlias> selectAllFieldAliases() {
    return new GcDbAccess().sql("select * from grouper_data_alias where alias_type = 'F'").selectList(GrouperDataAlias.class);

  }

  public static List<GrouperDataAlias> selectAllRowAliases() {
    return new GcDbAccess().sql("select * from grouper_data_alias where alias_type = 'R'").selectList(GrouperDataAlias.class);

  }

  
  public static List<GrouperDataAlias> selectByDataFieldInternalId(long dataFieldInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_alias where data_field_internal_id = ?")
        .addBindVar(dataFieldInternalId)
        .selectList(GrouperDataAlias.class);

  }
    
  public static List<GrouperDataAlias> selectByDataFieldInternalIds(Set<Long> dataFieldInternalIds) {
    if (dataFieldInternalIds == null || dataFieldInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_alias ")
        .selectMultipleColumnName("data_field_internal_id")
        .bindVars(new ArrayList<Long>(dataFieldInternalIds))
        .selectList(GrouperDataAlias.class);
    
  }
  
  public static List<GrouperDataAlias> selectByDataRowInternalId(long dataRowInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_alias where data_row_internal_id = ?")
        .addBindVar(dataRowInternalId)
        .selectList(GrouperDataAlias.class);
  }

  public static List<GrouperDataAlias> selectByDataRowInternalIds(Set<Long> dataRowInternalIds) {
    if (dataRowInternalIds == null || dataRowInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_alias ")
        .selectMultipleColumnName("data_row_internal_id")
        .bindVars(new ArrayList<Long>(dataRowInternalIds))
        .selectList(GrouperDataAlias.class);
    
  }
  
  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataAlias.class)).executeSql();
  }

  /**
   * @param grouperDataAlias
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataAlias grouperDataAlias) {
    
    GrouperUtil.assertion(grouperDataAlias != null, "grouperDataAlias is null");
    
    boolean isInsert = grouperDataAlias.getInternalId() == -1;

    grouperDataAlias.storePrepare();

    if (!isInsert) {
      boolean changed = new GcDbAccess().storeToDatabase(grouperDataAlias);
      return changed;
    }

    RuntimeException runtimeException = null;
    // might be other places saving the same data field alias
    for (int i=0;i<5;i++) {
      try {
        new GcDbAccess().storeToDatabase(grouperDataAlias);
        return true;
      } catch (RuntimeException re) {
        runtimeException = re;
        GrouperUtil.sleep(100 * (i+1));
        GrouperDataAlias grouperDataAliasNew = selectByLowerName(grouperDataAlias.getName());
        if (grouperDataAliasNew != null) {
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

  public static GrouperDataAlias selectByLowerName(String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    GrouperDataAlias grouperDataAlias = new GcDbAccess().sql("select * from grouper_data_alias where lower_name = ?")
        .addBindVar(StringUtils.lowerCase(name)).select(GrouperDataAlias.class);
    return grouperDataAlias;
  }
  
  public static Set<GrouperDataAlias> selectByLowerNames(Set<String> lowerNames) {
    if (lowerNames == null || lowerNames.size() == 0) {
      return new HashSet<>();
    }
    
    Set<GrouperDataAlias> result = new HashSet<>();
    
    List<GrouperDataAlias> grouperDataFields = new GcDbAccess().sql("select * from grouper_data_alias ")
        .selectMultipleColumnName("lower_name")
        .bindVars(new ArrayList<String>(lowerNames))
        .selectList(GrouperDataAlias.class);
    
    result.addAll(grouperDataFields);
    
    for (GrouperDataAlias grouperDataAlias: result) {
      lowerNameToInternalIdCache().put(grouperDataAlias.getLowerName(), grouperDataAlias.getInternalId());
      internalIdToLowerNameCache().put(grouperDataAlias.getInternalId(), grouperDataAlias.getLowerName());
    }
   
    return result;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDataAlias grouperDataAlias) {
    grouperDataAlias.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataAlias);
  }

  public static void delete(Collection<GrouperDataAlias> grouperDataAliases) {
    for (GrouperDataAlias grouperDataAlias: grouperDataAliases) {      
      grouperDataAlias.storePrepare();
    }
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataAliases);
  }

  /**
   * cache, use the method to get this
   */
  private static ExpirableCache<String, Long> lowerNameToInternalIdCache = null;
  
  /**
   * max terms in memory
   */
  private static int maxTermsInMemoryCache = 50000;
  
  /**
   * data field alias cache
   * @return the cache
   */
  private static ExpirableCache<String, Long> lowerNameToInternalIdCache() {
    if (lowerNameToInternalIdCache == null) {
      lowerNameToInternalIdCache = new ExpirableCache<String, Long>(60);
    }
    return lowerNameToInternalIdCache;
  }
  
  /**
   * cache, use the method to get this
   */
  private static ExpirableCache<Long, String> internalIdToLowerNameCache = null;
  
  /**
   * data field alias cache
   * @return the cache
   */
  private static ExpirableCache<Long, String> internalIdToLowerNameCache() {
    if (internalIdToLowerNameCache == null) {
      internalIdToLowerNameCache = new ExpirableCache<Long, String>(60);
    }
    return internalIdToLowerNameCache;
  }

  /**
   * @param dataFieldInternalId 
   * @param name 
   * @return the data field alias
   */
  public static Long findOrAddFieldAlias(Long dataFieldInternalId, String name) {
    return findOrAddHelper(dataFieldInternalId, null, name);
  }
  

  /**
   * @param names
   * @return
   */
  public static void insertMissingAliases(Long dataFieldInternalId, Long dataRowInternalId, Set<String> names) {
    
    if (CollectionUtils.isEmpty(names)) {
      return;
    }
    
    Set<String> lowerNames = new HashSet<>();
    for (String nameOne: names) {
      String lowerName = StringUtils.lowerCase(nameOne);
      lowerNames.add(lowerName);
    }
    selectByLowerNames(lowerNames); // this will populate the cache
    
    List<GrouperDataAlias> aliasToInsert = new ArrayList<>();
    Set<String> namesToInsert = new HashSet<>();
    
    for (String name: names) {
      Long internalId = lowerNameToInternalIdCache().get(name);
      if (internalId == null) {
        GrouperDataAlias grouperDataAlias = new GrouperDataAlias();
        grouperDataAlias.setDataFieldInternalId(dataFieldInternalId);
        grouperDataAlias.setDataRowInternalId(dataRowInternalId);
        grouperDataAlias.setAliasType(dataFieldInternalId != null ? "F" : "R");
        grouperDataAlias.setName(name);
        String lowerName = StringUtils.lowerCase(name);
        grouperDataAlias.setLowerName(lowerName);
        grouperDataAlias.storePrepare();
        aliasToInsert.add(grouperDataAlias);
        namesToInsert.add(name);
      }
    }
    
    if (aliasToInsert.size() == 0) {
      return;
    }
    
    // get ids in one fell swoop
    List<Long> ids = TableIndex.reserveIds(TableIndexType.dataAlias, aliasToInsert.size());
    int i = 0;
    for (GrouperDataAlias grouperDataAlias: aliasToInsert) {
      grouperDataAlias.setTempInternalIdOnDeck(ids.get(i));
      i++;
    }
    
    int storeBatchToDatabase = new GcDbAccess().retryBatchStoreFailures(true).storeBatchToDatabase(aliasToInsert, 1000);
    if (storeBatchToDatabase != aliasToInsert.size()) {
      GrouperUtil.sleep(400); // Maybe there are other transactions that are working on the same objects, let's wait for them to finish.
      // Maybe they will insert the missing ones and then we can select 
    }
    
    selectByLowerNames(lowerNames); // this is going to populate the cache

  }

  /**
   * @param dataRowInternalId 
   * @param name 
   * @return the data field alias
   */
  public static Long findOrAddRowAlias(Long dataRowInternalId, String name) {
    return findOrAddHelper(null, dataRowInternalId, name);
  }

  /**
   * @param dataFieldInternalId 
   * @param name 
   * @return the data field alias
   */
  private static Long findOrAddHelper(Long dataFieldInternalId, Long dataRowInternalId, String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }

    GrouperUtil.assertion((dataFieldInternalId == null) != (dataRowInternalId == null), 
        "Either pass in a dataField internal ID or a dataRow internal ID but not neither or both!");
    
    String lowerName = StringUtils.lowerCase(name);

    Long internalId = lowerNameToInternalIdCache().get(name);
    if (internalId == null) {
      GrouperDataAlias grouperDataAlias = new GrouperDataAlias();
      grouperDataAlias.setDataFieldInternalId(dataFieldInternalId);
      grouperDataAlias.setDataRowInternalId(dataRowInternalId);
      grouperDataAlias.setAliasType(dataFieldInternalId != null ? "F" : "R");
      grouperDataAlias.setName(name);
      grouperDataAlias.setLowerName(lowerName);
      store(grouperDataAlias);
      internalId = grouperDataAlias.getInternalId();
      
      if (lowerNameToInternalIdCache().size(false) < maxTermsInMemoryCache) {
        lowerNameToInternalIdCache().put(lowerName, internalId);
        internalIdToLowerNameCache().put(internalId, lowerName);
      }
    }
    return internalId;
  }
  
}
