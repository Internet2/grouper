package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * 
 * @author mchyzer
 *
 */
public class ConfigurationContainer {

  /**
   * config file name selected if any
   */
  private ConfigFileName configFileName;

  /**
   * config file name selected if any
   * @return config file name
   */
  public ConfigFileName getConfigFileName() {
    return this.configFileName;
  }

  /**
   * config file name selected if any
   * @param configFileName1
   */
  public void setConfigFileName(ConfigFileName configFileName1) {
    this.configFileName = configFileName1;
  }
  
  /**
   * config file name selected if any
   * @return config file name array
   */
  public ConfigFileName[] getAllConfigFileNames() {
    return ConfigFileName.values();
  }
  
}
