/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public enum ConfigFileHierarchy {

  /**
   * the base config that ships with grouper
   */
  BASE(100), 
  
  /**
   * the config at your institution that spans across all envs
   */
  INSTITUTION(1000), 
  
  /**
   * config for the environment at your institution, dev, test, prod whatever
   */
  ENVIRONMENT(10000), 
  
  /**
   * if configs differ between UI, WS, etc put that here
   */
  GROUPER_ENGINE(100000);
  
  /**
   * order the config gets loaded
   * the lower the number the first it is read
   */
  private int order;
  
  /**
   * construct
   * @param theOrder
   */
  private ConfigFileHierarchy(int theOrder) {
    this.order = theOrder;
  }
  
  /**
   * order the config gets loaded
   * the lower the number the first it is read
   * @return the order
   */
  public int getOrder() {
    return this.order;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static ConfigFileHierarchy valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(ConfigFileHierarchy.class, 
        string, exceptionOnNull);

  }

}
