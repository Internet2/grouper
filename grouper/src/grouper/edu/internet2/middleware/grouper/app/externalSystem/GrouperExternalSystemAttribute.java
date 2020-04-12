package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperExternalSystemAttribute {
  
  private  ConfigItemMetadataType type;
  
  private boolean required;
  
  private String value;
  
  private boolean expressionLanguage;
  
  private boolean password;
  
  private String configSuffix;
  
  private String defaultValue;
  
  private ConfigItemFormElement formElement;
  
  private String fullPropertyName;
  
  
  
  
  public String getFullPropertyName() {
    return fullPropertyName;
  }


  
  public void setFullPropertyName(String fullPropertyName) {
    this.fullPropertyName = fullPropertyName;
  }

  // first one is the value and the second one is the label  
  private List<MultiKey> dropdownValuesAndLabels;
  
  
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

  
  public boolean isExpressionLanguage() {
    return expressionLanguage;
  }

  
  public void setExpressionLanguage(boolean expressionLanguage) {
    this.expressionLanguage = expressionLanguage;
  }

  private String expressionLanguageScript;
  
  
  
  
  public String getExpressionLanguageScript() {
    return expressionLanguageScript;
  }

  private ConfigItemMetadata configItemMetadata;
  
  public ConfigItemMetadata getConfigItemMetadata() {
    return configItemMetadata;
  }
  
  public void setConfigItemMetadata(ConfigItemMetadata configItemMetadata) {
    this.configItemMetadata = configItemMetadata;
  }


  public String getLabel() {
    String label = GrouperTextContainer.textOrNull("externalSystem." + this.getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".label");
    if (StringUtils.isBlank(label)) {
      return this.getConfigSuffix();
    }
    return label;
  }

  public String getDescription() {
    String description = GrouperTextContainer.textOrNull("externalSystem." + this.getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".description");
    if (StringUtils.isBlank(description)) {
      return this.getConfigItemMetadata().getComment();
    }
    return description;
  }

  
  public void setExpressionLanguageScript(String expressionLanguageScript) {
    this.expressionLanguageScript = expressionLanguageScript;
  }


  public boolean isPassword() {
    return password;
  }

  
  public void setPassword(boolean password) {
    this.password = password;
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

  
  public List<MultiKey> getDropdownValuesAndLabels() {
    return dropdownValuesAndLabels;
  }

  
  public void setDropdownValuesAndLabels(List<MultiKey> dropdownValuesAndLabels) {
    this.dropdownValuesAndLabels = dropdownValuesAndLabels;
  }

}
