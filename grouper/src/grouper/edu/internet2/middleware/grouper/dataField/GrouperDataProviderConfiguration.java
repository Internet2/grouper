package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.exception.GrouperReferentialIntegrityException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDataProviderConfiguration extends GrouperConfigurationModuleBase {
  
  @Override
  public String getConfigIdElementIdHandle() {
    return "#dataProviderConfigId";
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

    String configId = this.getConfigId();

    List<String> referencingConfigs = new ArrayList<>();

    // check if this data provider is referenced by any queries
    List<GrouperDataProviderQueryConfiguration> allQueryConfigs = GrouperDataProviderQueryConfiguration.retrieveAllDataProviderQueryConfigurations();
    for (GrouperDataProviderQueryConfiguration queryConfig : GrouperUtil.nonNull(allQueryConfigs)) {
      String providerConfigId = queryConfig.retrieveAttributeValueFromConfig("providerConfigId", false);
      if (StringUtils.equals(configId, providerConfigId)) {
        referencingConfigs.add("data provider query '" + queryConfig.getConfigId() + "'");
      }
    }

    // check if this data provider is referenced by any change log queries
    List<GrouperDataProviderChangeLogQueryConfiguration> allChangeLogQueryConfigs = GrouperDataProviderChangeLogQueryConfiguration.retrieveAllDataProviderChangeLogQueryConfigurations();
    for (GrouperDataProviderChangeLogQueryConfiguration changeLogQueryConfig : GrouperUtil.nonNull(allChangeLogQueryConfigs)) {
      String providerConfigId = changeLogQueryConfig.retrieveAttributeValueFromConfig("providerConfigId", false);
      if (StringUtils.equals(configId, providerConfigId)) {
        referencingConfigs.add("data provider change log query '" + changeLogQueryConfig.getConfigId() + "'");
      }
    }

    // check if this data provider is referenced by any daemon jobs
    Pattern daemonPattern = Pattern.compile("^otherJob\\.(.*)\\.dataProviderConfigId$");
    Set<String> daemonConfigIds = GrouperLoaderConfig.retrieveConfig().propertyConfigIds(daemonPattern);
    for (String daemonConfigId : GrouperUtil.nonNull(daemonConfigIds)) {
      String daemonProviderConfigId = GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + daemonConfigId + ".dataProviderConfigId");
      if (StringUtils.equals(configId, daemonProviderConfigId)) {
        referencingConfigs.add("daemon '" + daemonConfigId + "'");
      }
    }

    if (referencingConfigs.size() > 0) {
      String referencingConfigsString = StringUtils.join(referencingConfigs, ", ");
      throw new GrouperReferentialIntegrityException("Error: cannot delete this data provider because it is referenced by: " + referencingConfigsString);
    }

    super.deleteConfig(fromUi);

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataEngine.syncDataProviders(grouperConfig);
  }
}
