package edu.internet2.middleware.grouper.dataField;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
    return grouperDataField;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDataField grouperDataField) {
    grouperDataField.storePrepare();
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

  public static List<GrouperDataField> selectAll() {
    return new GcDbAccess().sql("select * from grouper_data_field").selectList(GrouperDataField.class);

  }
  

}
