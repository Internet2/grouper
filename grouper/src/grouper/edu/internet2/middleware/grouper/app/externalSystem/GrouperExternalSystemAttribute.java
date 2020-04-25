package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.util.GrouperUtilElSafe;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperExternalSystemAttribute {
  
  public boolean isHasValue() {
    return (!this.isExpressionLanguage() && !StringUtils.isBlank(this.getValue()))
        || (this.isExpressionLanguage() && !StringUtils.isBlank(this.getExpressionLanguageScript()));
  }
  
  private  ConfigItemMetadataType type;
  
  private boolean required;
  
  private String value;
  
  private boolean expressionLanguage;
  
  private boolean password;
  
  private String configSuffix;
  
  private String defaultValue;
  
  private ConfigItemFormElement formElement;
  
  private String fullPropertyName;
  
  private GrouperExternalSystem grouperExternalSystem;
  
  public Object getObjectValueAllowInvalid() {
    
    ConfigItemMetadataType valueType =  GrouperUtil.defaultIfNull(this.getConfigItemMetadata().getValueType(), ConfigItemMetadataType.STRING);
    
    return valueType.convertValue(this.getValueOrExpressionEvaluation(), false);
    
  }
  
  /**
   * get the value or the expression language evaluation
   * @return the value
   */
  public String getValueOrExpressionEvaluation() {
    String value = null;
    
    if (this.isExpressionLanguage()) {
      value = this.getExpressionLanguageScript() != null? this.getExpressionLanguageScript(): null;
    } else if (this.getValue() != null) {
      value = this.getValue();
    } 
    
//    if (value == null && this.getDefaultValue() != null) {
//      value = this.getDefaultValue();
//    }
    return value;
  }
  
  public String getHtmlForElementIdHandle() {
    return "#config_" + this.getConfigSuffix() + "_id";
  }
  
  
  public static void main(String[] args) {
    Pattern pattern = Pattern.compile("^\\$\\{java\\.lang\\.System\\.getenv\\(\\)\\.get\\('([\\w]+)'\\)\\}");
    boolean evaluate = false;
    if (pattern.matcher("${java.lang.System.getenv().get('JAVA_HOME')}").matches()) {
      evaluate = true;
    }
    System.out.println(evaluate);
  }
  
  /**
   * 
   * @return
   */
  public String getEvaluatedValueForValidation() throws UnsupportedOperationException {
    
    if (!this.expressionLanguage) {
      return this.value;
    }
    
    // if script and certain type (e.g. env var), then validate
    //${java.lang.System.getenv().get('JAVA_HOME')}
    Pattern pattern = Pattern.compile("^\\$\\{java\\.lang\\.System\\.getenv\\(\\)\\.get\\('([\\w]+)'\\)\\}");
    boolean evaluate = false;
    if (pattern.matcher(this.expressionLanguageScript).matches()) {
      evaluate = true;
    }
    
    if (evaluate) {
      Map<String, Object> variableMap = new HashMap<String, Object>();

      variableMap.put("grouperUtil", new GrouperUtilElSafe());
      
      String value = GrouperUtil.substituteExpressionLanguage(this.expressionLanguageScript, variableMap, true, true, true);

      return value;
    }
    
    throw new UnsupportedOperationException();
  }
  
  /**
   * 
   * @return if show
   */
  public boolean isShow() {
    
    {
      Boolean showOverride = this.grouperExternalSystem.showAttributeOverride(this.configSuffix);
      if (showOverride != null) {
        return showOverride;
      }
    }
    
    String showEl = this.getConfigItemMetadata().getShowEl();
    if (StringUtils.isBlank(showEl)) {
      return true;
    }
    
    Map<String, Object> variableMap = new HashMap<String, Object>();

    variableMap.put("grouperUtil", new GrouperUtilElSafe());
    
    for (GrouperExternalSystemAttribute grouperExternalSystemAttribute : this.grouperExternalSystem.retrieveAttributes().values()) {
      
      variableMap.put(grouperExternalSystemAttribute.getConfigSuffix(), grouperExternalSystemAttribute.getObjectValueAllowInvalid());
      
    }

    String showString = GrouperUtil.substituteExpressionLanguage(showEl, variableMap, true, true, true);
    
    return GrouperUtil.booleanValue(showString, true);
  }
  
  
  public GrouperExternalSystem getGrouperExternalSystem() {
    return grouperExternalSystem;
  }



  
  public void setGrouperExternalSystem(GrouperExternalSystem grouperExternalSystem) {
    this.grouperExternalSystem = grouperExternalSystem;
  }



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
    
    String label = GrouperTextContainer.textOrNull("externalSystem." + this.getGrouperExternalSystem().getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".label");
    if (StringUtils.isBlank(label)) {
      return this.getConfigSuffix();
    }
    return label;
  }

  public String getDescription() {
    String description = GrouperTextContainer.textOrNull("externalSystem." + this.getGrouperExternalSystem().getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".description");
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
