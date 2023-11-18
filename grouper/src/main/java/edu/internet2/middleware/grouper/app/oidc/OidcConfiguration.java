package edu.internet2.middleware.grouper.app.oidc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;


public class OidcConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.oidc." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.oidc)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouper.oidc";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "testConfigId";
  }
  
  /**
   * list of configured oidc configs
   * @return
   */
  public static List<OidcConfiguration> retrieveAllOidcConfigs() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(OidcConfiguration.class.getName());
   return (List<OidcConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }
  
}
