package edu.internet2.middleware.grouper.attr.resolver;

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


public class GlobalAttributeResolverConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "entityAttributeResolver." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(entityAttributeResolver)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "entityAttributeResolver";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "entityAttributeResolverId";
  }
  
  /**
   * list of configured ws trusted jwt configs
   * @return
   */
  public static List<GlobalAttributeResolverConfiguration> retrieveAllGlobalAttributeResolverConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GlobalAttributeResolverConfiguration.class.getName());
   return (List<GlobalAttributeResolverConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
}
