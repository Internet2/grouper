package edu.internet2.middleware.grouperTierApiAuth.config;

import edu.internet2.middleware.tierApiAuthzServer.config.AsasConfigPropertiesCascadeBase;


/**
 * hierarchical config class for grouperAuthzApi.server.properties
 * @author mchyzer
 *
 */
public class GrouperAuthzApiServerConfig extends AsasConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private GrouperAuthzApiServerConfig() {
    
  }
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperAuthzApiServerConfig retrieveConfig() {
    return retrieveConfig(GrouperAuthzApiServerConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see AsasConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouperAuthzApiServer.config.hierarchy";
  }

  /**
   * @see AsasConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouperTierApiAuthz.server.properties";
  }
  
  /**
   * @see AsasConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouperTierApiAuthz.server.base.properties";
  }

  /**
   * @see AsasConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouperAuthzApiServer.config.secondsBetweenUpdateChecks";
  }

  
  
}
