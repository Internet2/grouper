package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigFile;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 * @author mchyzer
 *
 */
public class ConfigurationContainer {
  
  /**
   * config file name
   */
  private String currentConfigFileName;

  /**
   * 
   * @return current config file name
   */
  public String getCurrentConfigFileName() {
    return this.currentConfigFileName;
  }

  /**
   * current config file name
   * @param currentConfigFileName1
   */
  public void setCurrentConfigFileName(String currentConfigFileName1) {
    this.currentConfigFileName = currentConfigFileName1;
  }

  /**
   * current config property name
   * @return 
   */
  public String getCurrentConfigPropertyName() {
    return this.currentConfigPropertyName;
  }

  /**
   * 
   * @param currentConfigPropertyName1
   */
  public void setCurrentConfigPropertyName(String currentConfigPropertyName1) {
    this.currentConfigPropertyName = currentConfigPropertyName1;
  }

  /**
   * config property name
   */
  private String currentConfigPropertyName;
  
  /**
   * if show configure link
   * @return the configure link
   */
  public boolean isConfigureShow() {

    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.configuration.enabled", true)) {
      return false;

    }

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  /**
   * cache the config so it is consistent as the page draws
   */
  private ConfigPropertiesCascadeBase config;

  /**
   * cache the config so it is consistent as the page draws
   * @return the config
   */
  public ConfigPropertiesCascadeBase getConfig() {
    if (this.config == null) {
      this.config = this.getConfigFileName().getConfig();
    }
    return this.config;
  }

  /**
   * return config names from the selected config
   * @return the config names
   */
  public Set<String> getPropertyNames() {
    return this.getConfig().propertyNames();
  }
  
  /**
   * gui config file
   */
  private GuiConfigFile guiConfigFile;

  /**
   * gui config file
   * @return config file
   */
  public GuiConfigFile getGuiConfigFile() {
    return this.guiConfigFile;
  }

  /**
   * gui config file
   * @param guiConfigFile1
   */
  public void setGuiConfigFile(GuiConfigFile guiConfigFile1) {
    this.guiConfigFile = guiConfigFile1;
  }

  /**
   * cache the config so it is consistent as the page draws
   * @param config1
   */
  public void setConfig(ConfigPropertiesCascadeBase config1) {
    this.config = config1;
  }

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
