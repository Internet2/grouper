package edu.internet2.middleware.grouper.grouperUi.beans.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase.ConfigFile;
import net.redhogs.cronparser.CronExpressionDescriptor;

public class GuiConfigProperty {

  public static final String ESCAPED_PASSWORD = "*******";

  
  /**
   * if this is a scriptlet, get the scriptlet
   * @return the scriptlet
   */
  public String getScriptletForUi() {
    
    String key = this.configItemMetadata.getKeyOrSampleKey();
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = this.getGuiConfigSection().getGuiConfigFile().getConfigPropertiesCascadeBase();
    
    List<ConfigFile> configFiles = new ArrayList<ConfigFile>(configPropertiesCascadeBase.internalRetrieveConfigFiles());
    Collections.reverse(configFiles);
     
    String elKey = key + ".elConfig";
    
    for (ConfigFile configFile : configFiles) {
      Properties properties = configFile.getProperties();

      if (properties.containsKey(elKey)) {
        return properties.getProperty(elKey);
      }
      
    }
    
    return null;
  }
  
  /**
   * see if scriptlet
   * @return if scriptlet
   */
  public boolean isScriptlet() {
    
    String key = this.configItemMetadata.getKeyOrSampleKey();
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = this.getGuiConfigSection().getGuiConfigFile().getConfigPropertiesCascadeBase();
    
    List<ConfigFile> configFiles = new ArrayList<ConfigFile>(configPropertiesCascadeBase.internalRetrieveConfigFiles());
    Collections.reverse(configFiles);
    String elKey = key + ".elConfig";
    
    for (ConfigFile configFile : configFiles) {
      Properties properties = configFile.getProperties();

      if (properties.containsKey(elKey)) {
        return true;
      }
      
      if (properties.containsKey(key)) {
        return false;
      }
    }
    
    return false;
  }
  
  /**
   * 
   */
  public GuiConfigProperty() {
    // TODO Auto-generated constructor stub
  }

  /**
   * see if key is a password
   * @param value
   * @return if key is a password
   */
  public String escapePassword(String value) {
    
    if (ConfigUtils.isPassword(this.getGuiConfigSection().getGuiConfigFile().getConfigFileName(), 
        this.configItemMetadata, this.configItemMetadata.getKeyOrSampleKey(), value, true, this.isEncryptedInDatabase())) {
      if (!StringUtils.isBlank(value)) {
        File theFile = new File(value);
        if (theFile.exists() && theFile.isFile()) {
          return theFile.getAbsolutePath();
        }
      }
      return ESCAPED_PASSWORD;
    }
    return value;
  }
  
  /**
   * get the property value of a string
   * @return the string property value
   */
  public String getPropertyValue() {
    
    String key = this.configItemMetadata.getKeyOrSampleKey();

    ConfigPropertiesCascadeBase configPropertiesCascadeBase = this.getGuiConfigSection().getGuiConfigFile().getConfigPropertiesCascadeBase();
    if (configPropertiesCascadeBase.containsKey(key)) {
      return escapePassword(configPropertiesCascadeBase.propertyValueString(key));
    }
    return TextContainer.retrieveFromRequest().getText().get("configurationColumnValueNotSet");
  }
  

  /**
   * config section
   */
  private GuiConfigSection guiConfigSection;
  
  /**
   * config section
   * @return the section
   */
  public GuiConfigSection getGuiConfigSection() {
    return this.guiConfigSection;
  }

  /**
   * config section
   * @param guiConfigSection1
   */
  public void setGuiConfigSection(GuiConfigSection guiConfigSection1) {
    this.guiConfigSection = guiConfigSection1;
  }

  /**
   * 
   * @return if has type
   */
  public boolean isHasType() {
    return this.getConfigItemMetadata().getValueType() != null;
    
  }
  
  /**
   * config type, text, group, etc.  ConfigItemMetadataType
   * @return if has type
   */
  public String getType() {
    return this.getConfigItemMetadata().getValueType() == null ? null : this.getConfigItemMetadata().getValueType().getStringForUi();
  }
  
  /**
   * see where a key was set
   * @param key
   * @return where it was set (property file name)
   */
  public String getValueFromWhere() {
    
    String key = this.getConfigItemMetadata().getKeyOrSampleKey();
    String elKey = key + ".elConfig";

    ConfigPropertiesCascadeBase configPropertiesCascadeBase = this.getGuiConfigSection().getGuiConfigFile().getConfigPropertiesCascadeBase();
    
    List<ConfigFile> configFiles = new ArrayList<ConfigFile>(configPropertiesCascadeBase.internalRetrieveConfigFiles());
    Collections.reverse(configFiles);
     
    String fromWhere = null;
    
    for (ConfigFile configFile : configFiles) {
      Properties properties = configFile.getProperties();
      if (properties.containsKey(elKey) || properties.containsKey(key)) {
        fromWhere = configFile.getOriginalConfig();
        break;
      }
    }
    
    fromWhere = fromWhere == null ? null : fromWhere.replace("classpath:", "");
    fromWhere = fromWhere == null ? null : fromWhere.replace("database:grouper", "database");
    return fromWhere;
  }
  
