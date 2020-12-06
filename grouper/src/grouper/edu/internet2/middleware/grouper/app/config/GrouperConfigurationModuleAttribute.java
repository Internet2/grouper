package edu.internet2.middleware.grouper.app.config;

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

public class GrouperConfigurationModuleAttribute {
  
  private GrouperConfigurationModuleBase grouperConfigModule;
  
  /**
   * config item metadata type (eg: Boolean, String, etc)
   */
  private  ConfigItemMetadataType type;
  
  /**
   * is this attribute read only or not
   */
  private boolean readOnly;
  
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
  
  /**
   * does this attribute store expression language 
   */
  private boolean expressionLanguage;
  
  /**
   * does this attribute store password
   */
  private boolean password;
  
  /**
   * value when this attribute stores expression language
   */
  private String expressionLanguageScript;
  
  /**
   * first one is the value and the second one is the label  
   */
  private List<MultiKey> dropdownValuesAndLabels;
  
  /**
   * first one is value, second one label and third one is if the checkbox should be checked
   */
  private List<MultiKey> checkboxAttributes;

  /**
   * if this is a repeat group; this is a 0 based index.
   */
  private int repeatGroupIndex = -1;
  

  public GrouperConfigurationModuleBase getGrouperConfigModule() {
    return grouperConfigModule;
  }

  public void setGrouperConfigModule(GrouperConfigurationModuleBase grouperConfigModule) {
    this.grouperConfigModule = grouperConfigModule;
  }


  public ConfigItemMetadataType getType() {
    return type;
  }

  
  public void setType(ConfigItemMetadataType type) {
    this.type = type;
  }

  /**
   * is this attribute required
   * @return
   */
  public boolean isRequired() {
    
    if (configItemMetadata.isRequired()) {
      return true;
    }
    
    String requiredEl = configItemMetadata.getRequiredEl();
    
    if (StringUtils.isBlank(requiredEl)) {
      return false;
    }
    
    Map<String, Object> variableMap = new HashMap<String, Object>();

    variableMap.put("grouperUtil", new GrouperUtilElSafe());
    
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.grouperConfigModule.retrieveAttributes().values()) {
      
      variableMap.put(grouperConfigModuleAttribute.getConfigSuffix(), grouperConfigModuleAttribute.getObjectValueAllowInvalid());
      
    }

    String requiredString = GrouperUtil.substituteExpressionLanguage(requiredEl, variableMap, true, true, true);
    
    return GrouperUtil.booleanValue(requiredString, false);
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
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
    
    String key = "config." + this.getGrouperConfigModule().getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".label";
    
    String label = GrouperTextContainer.textOrNull(key);
    
    if (StringUtils.isBlank(label)) {   
      if (this.getConfigSuffix().matches(".*[0-9].*")) {
        key = "config." + this.getGrouperConfigModule().getClass().getSimpleName() + ".attribute." + (this.getConfigSuffix().replaceAll("[0-9]+", "i")) + ".label";
        label = GrouperTextContainer.textOrNull(key);
      } 
    }
    
    if (StringUtils.isBlank(label)) {
      key = "config.GenericConfiguration.attribute." + this.getConfigSuffix() + ".label";
      label = GrouperTextContainer.textOrNull(key);
    }
    
    if (StringUtils.isBlank(label)) {   
      if (this.getConfigSuffix().matches(".*[0-9].*")) {
        key = "config.GenericConfiguration.attribute." + (this.getConfigSuffix().replaceAll("[0-9]+", "i")) + ".label";
        label = GrouperTextContainer.textOrNull(key);
      } 
    }
    
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
    
    String key = "config." + this.getGrouperConfigModule().getClass().getSimpleName() + ".attribute." + this.getConfigSuffix() + ".description";
    
    String description = GrouperTextContainer.textOrNull(key);
    
    if (StringUtils.isBlank(description)) {      
      if (this.getConfigSuffix().matches(".*[0-9].*")) {
        key = "config." + this.getGrouperConfigModule().getClass().getSimpleName() + ".attribute." + (this.getConfigSuffix().replaceAll("[0-9]+", "i")) + ".description";
        description = GrouperTextContainer.textOrNull(key);
      } 
    }
    
    
    if (StringUtils.isBlank(description)) {
      key = "config.GenericConfiguration.attribute." + this.getConfigSuffix() + ".description";
      description = GrouperTextContainer.textOrNull(key);
    }
    
    if (StringUtils.isBlank(description)) {   
      if (this.getConfigSuffix().matches(".*[0-9].*")) {
        key = "config.GenericConfiguration.attribute." + (this.getConfigSuffix().replaceAll("[0-9]+", "i")) + ".description";
        description = GrouperTextContainer.textOrNull(key);
      } 
    }
    
    if (StringUtils.isBlank(description)) {
      return this.getConfigItemMetadata().getComment();
    }
    return description;
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


  
  public String getExpressionLanguageScript() {
    return expressionLanguageScript;
  }


  
  public void setExpressionLanguageScript(String expressionLanguageScript) {
    this.expressionLanguageScript = expressionLanguageScript;
  }


  
  public List<MultiKey> getDropdownValuesAndLabels() {
    return dropdownValuesAndLabels;
  }


  
  public void setDropdownValuesAndLabels(List<MultiKey> dropdownValuesAndLabels) {
    this.dropdownValuesAndLabels = dropdownValuesAndLabels;
  }
  
  
  public List<MultiKey> getCheckboxAttributes() {
    return checkboxAttributes;
  }

  
  public void setCheckboxAttributes(List<MultiKey> checkboxAttributes) {
    this.checkboxAttributes = checkboxAttributes;
  }
  
  
  public int getRepeatGroupIndex() {
    return repeatGroupIndex;
  }

  
  public void setRepeatGroupIndex(int repeatGroupIndex) {
    this.repeatGroupIndex = repeatGroupIndex;
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
    return value;
  }
  
  public boolean isHasValue() {
    return (!this.isExpressionLanguage() && !StringUtils.isBlank(this.getValue()))
        || (this.isExpressionLanguage() && !StringUtils.isBlank(this.getExpressionLanguageScript()));
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
   * @return converted value to correct configItemMetadataType from string
   */
  public Object getObjectValueAllowInvalid() {
    
    ConfigItemMetadataType valueType =  GrouperUtil.defaultIfNull(this.getConfigItemMetadata().getValueType(), ConfigItemMetadataType.STRING);
    
    String value = this.getValueOrExpressionEvaluation();
    if (StringUtils.isBlank(value)) {
      value = this.getDefaultValue();
    }
    
    return valueType.convertValue(value, false);
    
  }
  
  /**
   * 
   * @return if show
   */
  public boolean isShow() {
    
    {
      Boolean showOverride = this.grouperConfigModule.showAttributeOverride(this.configSuffix);
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
    
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.grouperConfigModule.retrieveAttributes().values()) {
      
      variableMap.put(grouperConfigModuleAttribute.getConfigSuffix(), grouperConfigModuleAttribute.getObjectValueAllowInvalid());
      
      
    }

    String showString = GrouperUtil.substituteExpressionLanguage(showEl, variableMap, true, true, true);
    
    return GrouperUtil.booleanValue(showString, true);
  }

}
