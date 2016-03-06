package edu.internet2.middleware.tierApiAuthzClient.util;

import edu.internet2.middleware.tierApiAuthzClient.config.AsacConfigPropertiesCascadeBase;


/**
 * hierarchical config class for authzStandardApi.client.properties
 * @author mchyzer
 *
 */
public class StandardApiClientConfig extends AsacConfigPropertiesCascadeBase {

  /**
   * root folder for testing
   * @return the root folder
   */
  public String unitTestRootFolder() {
    String rootFolder = StandardApiClientConfig.retrieveConfig().propertyValueStringRequired("authzStandardApiClient.unitTest.rootFolder");
    if (StandardApiClientUtils.isBlank(rootFolder)) {
      throw new RuntimeException("Need to set a config property: authzStandardApiClient.unitTest.rootFolder");
    }
    return rootFolder;
  }
  
  /**
   * use the factory
   */
  private StandardApiClientConfig() {
    
  }
  
  /**
   * @see edu.internet2.middleware.tierApiAuthzClient.config.AsacConfigPropertiesCascadeBase#getClassInSiblingJar()
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
