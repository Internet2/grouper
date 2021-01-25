/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class ConfigItemMetadata {

  /**
   * 
   */
  public ConfigItemMetadata() {
  }

  /**
   * 
   * @return key or sample key
   */
  public String getKeyOrSampleKey() {
    String result = this.key;
    if (StringUtils.isBlank(this.key)) {
      result = this.sampleKey;
    }
    if (result != null) {
      if (result.endsWith(".elConfig")) {
        return GrouperUtil.stripSuffix(result, ".elConfig");
      }
    }
    return result;
  }
  
  /**
   * if sample property
   * @return key or sample key
   */
  public boolean isSampleProperty() {
    return StringUtils.isBlank(this.key);
  }
  
  /**
   * if commented out property, this is the sample key
   */
  private String sampleKey;

  /**
   * if commented out property, this is the sample key
   * @return the sample key
   */
  public String getSampleKey() {
    return this.sampleKey;
  }

  /**
   * if commented out property, this is the sample key
   * @param sampleKey
   */
  public void setSampleKey(String sampleKey) {
    this.sampleKey = sampleKey;
  }

  /**
   * an example value for the property
   */
  private String sampleValue;
  
  
  /**
   * an example value for the property
   * @return the sampleValue
   */
  public String getSampleValue() {
    return this.sampleValue;
  }

  
  /**
   * an example value for the property
   * @param sampleValue1 the sampleValue to set
   */
  public void setSampleValue(String sampleValue1) {
    this.sampleValue = sampleValue1;
  }

  /**
   * if changing this property requires restart of grouper
   */
  private boolean requiresRestart;
  
  /**
   * if changing this property requires restart of grouper
   * @return the requiresRestart
   */
  public boolean isRequiresRestart() {
    return this.requiresRestart;
  }
  
  /**
   * if changing this property requires restart of grouper
   * @param requiresRestart1 the requiresRestart to set
   */
  public void setRequiresRestart(boolean requiresRestart1) {
    this.requiresRestart = requiresRestart1;
  }

  /**
   * if the metadata json was processed successfully
   */
  private Boolean metadataProcessedSuccessfully;
  
  /**
   * if the metadata json was processed successfully
   * @return the metadataProcessedSuccessfully
   */
  public Boolean getMetadataProcessedSuccessfully() {
    return this.metadataProcessedSuccessfully;
  }
  
  /**
   * if the metadata json was processed successfully
   * @param metadataProcessedSuccessfully1 the metadataProcessedSuccessfully to set
   */
  public void setMetadataProcessedSuccessfully(Boolean metadataProcessedSuccessfully1) {
    this.metadataProcessedSuccessfully = metadataProcessedSuccessfully1;
  }

  /**
   * if metadata json was converted to json successfully and valid
   */
  private String metadataError;
  
  /**
   * if metadata json was converted to json successfully and valid
   * @return the metadataError
   */
  public String getMetadataError() {
    return this.metadataError;
  }
  
  /**
   * if metadata json was converted to json successfully and valid
   * @param metadataError1 the metadataError to set
   */
  public void setMetadataError(String metadataError1) {
    this.metadataError = metadataError1;
  }

  /**
   * common label of repeated configs that should be in a repeated block
   */
  private String repeatGroup;
  
  /**
   * common label of repeated configs that should be in a repeated block
   * @return
   */
  public String getRepeatGroup() {
    return repeatGroup;
  }

  /**
   * common label of repeated configs that should be in a repeated block
   * @param repeatGroup
   */
  public void setRepeatGroup(String repeatGroup) {
    this.repeatGroup = repeatGroup;
  }
  
  /**
   * how many times a property should be repeated
   */
  private int repeatCount;
  
  /**
   * how many times a property should be repeated
   * @return
   */
  public int getRepeatCount() {
    return repeatCount;
  }

  /**
   * how many times a property should be repeated
   * @param repeatCount
   */
  public void setRepeatCount(int repeatCount) {
    this.repeatCount = repeatCount;
  }
  
  /**
   * repeat group index - 0 based. only applicable for repeat groups
   */
  private int repeatGroupIndex;
  
  
  public int getRepeatGroupIndex() {
    return repeatGroupIndex;
  }

  
  public void setRepeatGroupIndex(int repeatGroupIndex) {
    this.repeatGroupIndex = repeatGroupIndex;
  }

  /**
   * used for ordering elements
   */
  private int order;
  
  /**
   * @return used for ordering elements
   */
  public int getOrder() {
    return order;
  }

  /**
   * used for ordering elements
   * @param order
   */
  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * put a label to group items together.  if blank then all in default subsection
   */
  private String subSection;
  
  
  
  /**
   * put a label to group items together.  if blank then all in default subsection
   * @return
   */
  public String getSubSection() {
    return subSection;
  }

  /**
   * put a label to group items together.  if blank then all in default subsection
   * @param section
   */
  public void setSubSection(String section) {
    this.subSection = section;
  }

  /**
   * raw json string in the properties file
   */
  private String rawMetadataJson;
  
  /**
   * raw json string in the properties file
   * @return the rawMetadataJson
   */
  public String getRawMetadataJson() {
    return this.rawMetadataJson;
  }
  
  /**
   * raw json string in the properties file
   * @param rawMetadataJson1 the rawMetadataJson to set
   */
  public void setRawMetadataJson(String rawMetadataJson1) {
    this.rawMetadataJson = rawMetadataJson1;
    
  }

  /**
   * if this is set, put in an expression language that can depend on other attribute suffixes
   * to see if an item is shown or not
   */
  private String showEl;

  
  
  /**
   * if this is set, put in an expression language that can depend on other attribute suffixes
   * to see if an item is shown or not
   * @return
   */
  public String getShowEl() {
    return showEl;
  }

  /**
   * if this is set, put in an expression language that can depend on other attribute suffixes
   * to see if an item is shown or not
   * @param showEl
   */
  public void setShowEl(String showEl) {
    this.showEl = showEl;
  }
  
  /**
   * if this is set, put in an expression language that can depend on other attribute suffixes
   * to see if an item is required or not
   */
  private String requiredEl;
  

  /**
   * if this is set, put in an expression language that can depend on other attribute suffixes
   * to see if an item is required or not
   * @return
   */
  public String getRequiredEl() {
    return requiredEl;
  }

  /**
   * if this is set, put in an expression language that can depend on other attribute suffixes
   * to see if an item is required or not
   * @param requiredEl
   */
  public void setRequiredEl(String requiredEl) {
    this.requiredEl = requiredEl;
  }

  /**
   * replace $i$ with repeat index
   * @param repeatIndex
   * @return
   */
  public ConfigItemMetadata clone(int repeatIndex) {
    
    ConfigItemMetadata copy = new ConfigItemMetadata();
    copy.checkboxValuesFromClass = GrouperUtil.replace(this.checkboxValuesFromClass, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.comment = GrouperUtil.replace(this.comment, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.key = GrouperUtil.replace(this.key, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.mustExtendClass = GrouperUtil.replace(this.mustExtendClass, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.mustImplementInterface = GrouperUtil.replace(this.mustImplementInterface, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.optionValuesFromClass = GrouperUtil.replace(this.optionValuesFromClass, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.regex = GrouperUtil.replace(this.regex, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.repeatGroup = GrouperUtil.replace(this.repeatGroup, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.requiredEl = GrouperUtil.replace(this.requiredEl, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.defaultValueEl = GrouperUtil.replace(this.defaultValueEl, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.defaultValue = GrouperUtil.replace(this.defaultValue, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.sampleKey = GrouperUtil.replace(this.sampleKey, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.sampleValue = GrouperUtil.replace(this.sampleValue, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.showEl = GrouperUtil.replace(this.showEl, "$i$", GrouperUtil.stringValue(repeatIndex));
    copy.subSection = GrouperUtil.replace(this.subSection, "$i$", GrouperUtil.stringValue(repeatIndex));
    
    copy.formElement = this.formElement;
    copy.multiple = this.multiple;
    copy.optionValues = this.optionValues;
    copy.order = this.order;
    copy.readOnly = this.readOnly;
    copy.requiresRestart = this.requiresRestart;
    copy.required = this.required;
    copy.sensitive = this.sensitive;
    copy.valueType = this.valueType;
//    copy.rawMetadataJson = this.rawMetadataJson;
//    copy.repeatCount = this.repeatCount;
//    copy.value = this.valueType;
    
    return copy;
  }
  
  /**
   * 
   */
  public void processMetadata() {
    
    this.multiple = false;
    this.mustExtendClass = null;
    this.mustImplementInterface = null;
    this.regex = null;
    this.required = false;
    this.requiresRestart = false;
    this.sampleValue = null;
    this.sensitive = false;
    this.valueType = null;
    this.subSection = null;
    this.showEl = null;
    this.requiredEl = null;
    this.formElement = null;
    this.optionValues = null;
    this.optionValuesFromClass = null;
    this.checkboxValuesFromClass = null;
    this.readOnly = false;
    this.repeatGroup = null;
    this.defaultValue = null;
    this.defaultValueEl = null;
    this.repeatCount = 0;
    this.order = 0;
    
    if (!StringUtils.isBlank(this.rawMetadataJson)) {
      
      JSONObject jsonObject = JSONObject.fromObject(this.rawMetadataJson);
      
      if (jsonObject.containsKey("multiple")) {
        this.multiple = jsonObject.getBoolean("multiple");
        jsonObject.remove("multiple");
      }
      
      if (jsonObject.containsKey("repeatGroup")) {
        this.repeatGroup = jsonObject.getString("repeatGroup");
        jsonObject.remove("repeatGroup");
      }
      
      if (jsonObject.containsKey("repeatCount")) {
        this.repeatCount = jsonObject.getInt("repeatCount");
        jsonObject.remove("repeatCount");
      }
      
      if (jsonObject.containsKey("order")) {
        this.order = jsonObject.getInt("order");
        jsonObject.remove("order");
      }
      
      if (jsonObject.containsKey("mustExtendClass")) {
        this.mustExtendClass = jsonObject.getString("mustExtendClass");
        jsonObject.remove("mustExtendClass");
      }
      
      if (jsonObject.containsKey("mustImplementInterface")) {
        this.mustImplementInterface = jsonObject.getString("mustImplementInterface");
        jsonObject.remove("mustImplementInterface");
      }
      
      if (jsonObject.containsKey("optionValuesFromClass")) {
        this.optionValuesFromClass = jsonObject.getString("optionValuesFromClass");
        jsonObject.remove("optionValuesFromClass");
      }
      
      if (jsonObject.containsKey("checkboxValuesFromClass")) {
        this.checkboxValuesFromClass = jsonObject.getString("checkboxValuesFromClass");
        jsonObject.remove("checkboxValuesFromClass");
      }
      
      if (jsonObject.containsKey("regex")) {
        this.regex = jsonObject.getString("regex");
        jsonObject.remove("regex");
      }
      
      if (jsonObject.containsKey("defaultValueEl")) {
        this.defaultValueEl = jsonObject.getString("defaultValueEl");
        jsonObject.remove("defaultValueEl");
      }
      
      if (jsonObject.containsKey("required")) {
        this.required = jsonObject.getBoolean("required");
        jsonObject.remove("required");
      }
      
      if (jsonObject.containsKey("readOnly")) {
        this.readOnly = jsonObject.getBoolean("readOnly");
        jsonObject.remove("readOnly");
      }
      
      if (jsonObject.containsKey("requiresRestart")) {
        this.requiresRestart = jsonObject.getBoolean("requiresRestart");
        jsonObject.remove("requiresRestart");
      }
      
      if (jsonObject.containsKey("sampleValue")) {
        this.sampleValue = jsonObject.getString("sampleValue");
        jsonObject.remove("sampleValue");
      }
      
      if (jsonObject.containsKey("subSection")) {
        this.subSection = jsonObject.getString("subSection");
        jsonObject.remove("subSection");
      }
      
      if (jsonObject.containsKey("sensitive")) {
        this.sensitive = jsonObject.getBoolean("sensitive");
        jsonObject.remove("sensitive");
      }
      
      if (jsonObject.containsKey("valueType")) {
        this.valueType = ConfigItemMetadataType.valueOfIgnoreCase(jsonObject.getString("valueType"), false);
        jsonObject.remove("valueType");
      }
      
      if (jsonObject.containsKey("defaultValue")) {
        this.defaultValue = jsonObject.getString("defaultValue");
        jsonObject.remove("defaultValue");
      }
      
      if (jsonObject.containsKey("showEl")) {
        this.showEl = jsonObject.getString("showEl");
        jsonObject.remove("showEl");
      }
      
      if (jsonObject.containsKey("requiredEl")) {
        this.requiredEl = jsonObject.getString("requiredEl");
        jsonObject.remove("requiredEl");
      }
      
      if (jsonObject.containsKey("formElement")) {
        String formElementString = jsonObject.getString("formElement");
        this.formElement = ConfigItemFormElement.valueOfIgnoreCase(formElementString, true);
        jsonObject.remove("formElement");
      }
      
      if (jsonObject.containsKey("optionValues")) {
        JSONArray jsonArray = jsonObject.getJSONArray("optionValues");
        this.optionValues = new String[jsonArray.size()];
        for (int i=0;i<this.optionValues.length;i++) {
          this.optionValues[i] = jsonArray.getString(i);
        }
        jsonObject.remove("optionValues");
      }
      
      
      if (jsonObject.keySet().size() > 0) {
        this.metadataError = "Extra keys from json (unexpected): " + GrouperUtil.join(jsonObject.keySet().iterator(), ", ");
        throw new RuntimeException(this.metadataError);
      }
    }
  }
  
  /** value in config file */
  private String value;
  
  /**
   * value in config file
   * @return the value
   */
  public String getValue() {
    return this.value;
  }
  
  /**
   * value in config file
   * @param value1 the value to set
   */
  public void setValue(String value1) {
    this.value = value1;
  }

  /**
   * if this is a required field
   */
  private boolean required;
  
  /**
   * if this is a required field
   * @return the required
   */
  public boolean isRequired() {
    return this.required;
  }

  /**
   * if this is a required field
   * @param required1 the required to set
   */
  public void setRequired(boolean required1) {
    this.required = required1;
  }

  /**
   * if this is a drop down, these are the acceptable values
   */
  private String[] optionValues; 
  
  /**
   * if this is a drop down, these are the acceptable values
   * @return option values
   */
  public String[] getOptionValues() {
    return optionValues;
  }

  /**
   * if this is a drop down, these are the acceptable values
   * @param optionValues
   */
  public void setOptionValues(String[] optionValues) {
    this.optionValues = optionValues;
  }

  /**
   * must be in ConfigItemFormElement enum
   */
  private ConfigItemFormElement formElement;

  /**
   * must be in ConfigItemFormElement enum
   * @return config item form element
   */
  public ConfigItemFormElement getFormElement() {
    return formElement;
  }

  /**
   * must be in ConfigItemFormElement enum
   * @param formElement1
   */
  public void setFormElement(ConfigItemFormElement formElement1) {
    this.formElement = formElement1;
  }

  /**
   * value type, e.g. boolean, string, group, subject, integer, floating, etc
   */
  private ConfigItemMetadataType valueType;
  
  
  /**
   * value type, e.g. boolean, string, group, subject, integer, floating, etc
   * @return the valueType
   */
  public ConfigItemMetadataType getValueType() {
    return this.valueType;
  }

  
  /**
   * value type, e.g. boolean, string, group, subject, integer, floating, etc
   * @param valueType1 the valueType to set
   */
  public void setValueType(ConfigItemMetadataType valueType1) {
    this.valueType = valueType1;
  }

  /**
   * if this can be a list of values, this is the regex
   */
  private String regex;
  
  /**
   * if this can be a list of values, this is the regex
   * @return the regex
   */
  public String getRegex() {
    return this.regex;
  }
  
  /**
   * if this can be a list of values, this is the regex
   * @param regex1 the regex to set
   */
  public void setRegex(String regex1) {
    this.regex = regex1;
  }

  /**
   * if multiple values are allowed (e.g. comma separated)
   */
  private boolean multiple = false;

  /**
   * if multiple values are allowed (e.g. comma separated)
   * @return the multiple
   */
  public boolean isMultiple() {
    return this.multiple;
  }

  /**
   * if multiple values are allowed (e.g. comma separated)
   * @param multiple1 the multiple to set
   */
  public void setMultiple(boolean multiple1) {
    this.multiple = multiple1;
  }

  /**
   * if this is a password or other private field that probably shouldnt be displayed
   * especially if the password or encrypted password is listed in the value
   */
  private boolean sensitive = false;
  
  /**
   * if this is a password or other private field that probably shouldnt be displayed
   * especially if the password or encrypted password is listed in the value
   * @return the sensitive
   */
  public boolean isSensitive() {
    return this.sensitive;
  }

  /**
   * if this is a password or other private field that probably shouldnt be displayed
   * especially if the password or encrypted password is listed in the value
   * @param sensitive1 the sensitive to set
   */
  public void setSensitive(boolean sensitive1) {
    this.sensitive = sensitive1;
  }

  /**
   * key to this value (optional)
   */
  private String key;
  
  /**
   * key to this value (optional)
   * @return the key
   */
  public String getKey() {
    return this.key;
  }
  
  /**
   * key to this value (optional)
   * @param key1 the key to set
   */
  public void setKey(String key1) {
    this.key = key1;
  }

  /**
   * default value
   */
  private String defaultValue;
  
  /**
   * default value
   * @return the defaultValue
   */
  public String getDefaultValue() {
    return this.defaultValue;
  }
  
  /**
   * default value
   * @param defaultValue the defaultValue to set
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * comments in the file about this item
   */
  private String comment;

  /**
   * comments in the file about this item
   * @return the comment
   */
  public String getComment() {
    return this.comment;
  }
  
  /**
   * comments in the file about this item
   * @param comment1 the comment to set
   */
  public void setComment(String comment1) {
    this.comment = comment1;
  }
  
  /**
   * default value EL expression
   */
  private String defaultValueEl;
  
  /**
   * default value EL expression
   * @return
   */
  public String getDefaultValueEl() {
    return defaultValueEl;
  }

  /**
   * default value EL expression
   * @param defaultValueEl
   */
  public void setDefaultValueEl(String defaultValueEl) {
    this.defaultValueEl = defaultValueEl;
  }

  /**
   * fully qualified classname that this value which is a class must extend
   */
  private String mustExtendClass;

  
  /**
   * fully qualified classname that this value which is a class must extend
   * @return the mustExtendClass
   */
  public String getMustExtendClass() {
    return this.mustExtendClass;
  }

  
  /**
   * fully qualified classname that this value which is a class must extend
   * @param mustExtendClass1 the mustExtendClass to set
   */
  public void setMustExtendClass(String mustExtendClass1) {
    this.mustExtendClass = mustExtendClass1;
  }

  /**
   * fully qualified interface that this value which is a class must implement
   */
  private String mustImplementInterface;

  
  /**
   * fully qualified interface that this value which is a class must implement
   * @return the mustImplementInterface
   */
  public String getMustImplementInterface() {
    return this.mustImplementInterface;
  }

  
  /**
   * fully qualified interface that this value which is a class must implement
   * @param mustImplementInterface1 the mustImplementInterface to set
   */
  public void setMustImplementInterface(String mustImplementInterface1) {
    this.mustImplementInterface = mustImplementInterface1;
  }

  /**
   * is this property read only?
   */
  private boolean readOnly;

  /**
   * is this property read only?
   * @return
   */
  public boolean isReadOnly() {
    return this.readOnly;
  }

  /**
   * is this property read only
   * @param readOnly
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }
  
  /**
   * option values from class for dropdowns
   */
  private String optionValuesFromClass;

  /**
   * @return option values from class for dropdowns
   */
  public String getOptionValuesFromClass() {
    return optionValuesFromClass;
  }

  /**
   * option values from class for dropdowns
   * @param optionValuesFromClass
   */
  public void setOptionValuesFromClass(String optionValuesFromClass) {
    this.optionValuesFromClass = optionValuesFromClass;
  }
  
  /**
   * checkbox values, labels, and checked for checkboxes 
   */
  private String checkboxValuesFromClass;

  /**
   * checkbox values, labels, and checked for checkboxes
   * @return
   */
  public String getCheckboxValuesFromClass() {
    return checkboxValuesFromClass;
  }

  /**
   * checkbox values, labels, and checked for checkboxes
   * @param checkboxValuesFromClass
   */
  public void setCheckboxValuesFromClass(String checkboxValuesFromClass) {
    this.checkboxValuesFromClass = checkboxValuesFromClass;
  }
  
}
