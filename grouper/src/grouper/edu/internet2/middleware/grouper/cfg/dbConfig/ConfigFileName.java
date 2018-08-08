/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public enum ConfigFileName {

  /**
   * grouper.properties
   */
  GROUPER_PROPERTIES("grouper.properties"), 
  
  /**
   * grouper.hibernate.properties
   */
  GROUPER_HIBERNATE_PROPERTIES("grouper.hibernate.properties"), 
  
  /**
   * grouper-loader.properties
   */
  GROUPER_LOADER_PROPERTIES("grouper-loader.properties"), 
  
  /**
   * grouper.cache.properties
   */
  GROUPER_CACHE_PROPERTIES("grouper.cache.properties"),
  
  /**
   * subject.properties
   */
  SUBJECT_PROPERTIES("subject.properties"),
  
  /**
   * grouper.client.properties
   */
  GROUPER_CLIENT_PROPERTIES("grouper.client.properties");
  
  /**
   * order the config gets loaded
   * the lower the number the first it is read
   */
  private String configFileName;
  
  /**
   * construct
   * @param theConfigFileName
   */
  private ConfigFileName(String theConfigFileName) {
    this.configFileName = theConfigFileName;
  }
  
  /**
   * order the config gets loaded
   * the lower the number the first it is read
   * @return the order
   */
  public String getConfigFileName() {
    return this.configFileName;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static ConfigFileName valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    
    // match the config file name
    for (ConfigFileName configFileName : ConfigFileName.values()) {
      if (StringUtils.equalsIgnoreCase(string, configFileName.getConfigFileName())) {
        return configFileName;
      }
    }
    
    return GrouperUtil.enumValueOfIgnoreCase(ConfigFileName.class, 
        string, exceptionOnNull);

  }

}
