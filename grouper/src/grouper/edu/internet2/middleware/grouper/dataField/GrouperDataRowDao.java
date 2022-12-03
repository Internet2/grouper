package edu.internet2.middleware.grouper.dataField;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
  

}
