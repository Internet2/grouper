package edu.internet2.middleware.grouper.authentication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public class WsTrustedJwtConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.jwt.trusted." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.jwt\\.trusted)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouper.jwt.trusted";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "testConfigId";
  }
  
  /**
   * list of configured ws trusted jwt configs
   * @return
   */
  public static List<WsTrustedJwtConfiguration> retrieveAllWsTrustedJwtConfigs() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(WsTrustedJwtConfiguration.class.getName());
   return (List<WsTrustedJwtConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }

}
