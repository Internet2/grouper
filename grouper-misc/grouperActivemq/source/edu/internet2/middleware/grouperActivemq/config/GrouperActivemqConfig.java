/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperActivemq.config;




/**
 * Grouper activemq config
 */
public class GrouperActivemqConfig extends ConfigPropertiesCascadeBase {

  /**
   * 
   */
  public GrouperActivemqConfig() {
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperActivemqConfig retrieveConfig() {
    return retrieveConfig(GrouperActivemqConfig.class);
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
    return "grouperActivemq.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper.activemq.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper.activemq.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouperActivemq.config.secondsBetweenUpdateChecks";
  }

}
