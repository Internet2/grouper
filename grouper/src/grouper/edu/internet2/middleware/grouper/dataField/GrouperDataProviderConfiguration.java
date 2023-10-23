package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDataProviderConfiguration extends GrouperConfigurationModuleBase {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperDataProvider." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperDataProvider)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperDataProvider";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "dataProviderConfigId";
  }
  
  /**
   * list of configured data provider configs
   * @return
   */
  public static List<GrouperDataProviderConfiguration> retrieveAllDataProviderConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GrouperDataProviderConfiguration.class.getName());
   return (List<GrouperDataProviderConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay,
      List<String> actionsPerformed) {
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay,
        actionsPerformed);
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataEngine.syncDataProviders(grouperConfig);
  }

  @Override
  public void deleteConfig(boolean fromUi) {
    super.deleteConfig(fromUi);
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataEngine.syncDataProviders(grouperConfig);
  }

}
