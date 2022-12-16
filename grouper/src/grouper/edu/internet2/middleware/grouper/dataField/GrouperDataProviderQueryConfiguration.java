package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDataProviderQueryConfiguration extends GrouperConfigurationModuleBase {

  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperDataProviderQuery." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperDataProviderQuery)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperDataProviderQuery";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "dataProviderQueryConfigId";
  }
  
  /**
   * list of configured data provider query configs
   * @return
   */
  public static List<GrouperDataProviderQueryConfiguration> retrieveAllDataProviderQueryConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GrouperDataProviderQueryConfiguration.class.getName());
   return (List<GrouperDataProviderQueryConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
}
