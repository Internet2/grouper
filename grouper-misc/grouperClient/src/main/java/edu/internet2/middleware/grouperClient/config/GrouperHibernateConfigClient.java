package edu.internet2.middleware.grouperClient.config;

public class GrouperHibernateConfigClient extends ConfigPropertiesCascadeBase {

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperHibernateConfigClient retrieveConfig() {
    return retrieveConfig(GrouperHibernateConfigClient.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues
   */
  @Override
  public void clearCachedCalculatedValues() {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouper.hibernate.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper.hibernate.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper.hibernate.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouper.hibernate.config.secondsBetweenUpdateChecks";
  }
  
} 