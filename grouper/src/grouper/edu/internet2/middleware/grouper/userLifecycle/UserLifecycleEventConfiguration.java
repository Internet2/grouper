package edu.internet2.middleware.grouper.userLifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.internal.Engine;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowDao;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependency;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyType;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyTypeDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheHistoryFullSyncDaemon;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UserLifecycleEventConfiguration extends GrouperConfigurationModuleBase implements OptionValueDriver {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperUserLifecycleEvent." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperUserLifecycleEvent)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperUserLifecycleEvent";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "userLifecycleEventConfigId";
  }
  
  /**
   * list of configured data field configs
   * @return
   */
  public static List<UserLifecycleEventConfiguration> retrieveAllUserLifecycleEventsConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(UserLifecycleEventConfiguration.class.getName());
   return (List<UserLifecycleEventConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
  
  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
    
    GrouperConfigurationModuleAttribute naturalLanguageDescriptionJexlPrivilegedAttribute = attributes.get("naturalLanguageDescriptionJexlPrivileged");
    String jexlScript = naturalLanguageDescriptionJexlPrivilegedAttribute.getValueOrExpressionEvaluationValue();
    
    jexlScript = jexlScript.trim();
    if (jexlScript.startsWith("${") && jexlScript.endsWith("}")) {
      jexlScript = jexlScript.substring(2, jexlScript.length()-1);
    }
    
    JexlEngine jexlEngine = new Engine();

    // TODO dont mess with values in strings
    jexlScript = GrouperUtil.replace(jexlScript, "\n", " ");
    jexlScript = GrouperUtil.replace(jexlScript, "\r", " ");
    jexlScript = jexlScript.replaceAll("!\\s+", "!");
    
    try {      
      JexlExpression expression = (JexlExpression)jexlEngine.createExpression(jexlScript);
    } catch(Exception e) {
      validationErrorsToDisplay.put(naturalLanguageDescriptionJexlPrivilegedAttribute.getHtmlForElementIdHandle(), "Invalid jexl script for naturalLanguageDescriptionJexlPrivileged");
      return;
    }
    
    GrouperConfigurationModuleAttribute naturalLanguageDescriptionJexlUnprivilegedAttribute = attributes.get("naturalLanguageDescriptionJexlUnprivileged");
    jexlScript = naturalLanguageDescriptionJexlUnprivilegedAttribute.getValueOrExpressionEvaluationValue();
    
    jexlScript = jexlScript.trim();
    if (jexlScript.startsWith("${") && jexlScript.endsWith("}")) {
      jexlScript = jexlScript.substring(2, jexlScript.length()-1);
    }
    
    jexlEngine = new Engine();

    // TODO dont mess with values in strings
    jexlScript = GrouperUtil.replace(jexlScript, "\n", " ");
    jexlScript = GrouperUtil.replace(jexlScript, "\r", " ");
    jexlScript = jexlScript.replaceAll("!\\s+", "!");
    
    try {      
      JexlExpression expression = (JexlExpression)jexlEngine.createExpression(jexlScript);
    } catch(Exception e) {
      validationErrorsToDisplay.put(naturalLanguageDescriptionJexlPrivilegedAttribute.getHtmlForElementIdHandle(), "Invalid jexl script for naturalLanguageDescriptionJexlUnprivileged");
      return;
    }
    
    GrouperConfigurationModuleAttribute groupUUIDOrNameAttribute = attributes.get("groupUserAddGroup");
    if (groupUUIDOrNameAttribute != null && StringUtils.isNotBlank(groupUUIDOrNameAttribute.getValueOrExpressionEvaluationValue()) ) {
      String groupUuidOrName = groupUUIDOrNameAttribute.getValueOrExpressionEvaluationValue();
      
      Group group = GroupFinder.findByUuid(groupUuidOrName, false);
      if (group == null) {
        group = GroupFinder.findByName(groupUuidOrName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("userLifecycleEventConfigSaveErrorGroupNotFound");
        error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
        validationErrorsToDisplay.put(groupUUIDOrNameAttribute.getHtmlForElementIdHandle(), error);
      }
    }
    
    groupUUIDOrNameAttribute = attributes.get("groupUserRemoveGroup");
    if (groupUUIDOrNameAttribute != null && StringUtils.isNotBlank(groupUUIDOrNameAttribute.getValueOrExpressionEvaluationValue()) ) {
      String groupUuidOrName = groupUUIDOrNameAttribute.getValueOrExpressionEvaluationValue();
      
      Group group = GroupFinder.findByUuid(groupUuidOrName, false);
      if (group == null) {
        group = GroupFinder.findByName(groupUuidOrName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("userLifecycleEventConfigSaveErrorGroupNotFound");
        error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
        validationErrorsToDisplay.put(groupUUIDOrNameAttribute.getHtmlForElementIdHandle(), error);
      }
    }
    
    groupUUIDOrNameAttribute = attributes.get("naturalLanguageDescriptionJexlPrivilegedGroupIdOrName");
    if (groupUUIDOrNameAttribute != null && StringUtils.isNotBlank(groupUUIDOrNameAttribute.getValueOrExpressionEvaluationValue()) ) {
      String groupUuidOrName = groupUUIDOrNameAttribute.getValueOrExpressionEvaluationValue();
      
      Group group = GroupFinder.findByUuid(groupUuidOrName, false);
      if (group == null) {
        group = GroupFinder.findByName(groupUuidOrName, false);
      }
      
      if (group == null) {
        String error = GrouperTextContainer.textOrNull("userLifecycleEventConfigSaveErrorGroupNotFound");
        error = GrouperUtil.replace(error, "$$groupUUIDOrName$$", groupUuidOrName);
        validationErrorsToDisplay.put(groupUUIDOrNameAttribute.getHtmlForElementIdHandle(), error);
      }
    }
    
    GrouperConfigurationModuleAttribute stemUUIDOrNameAttribute = attributes.get("groupUserRemoveFromFolder");
    if (stemUUIDOrNameAttribute != null && StringUtils.isNotBlank(stemUUIDOrNameAttribute.getValueOrExpressionEvaluationValue()) ) {
      String stemUuidOrName = stemUUIDOrNameAttribute.getValueOrExpressionEvaluationValue();
      
      Stem stem = (Stem)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Stem stem = StemFinder.findByUuid(grouperSession, stemUuidOrName, false);
          if (stem != null) {
            return stem;
          }
          
          stem = StemFinder.findByName(grouperSession, stemUuidOrName, false);
          return stem;
        }
      });
      
      
      if (stem == null) {
        String error = GrouperTextContainer.textOrNull("userLifecycleEventConfigSaveErrorStemNotFound");
        error = GrouperUtil.replace(error, "$$stemUuidOrName$$", stemUuidOrName);
        validationErrorsToDisplay.put(groupUUIDOrNameAttribute.getHtmlForElementIdHandle(), error);
      }
    }
    
    GrouperConfigurationModuleAttribute groupUserRemoveDataFieldConfigIdAttribute = attributes.get("groupUserRemoveDataFieldConfigId");
    if (groupUserRemoveDataFieldConfigIdAttribute != null && StringUtils.isNotBlank(groupUserRemoveDataFieldConfigIdAttribute.getValueOrExpressionEvaluationValue()) ) {
      String configId = groupUserRemoveDataFieldConfigIdAttribute.getValueOrExpressionEvaluationValue();
      
      GrouperDataField grouperDataField = GrouperDataFieldDao.selectByText(configId);
      
      if (grouperDataField == null) {
        String error = GrouperTextContainer.textOrNull("userLifecycleEventConfigSaveErrorDataFieldNotFound");
        error = GrouperUtil.replace(error, "$$configId$$", configId);
        validationErrorsToDisplay.put(groupUUIDOrNameAttribute.getHtmlForElementIdHandle(), error);
      }
    }
    
    GrouperConfigurationModuleAttribute groupUserRemoveDataRowConfigIdAttribute = attributes.get("groupUserRemoveDataRowConfigId");
    if (groupUserRemoveDataRowConfigIdAttribute != null && StringUtils.isNotBlank(groupUserRemoveDataRowConfigIdAttribute.getValueOrExpressionEvaluationValue()) ) {
      String configId = groupUserRemoveDataRowConfigIdAttribute.getValueOrExpressionEvaluationValue();
      
      GrouperDataRow grouperDataRow = GrouperDataRowDao.selectByConfigId(configId);
      
      if (grouperDataRow == null) {
        String error = GrouperTextContainer.textOrNull("userLifecycleEventConfigSaveErrorDataRowNotFound");
        error = GrouperUtil.replace(error, "$$configId$$", configId);
        validationErrorsToDisplay.put(groupUUIDOrNameAttribute.getHtmlForElementIdHandle(), error);
      }
    }
    
  }
  
  
  private static void addMembershipHistoryLifecycleDependencies(SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryLifecycle, 
      Collection<SqlCacheGroup> sqlCacheGroupsToCheck, Map<MultiKey, SqlCacheDependency> sqlCacheDependencies) {
    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroupsToCheck) {
      MultiKey multiKey = new MultiKey(sqlCacheGroup.getInternalId(), sqlCacheGroup.getInternalId());
      if (!sqlCacheDependencies.containsKey(multiKey)) {
        // check if other history dependencies
        List<Long> dependenciesFound = new GcDbAccess().sql("select gscdt.internal_id from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt "
            + "where gscd.dep_type_internal_id = gscdt.internal_id and gscdt.dependency_category='mshipHistory' and owner_internal_id = ?")
            .addBindVar(sqlCacheGroup.getInternalId())
            .selectList(Long.class);
        
        // add the dependency - check just in case something else added it in the meantime
        if (!dependenciesFound.contains(sqlCacheDependencyTypeMshipHistoryLifecycle.getInternalId())) {
          SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
          sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyTypeMshipHistoryLifecycle.getInternalId());
          sqlCacheDependency.setOwnerInternalId(sqlCacheGroup.getInternalId());
          sqlCacheDependency.setDependentInternalId(sqlCacheGroup.getInternalId());
          SqlCacheDependencyDao.store(sqlCacheDependency);
          
          sqlCacheDependencies.put(multiKey, sqlCacheDependency);
        }
        
        if (dependenciesFound.size() == 0) {
          // we need to add the history
          SqlCacheHistoryFullSyncDaemon.syncMembershipHistory(sqlCacheGroup, null, null);
        }
      }
    }
  }
  
  private void prepareAndStoreSqlCacheDependencies(GrouperLifecycleEventConfig lifecycleEventConfig) {
    
    Collection<Long> groupInternalIds = new HashSet<>();
    if (lifecycleEventConfig.getGroupInternalId() != null) {
      groupInternalIds.add(lifecycleEventConfig.getGroupInternalId());
    } else if (lifecycleEventConfig.getStemIdIndex() != null) {
      
      //fetch group internal ids for the given folder
      String sql = """
          select 
            gg.internal_id
          from grouper_groups gg
          join grouper_stem_set gss
            on gss.if_has_stem_id = gg.parent_stem
          join grouper_stems gs
            on gs.id = gss.then_has_stem_id
          where gs.id_index = ?          
            and gg.enabled = 'T'
          order by gg.display_name;
          """;
      List<Long> groupInternalIdsForGroups = new GcDbAccess().sql(sql).addBindVar(lifecycleEventConfig.getStemIdIndex()).selectList(Long.class);
      groupInternalIds.addAll(groupInternalIdsForGroups);
    }
    
    Set<MultiKey> groupInternalIdsFieldInternalIds = new HashSet<MultiKey>();
    for (Long groupInternalId: groupInternalIds) {
      MultiKey groupInternalIdFieldInternalId = new MultiKey(groupInternalId, Group.getDefaultList().getInternalId());
      groupInternalIdsFieldInternalIds.add(groupInternalIdFieldInternalId);
    }
    
    Collection<SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(groupInternalIdsFieldInternalIds).values();
    
    SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryLifecycle = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", SqlCacheDependencyTypeDao.NAME_MSHIP_HISTORY_LIFECYCLE);
    Set<MultiKey> ownerInternalIdsDependentInternalIds = new HashSet<>();
    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroups) {
      ownerInternalIdsDependentInternalIds.add(new MultiKey(sqlCacheGroup.getInternalId(), sqlCacheGroup.getInternalId()));
    }
    Map<MultiKey, SqlCacheDependency> sqlCacheDependencies = SqlCacheDependencyDao.retrieveByDepTypeInternalIdAndOwnerInternalIdsDependentInternalIds(sqlCacheDependencyTypeMshipHistoryLifecycle.getInternalId(), ownerInternalIdsDependentInternalIds);
    // go through and see which ones don't have the mshipHistory_lifecycle dependency
    addMembershipHistoryLifecycleDependencies(sqlCacheDependencyTypeMshipHistoryLifecycle, sqlCacheGroups, sqlCacheDependencies);
  }
  
  
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay,
      List<String> actionsPerformed) {
    
    final String CONFIGID = this.getConfigId();    
    
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        UserLifecycleEventConfiguration.super.clearAttributeCache();
        ConfigPropertiesCascadeBase.clearCache();
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

          UserLifecycleEngine.syncUserLifecycleEventConfigs(grouperConfig);
          
          //lifecycle event config that was just inserted by the method call above
          GrouperLifecycleEventConfig lifecycleEventConfig = UserLifecycleEventConfigDao.selectByText(CONFIGID);
          if (lifecycleEventConfig != null) {      
            //it should never be null
            prepareAndStoreSqlCacheDependencies(lifecycleEventConfig);
          }
        }
        
        return null;
      }
    });
    
  }

  @Override
  public void deleteConfig(boolean fromUi) {
    
    final String CONFIGID = this.getConfigId();    
    
    super.deleteConfig(fromUi);
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        UserLifecycleEventConfiguration.super.clearAttributeCache();
        ConfigPropertiesCascadeBase.clearCache();
        
        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
        
        //lifecycle event config that is going to be deleted by the method call below
        GrouperLifecycleEventConfig lifecycleEventConfig = UserLifecycleEventConfigDao.selectByText(CONFIGID);
        
        UserLifecycleEngine.syncUserLifecycleEventConfigs(grouperConfig);
        
        Collection<Long> groupInternalIds = new HashSet<>();
        if (lifecycleEventConfig != null && lifecycleEventConfig.getGroupInternalId() != null) {
          groupInternalIds.add(lifecycleEventConfig.getGroupInternalId());
        } else if (lifecycleEventConfig.getStemIdIndex() != null) {
          
          //fetch group internal ids for the given folder
          String sql = """
              select 
                gg.internal_id
              from grouper_groups gg
              join grouper_stem_set gss
                on gss.if_has_stem_id = gg.parent_stem
              join grouper_stems gs
                on gs.id = gss.then_has_stem_id
              where gs.id_index = ?          
                and gg.enabled = 'T'
              order by gg.display_name;
              """;
          List<Long> groupInternalIdsForGroups = new GcDbAccess().sql(sql).addBindVar(lifecycleEventConfig.getStemIdIndex()).selectList(Long.class);
          groupInternalIds.addAll(groupInternalIdsForGroups);
        }
        
        Set<MultiKey> groupInternalIdsFieldInternalIds = new HashSet<MultiKey>();
        for (Long groupInternalId: groupInternalIds) {
          MultiKey groupInternalIdFieldInternalId = new MultiKey(groupInternalId, Group.getDefaultList().getInternalId());
          groupInternalIdsFieldInternalIds.add(groupInternalIdFieldInternalId);
        }
        
        Collection<SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(groupInternalIdsFieldInternalIds).values();
        
        List<Long> dependentGroupCacheInternalIds = sqlCacheGroups.stream().map(g -> g.getInternalId()).collect(Collectors.toList());
        
        SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryLifecycle = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", SqlCacheDependencyTypeDao.NAME_MSHIP_HISTORY_LIFECYCLE);
        
        List<SqlCacheDependency> sqlCacheDependenciesToDelete = SqlCacheDependencyDao.retrieveByDepTypeInternalIdAndDependentCacheInternalIds(sqlCacheDependencyTypeMshipHistoryLifecycle.getInternalId(), dependentGroupCacheInternalIds);
        
        for (SqlCacheDependency dependency : sqlCacheDependenciesToDelete) {          
          SqlCacheDependencyDao.delete(dependency);
        }
        
        for (SqlCacheGroup sqlCacheGroup: sqlCacheGroups) {          
          SqlCacheHistoryFullSyncDaemon.syncMembershipHistory(sqlCacheGroup, null, null);
        }
        
        return null;
      }
    });
  
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay, 
      Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    
    final String CONFIGID = this.getConfigId();    
    
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        UserLifecycleEventConfiguration.super.clearAttributeCache();
        ConfigPropertiesCascadeBase.clearCache();
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
          
          //lifecycle event config that is going to be updated. We need to retrieve it so that we can compare it with after the save 
          GrouperLifecycleEventConfig lifecycleEventConfigBefore = UserLifecycleEventConfigDao.selectByText(CONFIGID);

          UserLifecycleEngine.syncUserLifecycleEventConfigs(grouperConfig);
          
          //lifecycle event config that was just edited by the method call above.
          GrouperLifecycleEventConfig lifecycleEventConfigAfter = UserLifecycleEventConfigDao.selectByText(CONFIGID);
          
          Long beforeGroupId = lifecycleEventConfigBefore.getGroupInternalId();
          Long afterGroupId  = lifecycleEventConfigAfter.getGroupInternalId();

          Long beforeStemId = lifecycleEventConfigBefore.getStemIdIndex();
          Long afterStemId  = lifecycleEventConfigAfter.getStemIdIndex();

          // Compare groupInternalId
          if (!Objects.equals(beforeGroupId, afterGroupId)) {
              // Logic when groupInternalId changed
            
            //remove sql cache dependency from the before group
            Set<MultiKey> groupInternalIdsFieldInternalIds = new HashSet<MultiKey>();
            for (Long groupInternalId: GrouperUtil.nonNull(GrouperUtil.toSet(beforeGroupId))) {
              MultiKey groupInternalIdFieldInternalId = new MultiKey(groupInternalId, Group.getDefaultList().getInternalId());
              groupInternalIdsFieldInternalIds.add(groupInternalIdFieldInternalId);
            }
            
            Collection<SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(groupInternalIdsFieldInternalIds).values();
            
            List<Long> dependentGroupCacheInternalIds = sqlCacheGroups.stream().map(g -> g.getInternalId()).collect(Collectors.toList());
            
            SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryLifecycle = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", SqlCacheDependencyTypeDao.NAME_MSHIP_HISTORY_LIFECYCLE);
            
            List<SqlCacheDependency> sqlCacheDependenciesToDelete = SqlCacheDependencyDao.retrieveByDepTypeInternalIdAndDependentCacheInternalIds(sqlCacheDependencyTypeMshipHistoryLifecycle.getInternalId(), dependentGroupCacheInternalIds);
            
            for (SqlCacheDependency dependency : sqlCacheDependenciesToDelete) {          
              SqlCacheDependencyDao.delete(dependency);
            }
            
            for (SqlCacheGroup sqlCacheGroup: sqlCacheGroups) {         
              SqlCacheHistoryFullSyncDaemon.syncMembershipHistory(sqlCacheGroup, null, null);
            }
            
            prepareAndStoreSqlCacheDependencies(lifecycleEventConfigAfter);
            
          }

          // Compare stemIdIndex
          if (!Objects.equals(beforeStemId, afterStemId)) {
            // Logic when stemIdIndex changed
            
            //fetch group internal ids for the given folder so that we can delete their associated cache dependencies
            String sql = """
                select
                  gg.internal_id
                from grouper_groups gg
                join grouper_stem_set gss
                  on gss.if_has_stem_id = gg.parent_stem
                join grouper_stems gs
                  on gs.id = gss.then_has_stem_id
                where gs.id_index = ?          
                  and gg.enabled = 'T'
                order by gg.display_name;
                """;
            List<Long> groupInternalIdsForGroups = new GcDbAccess().sql(sql).addBindVar(beforeStemId).selectList(Long.class);
            
            Set<MultiKey> groupInternalIdsFieldInternalIds = new HashSet<MultiKey>();
            for (Long groupInternalId: groupInternalIdsForGroups) {
              MultiKey groupInternalIdFieldInternalId = new MultiKey(groupInternalId, Group.getDefaultList().getInternalId());
              groupInternalIdsFieldInternalIds.add(groupInternalIdFieldInternalId);
            }
            
            Collection<SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(groupInternalIdsFieldInternalIds).values();
            
            List<Long> dependentGroupCacheInternalIds = sqlCacheGroups.stream().map(g -> g.getInternalId()).collect(Collectors.toList());
            
            SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryLifecycle = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", SqlCacheDependencyTypeDao.NAME_MSHIP_HISTORY_LIFECYCLE);
            
            List<SqlCacheDependency> sqlCacheDependenciesToDelete = SqlCacheDependencyDao.retrieveByDepTypeInternalIdAndDependentCacheInternalIds(sqlCacheDependencyTypeMshipHistoryLifecycle.getInternalId(), dependentGroupCacheInternalIds);
            
            for (SqlCacheDependency dependency : sqlCacheDependenciesToDelete) {          
              SqlCacheDependencyDao.delete(dependency);
            }
            
            for (SqlCacheGroup sqlCacheGroup: sqlCacheGroups) {          
              SqlCacheHistoryFullSyncDaemon.syncMembershipHistory(sqlCacheGroup, null, null);
            }
              
            prepareAndStoreSqlCacheDependencies(lifecycleEventConfigAfter);
          
          }
          
        }
        
        return null;
      }
    });
    
  }

  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    
    List<UserLifecycleEventConfiguration> configs = (List<UserLifecycleEventConfiguration>) (Object) this.listAllConfigurationsOfThisType();
    
    for (UserLifecycleEventConfiguration config: configs) {
      
      if (config.isEnabled()) {
        String configId = config.getConfigId();
        keysAndLabels.add(new MultiKey(configId, configId));
      }
      
    }
    
    Collections.sort(keysAndLabels, new Comparator<MultiKey>() {

      @Override
      public int compare(MultiKey o1, MultiKey o2) {
        return ((String)o1.getKey(0)).compareTo((String)o2.getKey(0));
      }
    });
    
    return keysAndLabels;
    
  }
  
  

}
