package edu.internet2.middleware.grouperClient.util;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * hierarchical config class for grouper.client.properties
 * @author mchyzer
 *
 */
public class GrouperClientConfig extends ConfigPropertiesCascadeBase {

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperClientConfig retrieveConfig() {
    return retrieveConfig(GrouperClientConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouperClient.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper.client.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper.client.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouperClient.config.secondsBetweenUpdateChecks";
  }

  
  
}
