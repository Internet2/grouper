package edu.internet2.middleware.grouperClient.config;


/**
 * 
 * @author mchyzer
 *
 */
public class ConfigPropertiesOverrideHasHierarchy extends ConfigPropertiesCascadeBase {

  /**
   * this is used to tell engine where the default and example config is...
   */
  private static ConfigPropertiesOverrideHasHierarchy configSingleton = new ConfigPropertiesOverrideHasHierarchy();
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static ConfigPropertiesOverrideHasHierarchy retrieveConfig() {
    return (ConfigPropertiesOverrideHasHierarchy)configSingleton.retrieveFromConfigFileOrCache();
  }

  @Override
  public String getMainConfigClasspath() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getMainExampleConfigClasspath() {
    // TODO Auto-generated method stub
    return null;
  }
  
  
}
