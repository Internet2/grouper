package edu.internet2.middleware.grouperClient.config;


/**
 * 
 * @author mchyzer
 *
 */
public class ConfigPropertiesOriginalHasHierarchy extends ConfigPropertiesCascadeBase {

  /**
   * this is used to tell engine where the default and example config is...
   */
  private static ConfigPropertiesOriginalHasHierarchy configSingleton = new ConfigPropertiesOriginalHasHierarchy();
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static ConfigPropertiesOriginalHasHierarchy retrieveConfig() {
    return (ConfigPropertiesOriginalHasHierarchy)configSingleton.retrieveFromConfigFileOrCache();
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath()
   */
  @Override
  protected String getMainConfigClasspath() {
    return "testCascadeConfig2.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath()
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "testCascadeConfig-example2.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey()
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "config.hierarchy2";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "config.checkConfigEverySeconds2";
  }
  
  
}
