package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperExternalSystemAttribute {
  
  private  GrouperExternalSystemAttributeType type;
  
  private boolean required;
  
  private String value;
  
  private boolean expressionLanguage;
  
  private boolean password;
  
  private String configSuffix;
  
  private String defaultValue;
  
  private GrouperExternalSystemAttributeFormElement formElement;
  
  // first one is the value and the second one is the label  
  private List<MultiKey> dropdownMultikeys;
  
  
  public GrouperExternalSystemAttributeType getType() {
    return type;
  }

  
  public void setType(GrouperExternalSystemAttributeType type) {
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

  
  public GrouperExternalSystemAttributeFormElement getFormElement() {
    return formElement;
  }

  
  public void setFormElement(GrouperExternalSystemAttributeFormElement formElement) {
    this.formElement = formElement;
  }

  
  public List<MultiKey> getDropdownMultikeys() {
    return dropdownMultikeys;
  }

  
  public void setDropdownMultikeys(List<MultiKey> dropdownMultikeys) {
    this.dropdownMultikeys = dropdownMultikeys;
  }

  enum GrouperExternalSystemAttributeType {
    
    INTEGER, BOOLEAN, STRING;
    
  }
  
  enum GrouperExternalSystemAttributeFormElement {
    
    TEXT, TEXTAREA, PASSWORD, DROPDOWN;
    
  }
  
  

}
