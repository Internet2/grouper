package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDataProviderChangeLogQueryConfiguration extends GrouperConfigurationModuleBase {

  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperDataProviderChangeLogQuery." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperDataProviderChangeLogQuery)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperDataProviderChangeLogQuery";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "dataProviderChangeLogQueryConfigId";
  }
  
  /**
   * list of configured data provider change log query configs
   * @return
   */
  public static List<GrouperDataProviderChangeLogQueryConfiguration> retrieveAllDataProviderChangeLogQueryConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GrouperDataProviderChangeLogQueryConfiguration.class.getName());
   return (List<GrouperDataProviderChangeLogQueryConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
}