  /**
   * 
   * @return cron description
   */
  public String getCronDescription() {
    
    String key = this.getConfigItemMetadata().getKeyOrSampleKey();

    if (!key.toLowerCase().contains("cron")) {
      return null;
    }
    
    String propertyValue = this.getPropertyValue();
    
    if (StringUtils.isBlank(propertyValue) || StringUtils.equals(propertyValue, TextContainer.retrieveFromRequest().getText().get("configurationColumnValueNotSet"))) {
      return null;
    }
    
    try {
      return CronExpressionDescriptor.getDescription(propertyValue);
    } catch (Exception e) {
      
      LOG.error("Cant parse cron string:" + propertyValue, e);
      
      return TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlCronDescriptionError");
    }

  }
    
  /**
   * if different
   * @return the unprocessed value
   */
  public String getUnprocessedValueIfDifferent() {
    String key = this.getConfigItemMetadata().getKeyOrSampleKey();
    String elKey = key + ".elConfig";
    String propertyValue = this.getPropertyValue();
    
    if (StringUtils.equals(propertyValue, ESCAPED_PASSWORD)) {
      return null;
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = this.getGuiConfigSection().getGuiConfigFile().getConfigPropertiesCascadeBase();
    
    List<ConfigFile> configFiles = new ArrayList<ConfigFile>(configPropertiesCascadeBase.internalRetrieveConfigFiles());
    Collections.reverse(configFiles);
     
    String unprocessedValue = null;
    
    for (ConfigFile configFile : configFiles) {
      Properties properties = configFile.getProperties();
      if (properties.containsKey(elKey)) {
        return null;
      } else if (properties.containsKey(key)) {
        unprocessedValue = properties.getProperty(key);
        break;
      }
    }

    if (unprocessedValue != null) {
      unprocessedValue = unprocessedValue.trim();
    }
    
    if (!StringUtils.isBlank(unprocessedValue) && !StringUtils.equals(propertyValue, unprocessedValue)) {
      return unprocessedValue;
    }
    
    return null;
  }
  
  /**
   * get the base value if different, or give the default value
   * @param key
   * @return where it was set (property file name)
   */
  public String getBaseValueIfDifferent() {
    
    String key = this.getConfigItemMetadata().getKeyOrSampleKey();
    String elKey = key + ".elConfig";

    String propertyValue = this.getPropertyValue();
    
    if (StringUtils.equals(propertyValue, ESCAPED_PASSWORD)) {
      return null;
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = this.getGuiConfigSection().getGuiConfigFile().getConfigPropertiesCascadeBase();
    
    List<ConfigFile> configFiles = new ArrayList<ConfigFile>(configPropertiesCascadeBase.internalRetrieveConfigFiles());
     
    ConfigFile baseConfigFile = configFiles.get(0);
    
    String fromWhere = this.getValueFromWhere();
    
    // set in base
    if (StringUtils.equals("classpath:" + fromWhere, baseConfigFile.getOriginalConfig())) {
      return null;
    }
    
    Properties properties = baseConfigFile.getProperties();
    String baseValue = null;
    if (properties.containsKey(elKey)) {
      baseValue = properties.getProperty(elKey);
    } else if (properties.containsKey(key)) {
      baseValue = properties.getProperty(key);
    }

    if (!StringUtils.isBlank(baseValue)) {
      return baseValue;
    }
    return this.configItemMetadata.getDefaultValue();
  }
  
  /**
   * 
   */
  private ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GuiConfigProperty.class);

  /**
   * config item metadata
   * @return the metadata
   */
  public ConfigItemMetadata getConfigItemMetadata() {
    return this.configItemMetadata;
  }

  /**
   * config item metadata
   * @param configItemMetadata1
   */
  public void setConfigItemMetadata(ConfigItemMetadata configItemMetadata1) {
    this.configItemMetadata = configItemMetadata1;
  }

  /**
   * if encrypted in database dont show it
   */
  private boolean encryptedInDatabase = false;

  /**
   * if encrypted in database dont show it
   * @return the encryptedInDatabase
   */
  public boolean isEncryptedInDatabase() {
    return this.encryptedInDatabase;
  }

  /**
   * if this is in the database
   * @return true if from database
   */
  public boolean isFromDatabase() {
    return "database".equals(this.getValueFromWhere());
  }
  
  /**
   * if encrypted in database dont show it
   * @param encryptedInDatabase1 the encryptedInDatabase to set
   */
  public void setEncryptedInDatabase(boolean encryptedInDatabase1) {
    this.encryptedInDatabase = encryptedInDatabase1;
  }

  
  
}
