package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

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

}
