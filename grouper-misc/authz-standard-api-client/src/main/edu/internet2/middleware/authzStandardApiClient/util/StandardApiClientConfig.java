package edu.internet2.middleware.authzStandardApiClient.util;

import edu.internet2.middleware.authzStandardApiClient.config.AsacConfigPropertiesCascadeBase;


/**
 * hierarchical config class for authzStandardApi.client.properties
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
   * @see edu.internet2.middleware.authzStandardApiClient.config.AsacConfigPropertiesCascadeBase#getClassInSiblingJar()
   */
  @Override
  protected Class<?> getClassInSiblingJar() {
    return StandardApiClientConfig.class;
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
    return "authzStandardApiClient.config.hierarchy";
  }

  /**
   * @see AsacConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "authzStandardApi.client.properties";
  }
  
  /**
   * @see AsacConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "authzStandardApi.client.base.properties";
  }

  /**
   * @see AsacConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "authzStandardApiClient.config.secondsBetweenUpdateChecks";
  }

  
  
}
