package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.exception.GrouperReferentialIntegrityException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperDataRowConfiguration extends GrouperConfigurationModuleBase {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperDataRow." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperDataRow)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperDataRow";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "dataRowConfigId";
  }
  
  /**
   * list of configured data row configs
   * @return
   */
  public static List<GrouperDataRowConfiguration> retrieveAllDataRowConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GrouperDataRowConfiguration.class.getName());
   return (List<GrouperDataRowConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
    GrouperConfigurationModuleAttribute rowAliases = this.retrieveAttributes().get("rowAliases");

    if (rowAliases != null && StringUtils.isNotBlank(rowAliases.getValueOrExpressionEvaluation())) {
      
      String rowAliasesCommaSeparated = rowAliases.getValueOrExpressionEvaluation();
      Set<String> rowAliasesSet = GrouperUtil.splitTrimToSet(rowAliasesCommaSeparated, ",");
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      grouperDataEngine.loadFieldsAndRows(grouperConfig);
      
      Map<String,GrouperDataFieldConfig> fieldConfigByAlias = grouperDataEngine.getFieldConfigByAlias();
      for (String fieldAliasBeingAdded: rowAliasesSet) {
        if (fieldConfigByAlias.containsKey(fieldAliasBeingAdded.toLowerCase())) {
          String errorMessage = GrouperTextContainer.retrieveFromRequest().getText().get("dataFieldRowAliasAlreadyUsedError");
          errorMessage = errorMessage.replace("##dataFieldAlias##", fieldAliasBeingAdded);
          errorsToDisplay.add(errorMessage);
        }
      }
      
      Map<String,GrouperDataRowConfig> rowConfigByAlias = grouperDataEngine.getRowConfigByAlias();

      GrouperDataRowConfig grouperDataRowConfig = grouperDataEngine.getRowConfigByConfigId().get(this.getConfigId());

      for (String fieldAliasBeingAdded: rowAliasesSet) {
        
        GrouperDataRowConfig thisGrouperDataRowConfig = rowConfigByAlias.get(fieldAliasBeingAdded);

        boolean conflicts = rowConfigByAlias.containsKey(fieldAliasBeingAdded.toLowerCase()) && isInsert;
        if (!isInsert) {
          if (thisGrouperDataRowConfig != null && grouperDataRowConfig != null 
              && !StringUtils.equals(thisGrouperDataRowConfig.getConfigId(), grouperDataRowConfig.getConfigId())) {
            conflicts = true;
          }
        }
        if (conflicts) {
          String errorMessage = GrouperTextContainer.retrieveFromRequest().getText().get("dataFieldRowAliasAlreadyUsedError");
          errorMessage = errorMessage.replace("##dataFieldAlias##", fieldAliasBeingAdded);
          errorsToDisplay.add(errorMessage);
        }
      }
        
    }
    
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay,
      List<String> actionsPerformed) {
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        GrouperDataRowConfiguration.super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

          GrouperDataEngine.syncDataRows(grouperConfig);
          GrouperDataEngine.syncDataAliases(grouperConfig);
        }
        
        return null;
      }
    });
    
  }

  @Override
  public void deleteConfig(boolean fromUi) {

    String configId = this.getConfigId();

    List<String> referencingConfigs = new ArrayList<>();

    // check if this data row is referenced by any data provider queries
    List<GrouperDataProviderQueryConfiguration> allQueryConfigs = GrouperDataProviderQueryConfiguration.retrieveAllDataProviderQueryConfigurations();
    for (GrouperDataProviderQueryConfiguration queryConfig : GrouperUtil.nonNull(allQueryConfigs)) {
      String rowConfigId = queryConfig.retrieveAttributeValueFromConfig("providerQueryRowConfigId", false);
      if (StringUtils.equals(configId, rowConfigId)) {
        referencingConfigs.add("data provider query '" + queryConfig.getConfigId() + "'");
      }
    }

    // check if this data row is used by any scripted groups (ABAC)
    List<String> dependentGroupNames = new GcDbAccess().sql("select distinct depen_group_name from grouper_sql_dependency_row_v where owner_data_row_config_id = ?").addBindVar(configId).selectList(String.class);
    for (String groupName : GrouperUtil.nonNull(dependentGroupNames)) {
      referencingConfigs.add("scripted group '" + groupName + "'");
    }

    if (referencingConfigs.size() > 0) {
      String referencingConfigsString = StringUtils.join(referencingConfigs, ", ");
      throw new GrouperReferentialIntegrityException("Error: cannot delete this data row because it is referenced by: " + referencingConfigsString);
    }

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {

        GrouperDataRowConfiguration.super.deleteConfig(fromUi);

        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

        GrouperDataEngine.syncDataAliases(grouperConfig);
        GrouperDataEngine.syncDataRows(grouperConfig);

        return null;
      }
    });

  }
  
  @Override
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay, 
      Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        
        GrouperDataRowConfiguration.super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

          GrouperDataEngine.syncDataRows(grouperConfig);
          GrouperDataEngine.syncDataAliases(grouperConfig);
        }
        
        return null;
      }
    });
    
  }

}
