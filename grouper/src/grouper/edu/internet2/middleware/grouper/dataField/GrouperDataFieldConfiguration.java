package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDataFieldConfiguration extends GrouperConfigurationModuleBase {

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

}
