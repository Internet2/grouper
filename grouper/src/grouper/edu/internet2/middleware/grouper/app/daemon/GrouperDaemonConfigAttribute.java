package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;

public class GrouperDaemonConfigAttribute {
  
  private GrouperDaemonConfiguration grouperDaemonConfiguration;
  
  /**
   * config item metadata type (eg: Boolean, String, etc)
   */
  private  ConfigItemMetadataType type;
  
  /**
   * is this attribute required or not
   */
  private boolean required;
  
  /**
   * value for the attribute
   */
  private String value;
  
  /**
   * config suffix 
   */
  private String configSuffix;
  
  /**
   * default value for the attribute
   */
  private String defaultValue;
  
  /**
   * type of html element to render (Textfield, Textarea, etc)
   */
  private ConfigItemFormElement formElement;
  
  /**
   * full property name
   */
  private String fullPropertyName;

  
  public GrouperDaemonConfiguration getGrouperDaemonConfiguration() {
    return grouperDaemonConfiguration;
  }

  
  public void setGrouperDaemonConfiguration(
      GrouperDaemonConfiguration grouperDaemonConfiguration) {
    this.grouperDaemonConfiguration = grouperDaemonConfiguration;
  }

  
  public ConfigItemMetadataType getType() {
    return type;
  }

  
  public void setType(ConfigItemMetadataType type) {
    this.type = type;
  }

  
  public boolean isRequired() {
    return required;
  }

  
  public void setRequired(boolean required) {
    this.required = required;
  }

  
  public String getValue() {
    return value;
  }

  
  public void setValue(String value) {
    this.value = value;
  }

  
  public String getConfigSuffix() {
    return configSuffix;
  }

  
  public void setConfigSuffix(String configSuffix) {
    this.configSuffix = configSuffix;
  }

  
  public String getDefaultValue() {
    return defaultValue;
  }

  
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  
  public ConfigItemFormElement getFormElement() {
    return formElement;
  }

  
  public void setFormElement(ConfigItemFormElement formElement) {
    this.formElement = formElement;
  }

  
  public String getFullPropertyName() {
    return fullPropertyName;
  }

  
  public void setFullPropertyName(String fullPropertyName) {
    this.fullPropertyName = fullPropertyName;
  }
  
  
  /**
   * config item metadata this attribute is backed with
   */
  private ConfigItemMetadata configItemMetadata;
  
  /**
   * config item metadata this attribute is backed with
   * @return
   */
  public ConfigItemMetadata getConfigItemMetadata() {
    return configItemMetadata;
  }
  
  /**
   * config item metadata this attribute is backed with
   * @param configItemMetadata
   */
  public void setConfigItemMetadata(ConfigItemMetadata configItemMetadata) {
    this.configItemMetadata = configItemMetadata;
  }
  
  /**
   * get label to display for this attribute
   * @return
   */
  public String getLabel() {
    
    String label = GrouperTextContainer.textOrNull("daemonConfig." + this.getGrouperDaemonConfiguration().getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".label");
    if (StringUtils.isBlank(label)) {
      return this.getConfigSuffix();
    }
    return label;
  }

  /**
   * get description to display for this attribute
   * @return
   */
  public String getDescription() {
    String description = GrouperTextContainer.textOrNull("daemonConfig." + this.getGrouperDaemonConfiguration().getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".description");
    if (StringUtils.isBlank(description)) {
      return this.getConfigItemMetadata().getComment();
    }
    return description;
  }
  

}
