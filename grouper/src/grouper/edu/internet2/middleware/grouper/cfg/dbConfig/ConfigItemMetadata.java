/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

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
    
    if (!StringUtils.isBlank(this.rawMetadataJson)) {
      
      JSONObject jsonObject = JSONObject.fromObject(this.rawMetadataJson);
      
      if (jsonObject.containsKey("multiple")) {
        this.multiple = jsonObject.getBoolean("multiple");
        jsonObject.remove("multiple");
      }
      
      if (jsonObject.containsKey("mustExtendClass")) {
        this.mustExtendClass = jsonObject.getString("mustExtendClass");
        jsonObject.remove("mustExtendClass");
      }
      
      if (jsonObject.containsKey("mustImplementInterface")) {
        this.mustImplementInterface = jsonObject.getString("mustImplementInterface");
        jsonObject.remove("mustImplementInterface");
      }
      
      if (jsonObject.containsKey("regex")) {
        this.regex = jsonObject.getString("regex");
        jsonObject.remove("regex");
      }
      
      if (jsonObject.containsKey("required")) {
        this.required = jsonObject.getBoolean("required");
        jsonObject.remove("required");
      }
      
      if (jsonObject.containsKey("requiresRestart")) {
        this.requiresRestart = jsonObject.getBoolean("requiresRestart");
        jsonObject.remove("requiresRestart");
      }
      
      if (jsonObject.containsKey("sampleValue")) {
        this.sampleValue = jsonObject.getString("sampleValue");
        jsonObject.remove("sampleValue");
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
  
  
  
}
