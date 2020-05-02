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

/**
 * grouper external system attribute. one grouper system can have many attributes 
 */
public class GrouperExternalSystemAttribute {
  
  public boolean isHasValue() {
    return (!this.isExpressionLanguage() && !StringUtils.isBlank(this.getValue()))
        || (this.isExpressionLanguage() && !StringUtils.isBlank(this.getExpressionLanguageScript()));
  }
  
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
   * does this attribute store expression language 
   */
  private boolean expressionLanguage;
  
  /**
   * does this attribute store password
   */
  private boolean password;
  
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
  
  /**
   * grouper external system to which this instance of attribute belongs
   */
  private GrouperExternalSystem grouperExternalSystem;
  
  /**
   * @return converted value to correct configItemMetadataType from string
   */
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
  
  /**
   * get the html id for the field 
   * @return
   */
  public String getHtmlForElementIdHandle() {
    return "#config_" + this.getConfigSuffix() + "_id";
  }
  
  /**
   * @return get evaluated value for validation
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
  
  /**
   * grouper external system to which this instance of attribute belongs
   * @return {@link GrouperExternalSystem}
   */
  public GrouperExternalSystem getGrouperExternalSystem() {
    return grouperExternalSystem;
  }

  /**
   * grouper external system to which this instance of attribute belongs
   * @param grouperExternalSystem
   */
  public void setGrouperExternalSystem(GrouperExternalSystem grouperExternalSystem) {
    this.grouperExternalSystem = grouperExternalSystem;
  }

  /**
   * full property name
   * @return full property name
   */
  public String getFullPropertyName() {
    return fullPropertyName;
  }

  /**
   * full property name
   * @param fullPropertyName
   */
  public void setFullPropertyName(String fullPropertyName) {
    this.fullPropertyName = fullPropertyName;
  }

  /**
   * first one is the value and the second one is the label  
   */
  private List<MultiKey> dropdownValuesAndLabels;
  
  /**
   * config item metadata type (eg: Boolean, String, etc)
   * @return config item metadata type (eg: Boolean, String, etc)
   */
  public ConfigItemMetadataType getType() {
    return type;
  }

  /**
   * config item metadata type (eg: Boolean, String, etc)
   * @param type
   */
  public void setType(ConfigItemMetadataType type) {
    this.type = type;
  }

  /**
   * is this attribute required or not
   * @return
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * is this attribute required or not
   * @param required
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * value for the attribute
   * @return
   */
  public String getValue() {
    return value;
  }

  /**
   * value for the attribute
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * does this attribute store expression language
   * @return does this attribute store expression language
   */
  public boolean isExpressionLanguage() {
    return expressionLanguage;
  }

  /**
   * does this attribute store expression language
   * @param expressionLanguage
   */
  public void setExpressionLanguage(boolean expressionLanguage) {
    this.expressionLanguage = expressionLanguage;
  }

  /**
   * value when this attribute stores expression language
   */
  private String expressionLanguageScript;
  
  /**
   * value when this attribute stores expression language
   * @return
   */
  public String getExpressionLanguageScript() {
    return expressionLanguageScript;
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
    
    String label = GrouperTextContainer.textOrNull("externalSystem." + this.getGrouperExternalSystem().getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".label");
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
    String description = GrouperTextContainer.textOrNull("externalSystem." + this.getGrouperExternalSystem().getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".description");
    if (StringUtils.isBlank(description)) {
      return this.getConfigItemMetadata().getComment();
    }
    return description;
  }

  /**
   * value when this attribute stores expression language
   * @param expressionLanguageScript
   */
  public void setExpressionLanguageScript(String expressionLanguageScript) {
    this.expressionLanguageScript = expressionLanguageScript;
  }

  /**
   * does this attribute store password
   * @return
   */
  public boolean isPassword() {
    return password;
  }

  /**
   * does this attribute store password
   * @param password
   */
  public void setPassword(boolean password) {
    this.password = password;
  }

  /**
   * config suffix
   * @return
   */
  public String getConfigSuffix() {
    return configSuffix;
  }

  /**
   * config suffix
   * @param configSuffix
   */
  public void setConfigSuffix(String configSuffix) {
    this.configSuffix = configSuffix;
  }

  /**
   * default value for the attribute
   * @return
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * default value for the attribute
   * @param defaultValue
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * type of html element to render (Textfield, Textarea, etc)
   * @return
   */
  public ConfigItemFormElement getFormElement() {
    return formElement;
  }

  /**
   * type of html element to render (Textfield, Textarea, etc)
   * @param formElement
   */
  public void setFormElement(ConfigItemFormElement formElement) {
    this.formElement = formElement;
  }

  /**
   * first one is the value and the second one is the label
   * @return
   */
  public List<MultiKey> getDropdownValuesAndLabels() {
    return dropdownValuesAndLabels;
  }

  /**
   * first one is the value and the second one is the label
   * @param dropdownValuesAndLabels
   */
  public void setDropdownValuesAndLabels(List<MultiKey> dropdownValuesAndLabels) {
    this.dropdownValuesAndLabels = dropdownValuesAndLabels;
  }

}
