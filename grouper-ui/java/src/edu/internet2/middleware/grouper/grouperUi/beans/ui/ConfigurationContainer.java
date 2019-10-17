package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigFile;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigProperty;
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
   * import count added properties
   */
  private int countAdded = 0;
  
  
  
  
  /**
   * import count added properties
   * @return count
   */
  public int getCountAdded() {
    return this.countAdded;
  }

  /**
   * import count added properties
   * @param countAdded1
   */
  public void setCountAdded(int countAdded1) {
    this.countAdded = countAdded1;
  }

  /**
   * import count updated
   */
  private int countUpdated = 0;
  
  
  
  /**
   * import count updated
   * @return import count updated
   */
  public int getCountUpdated() {
    return this.countUpdated;
  }

  /**
   * import count updated
   * @param countUpdated1
   */
  public void setCountUpdated(int countUpdated1) {
    this.countUpdated = countUpdated1;
  }

  /**
   * import count properties
   */
  private int countProperties = 0;

  /**
   * import count properties
   * @return count
   */
  public int getCountProperties() {
    return  this.countProperties;
  }

  /**
   * import count properties
   * @param countProperties1
   */
  public void setCountProperties(int countProperties1) {
    this.countProperties = countProperties1;
  }

  /**
   * import count successful inserts/updates
   */
  private int countSuccess = 0;

  /**
   * import count successful inserts/updates
   * @return import count successful inserts/updates
   */
  public int getCountSuccess() {
    return this.countSuccess;
  }

  /**
   * import count successful inserts/updates
   * @param countSuccess1
   */
  public void setCountSuccess(int countSuccess1) {
    this.countSuccess = countSuccess1;
  }

  /**
   * import count unchanged
   */
  private int countUnchanged = 0;

  /**
   * import count unchanged
   * @return count unchanged
   */
  public int getCountUnchanged() {
    return  this.countUnchanged;
  }

  /**
   * import count unchanged
   * @param countUnchanged1
   */
  public void setCountUnchanged(int countUnchanged1) {
    this.countUnchanged = countUnchanged1;
  }

  /**
   * import count fatal error
   */
  private int countError = 0;

  /**
   * import count fatal error
   * @return count fatal
   */
  public int getCountError() {
    return  this.countError;
  }

  /**
   * import count fatal error
   * @param countFatalError1
   */
  public void setCountError(int countFatalError1) {
    this.countError = countFatalError1;
  }

  /**
   * import count warnings
   */
  private int countWarning = 0;

  /**
   * import count properties
   * @return
   */
  public int getCountWarning() {
    return  this.countWarning;
  }

  /**
   * import count warnings
   * @param countWarning1
   */
  public void setCountWarning(int countWarning1) {
    this.countWarning = countWarning1;
  }

  /**
   * 
   * @return config file names for import
   */
  public String getConfigFileNamesForImport() {
    StringBuilder results = new StringBuilder();
    
    for (ConfigFileName configFileName : ConfigFileName.values()) {
      if (results.length() != 0) {
        results.append(", ");
      }
      results.append(configFileName.getConfigFileName());
    }
    
    return results.toString();
  }
  
  /**
   * current config property we are editing
   */
  private GuiConfigProperty currentGuiConfigProperty;
  
  /**
   * current config property we are editing
   */
  public GuiConfigProperty getCurrentGuiConfigProperty() {
    return currentGuiConfigProperty;
  }
  
  /**
   * current config property we are editing
   */
  public void setCurrentGuiConfigProperty(GuiConfigProperty currentConfigProperty1) {
    this.currentGuiConfigProperty = currentConfigProperty1;
  }

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
