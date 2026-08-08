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

public class GrouperDataFieldConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public String getConfigIdElementIdHandle() {
    return "#dataFieldConfigId";
  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperDataField." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperDataField)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperDataField";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "dataFieldConfigId";
  }
  
  /**
   * list of configured data field configs
   * @return
   */
  public static List<GrouperDataFieldConfiguration> retrieveAllDataFieldConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GrouperDataFieldConfiguration.class.getName());
   return (List<GrouperDataFieldConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
    GrouperConfigurationModuleAttribute fieldAliases = this.retrieveAttributes().get("fieldAliases");

    if (fieldAliases != null && StringUtils.isNotBlank(fieldAliases.getValueOrExpressionEvaluation())) {
      
      String fieldAliasesCommaSeparated = fieldAliases.getValueOrExpressionEvaluation();
      Set<String> fieldAliasesSet = GrouperUtil.splitTrimToSet(fieldAliasesCommaSeparated, ",");
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      grouperDataEngine.loadFieldsAndRows(grouperConfig);
      
      Map<String,GrouperDataFieldConfig> fieldConfigByAlias = grouperDataEngine.getFieldConfigByAlias();
      for (String fieldAliasBeingAdded: fieldAliasesSet) {
        if (fieldConfigByAlias.containsKey(fieldAliasBeingAdded.toLowerCase()) &&
        		!StringUtils.equals(this.getConfigId(), fieldConfigByAlias.get(fieldAliasBeingAdded.toLowerCase()).getConfigId())) {
          String errorMessage = GrouperTextContainer.retrieveFromRequest().getText().get("dataFieldRowAliasAlreadyUsedError");
          errorMessage = errorMessage.replace("##dataFieldAlias##", fieldAliasBeingAdded);
          errorsToDisplay.add(errorMessage);
        }
      }
      
      Map<String,GrouperDataRowConfig> rowConfigByAlias = grouperDataEngine.getRowConfigByAlias();
      for (String fieldAliasBeingAdded: fieldAliasesSet) {
        if (rowConfigByAlias.containsKey(fieldAliasBeingAdded.toLowerCase()) &&
        		!StringUtils.equals(this.getConfigId(), rowConfigByAlias.get(fieldAliasBeingAdded.toLowerCase()).getConfigId())) {
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
        
        GrouperDataFieldConfiguration.super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

          GrouperDataEngine.syncDataFields(grouperConfig);
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

    // check if this data field is referenced by any data rows
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(null);

    for (Map.Entry<String, GrouperDataRowConfig> rowEntry : grouperDataEngine.getRowConfigByConfigId().entrySet()) {
      if (rowEntry.getValue().getDataFieldConfigIds().contains(configId)) {
        referencingConfigs.add("data row '" + rowEntry.getKey() + "'");
      }
    }

    // check if this data field is referenced by any data provider queries
    List<GrouperDataProviderQueryConfiguration> allQueryConfigs = GrouperDataProviderQueryConfiguration.retrieveAllDataProviderQueryConfigurations();
    for (GrouperDataProviderQueryConfiguration queryConfig : GrouperUtil.nonNull(allQueryConfigs)) {
      String numberOfFieldsString = queryConfig.retrieveAttributeValueFromConfig("providerQueryNumberOfDataFields", false);
      int numberOfFields = GrouperUtil.intValue(numberOfFieldsString, 0);
      for (int i = 0; i < numberOfFields; i++) {
        String fieldConfigId = queryConfig.retrieveAttributeValueFromConfig("providerQueryDataField." + i + ".providerDataFieldConfigId", false);
        if (StringUtils.equals(configId, fieldConfigId)) {
          referencingConfigs.add("data provider query '" + queryConfig.getConfigId() + "'");
          break;
        }
      }
    }

    // check if this data field is used by any scripted groups (ABAC)
    List<String> dependentGroupNames = new GcDbAccess().sql("select distinct depen_group_name from grouper_sql_dependency_attr_v where owner_data_field_config_id = ?").addBindVar(configId).selectList(String.class);
    for (String groupName : GrouperUtil.nonNull(dependentGroupNames)) {
      referencingConfigs.add("scripted group '" + groupName + "'");
    }

    if (referencingConfigs.size() > 0) {
      String referencingConfigsString = StringUtils.join(referencingConfigs, ", ");
      throw new GrouperReferentialIntegrityException("Error: cannot delete this data field because it is referenced by: " + referencingConfigsString);
    }

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      @Override
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {

        GrouperDataFieldConfiguration.super.deleteConfig(fromUi);

        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

        GrouperDataEngine.syncDataAliases(grouperConfig);
        GrouperDataEngine.syncDataFields(grouperConfig);

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
        
        
        GrouperDataFieldConfiguration.super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
            actionsPerformed);
        
        if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
          GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

          GrouperDataEngine.syncDataFields(grouperConfig);
          GrouperDataEngine.syncDataAliases(grouperConfig);
        }
        
        return null;
      }
    });
    
  }
  
}
