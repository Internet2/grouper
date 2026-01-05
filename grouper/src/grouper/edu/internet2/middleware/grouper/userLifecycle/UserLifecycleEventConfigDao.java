package edu.internet2.middleware.grouper.userLifecycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowDao;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public class UserLifecycleEventConfigDao {
  
  
  public UserLifecycleEventConfigDao() {
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperLifecycleEventConfig.class)).executeSql();
  }

  /**
   * @param grouperLifecycleEventConfig
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperLifecycleEventConfig grouperLifecycleEventConfig) {
    
    GrouperUtil.assertion(grouperLifecycleEventConfig != null, "grouperLifecycleEventConfig is null");
    
    boolean isInsert = grouperLifecycleEventConfig.getInternalId() == -1;

    grouperLifecycleEventConfig.storePrepare();

    if (!isInsert) {
      boolean changed = new GcDbAccess().storeToDatabase(grouperLifecycleEventConfig);
      return changed;
    }

    RuntimeException runtimeException = null;
    // might be other places saving the same field
    for (int i=0;i<5;i++) {
      boolean created = false;
      try {
        new GcDbAccess().storeToDatabase(grouperLifecycleEventConfig);
        created = true;
        
        return true;
      } catch (RuntimeException re) {
        if (created) {
          throw re;
        }
        runtimeException = re;
        GrouperUtil.sleep(100 * (i+1));
        GrouperLifecycleEventConfig grouperLifecycleEventConfigNew = selectByText(grouperLifecycleEventConfig.getConfigId());
        if (grouperLifecycleEventConfigNew != null) {
          grouperLifecycleEventConfig.setInternalId(grouperLifecycleEventConfigNew.getInternalId());
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

  public static GrouperLifecycleEventConfig selectByText(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }
    GrouperLifecycleEventConfig grouperLifecycleEventConfig = new GcDbAccess().sql("select * from grouper_lifecycle_event_config where config_id = ?")
        .addBindVar(configId).select(GrouperLifecycleEventConfig.class);
    
    if (grouperLifecycleEventConfig != null) {
      configIdToInternalIdCache().put(configId, grouperLifecycleEventConfig.getInternalId());
      internalIdToConfigIdCache().put(grouperLifecycleEventConfig.getInternalId(), configId);
    }
    return grouperLifecycleEventConfig;
  }
  
  public static Set<GrouperLifecycleEventConfig> selectByTexts(Set<String> configIds) {
    if (configIds == null || configIds.size() == 0) {
      return new HashSet<>();
    }
    
    Set<GrouperLifecycleEventConfig> result = new HashSet<>();
    
    List<GrouperLifecycleEventConfig> grouperLifecycleEventConfigs = new GcDbAccess().sql("select * from grouper_lifecycle_event_config ")
        .selectMultipleColumnName("config_id")
        .bindVars(new ArrayList<String>(configIds))
        .selectList(GrouperLifecycleEventConfig.class);
    
    result.addAll(grouperLifecycleEventConfigs);
    
    for (GrouperLifecycleEventConfig grouperLifecycleEventConfig: result) {
      configIdToInternalIdCache().put(grouperLifecycleEventConfig.getConfigId(), grouperLifecycleEventConfig.getInternalId());
      internalIdToConfigIdCache().put(grouperLifecycleEventConfig.getInternalId(), grouperLifecycleEventConfig.getConfigId());
    }
   
    return result;
  }
  
  public static void delete(List<GrouperLifecycleEventConfig> grouperLifecycleEventConfigs) {
    if (GrouperUtil.length(grouperLifecycleEventConfigs) == 0) {
      return;
    }
    
    new GcDbAccess().deleteFromDatabaseMultiple(grouperLifecycleEventConfigs);
    
    //clearing the cache on deletes because if you add the same configId right after deleting it
    // the cache thinks the value is still there and does not add a new row in the grouper_lifecycle_event_config
    configIdToInternalIdCache().clear();
    internalIdToConfigIdCache().clear();
    
  }
  
  /**
   * 
   * @param grouperLifecycleEventConfig
   */
  public static void delete(GrouperLifecycleEventConfig grouperLifecycleEventConfig) {
    grouperLifecycleEventConfig.storePrepare();
    
    new GcDbAccess().deleteFromDatabase(grouperLifecycleEventConfig);
    
    //clearing the cache on deletes because if you add the same configId right after deleting it
    // the cache thinks the value is still there and does not add a new row in the grouper_lifecycle_event_config
    
    configIdToInternalIdCache().clear();
    internalIdToConfigIdCache().clear();
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
//  public static Long findOrAdd(String configId) {
//    if (StringUtils.isBlank(configId)) {
//      return null;
//    }
//
//    Long internalId = configIdToInternalIdCache().get(configId);
//    if (internalId == null) {
//      GrouperLifecycleEventConfig grouperLifecycleEventConfig = new GrouperLifecycleEventConfig();
//      grouperLifecycleEventConfig.setConfigId(configId);
//      store(grouperLifecycleEventConfig);
//      internalId = grouperLifecycleEventConfig.getInternalId();
//      
//      if (configIdToInternalIdCache().size(false) < maxTermsInMemoryCache) {
//        configIdToInternalIdCache().put(configId, internalId);
//        internalIdToConfigIdCache().put(internalId, configId);
//      }
//    }
//    return internalId;
//  }
  
  public static void updateEventLifecycleConfig(GrouperLifecycleEventConfig dbValue, String configId) {
    
    String groupIdOrName = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserAddGroup");
    String groupUserRemoveGroup = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserRemoveGroup");
    String groupUserRemoveFolder = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserRemoveFolder");
    String groupUserRemoveDataFieldConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserRemoveDataFieldConfigId");
    String groupUserRemoveDataRowConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserRemoveDataRowConfigId");
    
    String trigger = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".trigger");
    if (StringUtils.equals(trigger, "groupUserAdd") || StringUtils.equals(trigger, "groupUserRemove")) {
      
      String groupIdName = StringUtils.isNotBlank(groupIdOrName) ? groupIdOrName : groupUserRemoveGroup;
      Group group = GroupFinder.findByUuid(groupIdName, false);
      if (group == null) {
        group = GroupFinder.findByName(groupIdName, false);
      }
      
      if (group == null) {
        throw new RuntimeException("Group not found: "+groupIdName);
      }
      
      dbValue.setGroupInternalId(group.getInternalId());
    }  else {
      dbValue.setGroupInternalId(null);
    }
    
    if (StringUtils.isNotBlank(groupUserRemoveFolder)) {
      
      Stem stem = (Stem)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Stem stem = StemFinder.findByUuid(grouperSession, groupUserRemoveFolder, false);
          if (stem != null) {
            return stem;
          }
          
          stem = StemFinder.findByName(grouperSession, groupUserRemoveFolder, false);
          return stem;
        }
      });
      
      
      if (stem == null) {
        throw new RuntimeException("Stem not found: "+groupUserRemoveFolder);
      }
      
      dbValue.setStemIdIndex(stem.getIdIndex());
    } else {
      dbValue.setStemIdIndex(null);
    }
    
    if (StringUtils.isNotBlank(groupUserRemoveDataFieldConfigId)) {
      
      GrouperDataField grouperDataField = GrouperDataFieldDao.selectByText(groupUserRemoveDataFieldConfigId);
      
      if (grouperDataField == null) {
        throw new RuntimeException("Data field not found: "+groupUserRemoveDataFieldConfigId);
      }
      
      dbValue.setDataFieldInternalId(grouperDataField.getInternalId());
    } else {
      dbValue.setDataFieldInternalId(null);
    }
    
    if (StringUtils.isNotBlank(groupUserRemoveDataRowConfigId)) {
      
      GrouperDataRow grouperDataRow = GrouperDataRowDao.selectByConfigId(groupUserRemoveDataRowConfigId);
      
      if (grouperDataRow == null) {
        throw new RuntimeException("Data row not found: "+groupUserRemoveDataRowConfigId);
      }
      
      dbValue.setDataRowInternalId(grouperDataRow.getInternalId());
    } else {
      dbValue.setDataRowInternalId(null);
    }
    
    new GcDbAccess().storeToDatabase(dbValue);
    
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
    
    List<GrouperLifecycleEventConfig> eventConfigsToInsert = new ArrayList<>();
    Set<String> configIdsToInsert = new HashSet<>();
    
    for (String configId: configIds) {
      Long internalId = configIdToInternalIdCache().get(configId);
      if (internalId == null) {
        
        GrouperLifecycleEventConfig grouperLifecycleEventConfig = new GrouperLifecycleEventConfig();
        
        String groupIdOrName = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserAddGroup");
        if (StringUtils.isNotBlank(groupIdOrName)) {
          Group group = GroupFinder.findByUuid(groupIdOrName, false);
          if (group == null) {
            group = GroupFinder.findByName(groupIdOrName, false);
          }
          
          if (group == null) {
            throw new RuntimeException("Group not found: "+groupIdOrName);
          }
          
          grouperLifecycleEventConfig.setGroupInternalId(group.getInternalId());
        }
        
        groupIdOrName = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserRemoveGroup");
        if (StringUtils.isNotBlank(groupIdOrName)) {
          Group group = GroupFinder.findByUuid(groupIdOrName, false);
          if (group == null) {
            group = GroupFinder.findByName(groupIdOrName, false);
          }
          
          if (group == null) {
            throw new RuntimeException("Group not found: "+groupIdOrName);
          }
          
          grouperLifecycleEventConfig.setGroupInternalId(group.getInternalId());
        }
        
        
        String stemIdOrName = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserRemoveFolder");
        if (StringUtils.isNotBlank(stemIdOrName)) {
          
          Stem stem = (Stem)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              Stem stem = StemFinder.findByUuid(grouperSession, stemIdOrName, false);
              if (stem != null) {
                return stem;
              }
              
              stem = StemFinder.findByName(grouperSession, stemIdOrName, false);
              return stem;
            }
          });
          
          
          if (stem == null) {
            throw new RuntimeException("Stem not found: "+stemIdOrName);
          }
          
          grouperLifecycleEventConfig.setStemIdIndex(stem.getIdIndex());
        }
        
        String dataFieldConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserRemoveDataFieldConfigId");
        if (StringUtils.isNotBlank(dataFieldConfigId)) {
          
          GrouperDataField grouperDataField = GrouperDataFieldDao.selectByText(dataFieldConfigId);
          
          if (grouperDataField == null) {
            throw new RuntimeException("Data field not found: "+dataFieldConfigId);
          }
          
          grouperLifecycleEventConfig.setDataFieldInternalId(grouperDataField.getInternalId());
        }
        
        String dataRowConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".groupUserRemoveDataRowConfigId");
        if (StringUtils.isNotBlank(dataRowConfigId)) {
          
          GrouperDataRow grouperDataRow = GrouperDataRowDao.selectByConfigId(dataRowConfigId);
          
          if (grouperDataRow == null) {
            throw new RuntimeException("Data row not found: "+dataRowConfigId);
          }
          
          grouperLifecycleEventConfig.setDataRowInternalId(grouperDataRow.getInternalId());
        }
        
        
        grouperLifecycleEventConfig.setConfigId(configId);
        grouperLifecycleEventConfig.storePrepare();
        eventConfigsToInsert.add(grouperLifecycleEventConfig);
        
        configIdsToInsert.add(configId);
      }
    }
    
    if (eventConfigsToInsert.size() == 0) {
      return;
    }
    
    // get ids in one fell swoop
    List<Long> ids = TableIndex.reserveIds(TableIndexType.lifecycleEventConfig, eventConfigsToInsert.size());
    int i = 0;
    for (GrouperLifecycleEventConfig grouperLifecycleEventConfig: eventConfigsToInsert) {
      grouperLifecycleEventConfig.setTempInternalIdOnDeck(ids.get(i));
      i++;
    }
    
    int storeBatchToDatabase = new GcDbAccess().retryBatchStoreFailures(true).storeBatchToDatabase(eventConfigsToInsert, 1000);
    if (storeBatchToDatabase != eventConfigsToInsert.size()) {
      GrouperUtil.sleep(400); // Maybe there are other transactions that are working on the same objects, let's wait for them to finish.
      // Maybe they will insert the missing ones and then we can select 
    }
    selectByTexts(configIdsToInsert); // this is going to populate the cache

  }
  
  
  public static List<GrouperLifecycleEventConfig> selectAll() {
    return new GcDbAccess().sql("select * from grouper_lifecycle_event_config").selectList(GrouperLifecycleEventConfig.class);

  }

}
