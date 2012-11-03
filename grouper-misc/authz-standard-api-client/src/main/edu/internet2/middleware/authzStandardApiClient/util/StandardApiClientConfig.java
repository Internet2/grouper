package edu.internet2.middleware.authzStandardApiClient.util;

import edu.internet2.middleware.authzStandardApiClient.config.AsacConfigPropertiesCascadeBase;


/**
 * hierarchical config class for grouper.client.properties
 * @author mchyzer
 *
 */
public class StandardApiClientConfig extends AsacConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private StandardApiClientConfig() {
    
  }
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static StandardApiClientConfig retrieveConfig() {
    return retrieveConfig(StandardApiClientConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see AsacConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "standardApiClient.config.hierarchy";
  }

  /**
   * @see AsacConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "standardApi.client.properties";
  }
  
  /**
   * @see AsacConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "standardApi.client.base.properties";
  }

  /**
   * @see AsacConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "standardApiClient.config.secondsBetweenUpdateChecks";
  }

  
  
}
