package edu.internet2.middleware.grouper.app.sqlSync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class SqlSyncConfiguration extends GrouperConfigurationModuleBase implements OptionValueDriver {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_CLIENT_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperClient.syncTable." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperClient.syncTable)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperClient.syncTable";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "personSource";
  }
  
  /**
   * list of configured sql sync configs
   * @return
   */
  public static List<SqlSyncConfiguration> retrieveAllSqlSyncConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(SqlSyncConfiguration.class.getName());
   return (List<SqlSyncConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
  /**
   * is the config enabled or not
   * @return
   */
  @Override
  public boolean isEnabled() {
   try {
     GrouperConfigurationModuleAttribute enabledAttribute = this.retrieveAttributes().get("enabled");
     String enabledString = enabledAttribute.getValue();
     if (StringUtils.isBlank(enabledString)) {
       enabledString = enabledAttribute.getDefaultValue();
     }
     return GrouperUtil.booleanValue(enabledString, true);
   } catch (Exception e) {
     return false;
   }
    
  }
  
  
  /**
   * change status of config to disable/enable
   * @param enable
   * @param message
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void changeStatus(boolean enable, StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    GrouperConfigurationModuleAttribute enabledAttribute = this.retrieveAttributes().get("enabled");
    enabledAttribute.setValue(enable? "true": "false");
    
    ConfigFileName configFileName = this.getConfigFileName();
    ConfigFileMetadata configFileMetadata = configFileName.configFileMetadata();

    DbConfigEngine.configurationFileAddEditHelper2(configFileName, this.getConfigFileName().getConfigFileName(), configFileMetadata,
        enabledAttribute.getFullPropertyName(), 
        enabledAttribute.isExpressionLanguage() ? "true" : "false", 
        enabledAttribute.isExpressionLanguage() ? enabledAttribute.getExpressionLanguageScript() : enabledAttribute.getValue(),
        enabledAttribute.isPassword(), message, new Boolean[] {false},
        new Boolean[] {false}, true, "Sql sync config status changed", errorsToDisplay, validationErrorsToDisplay, false);    
    ConfigPropertiesCascadeBase.clearCache();
  }
  
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    
    List<SqlSyncConfiguration> sqlSyncConfigs = (List<SqlSyncConfiguration>) (Object) this.listAllConfigurationsOfThisType();
    
    for (SqlSyncConfiguration sqlSyncConfig: sqlSyncConfigs) {
      
      String configId = sqlSyncConfig.getConfigId();
      keysAndLabels.add(new MultiKey(configId, configId));
    }
    
    Collections.sort(keysAndLabels, new Comparator<MultiKey>() {

      @Override
      public int compare(MultiKey o1, MultiKey o2) {
        return ((String)o1.getKey(0)).compareTo((String)o2.getKey(0));
      }
    });
    
    return keysAndLabels;
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (validationErrorsToDisplay.size() == 0 && errorsToDisplay.size() == 0) {
      
      Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();
      
      GrouperConfigurationModuleAttribute tableFromAttribute = attributes.get("tableFrom");
      String tableFrom = tableFromAttribute.getValueOrExpressionEvaluation();
      
      if (!tableFrom.matches("^[a-zA-Z0-9_\\.]+$")) {
        String error = GrouperTextContainer.textOrNull("sqlSyncConfigSaveErrorTableFromContainsIllegalCharacters");
        validationErrorsToDisplay.put(tableFromAttribute.getHtmlForElementIdHandle(), error);
      }
      
      GrouperConfigurationModuleAttribute tableToAttribute = attributes.get("tableTo");
      String tableTo = tableToAttribute.getValueOrExpressionEvaluation();
      
      if (!tableTo.matches("^[a-zA-Z0-9_\\.]+$")) {
        String error = GrouperTextContainer.textOrNull("sqlSyncConfigSaveErrorTableToContainsIllegalCharacters");
        validationErrorsToDisplay.put(tableToAttribute.getHtmlForElementIdHandle(), error);
      }
      
    }
    
    
  }
  
  

}
