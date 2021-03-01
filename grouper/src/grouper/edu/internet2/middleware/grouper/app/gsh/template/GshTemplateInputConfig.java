package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GshTemplateInputConfig {
  
  private GshTemplateConfig gshTemplateConfig;
  
  private String name;
  
  private GshTemplateInputType gshTemplateInputType;
  
  private GshTemplateInputValidationType gshTemplateInputValidationType;
  
  private String validationRegex;
  
  private String validationJexl;
  
  private ValidationBuiltinType validationBuiltinType;
  
  private boolean required;
  
  private boolean trimWhitespace = true;
  
  private String defaultValue;
  
  private String validationMessage;
  
  private String validationMessageExternalizedTextKey;
  
  private Integer maxLength;
  
  private GshTemplateFormElementType gshTemplateFormElementType;
  
  private GshTemplateDropdownValueFormatType gshTemplateDropdownValueFormatType;
  
  private String dropdownCsvValue;
  
  private String dropdownJsonValue;
  
  private String dropdownJavaClassValue;
  

  public String getDropdownValueBasedOnType() {
    
    if (StringUtils.isNotBlank(dropdownCsvValue)) {
      return dropdownCsvValue;
    } else if (StringUtils.isNotBlank(dropdownJsonValue)) {
      return dropdownJsonValue;
    } else {
      return dropdownJavaClassValue;
    }
    
  }
  
  
  public String getDropdownCsvValue() {
    return dropdownCsvValue;
  }



  
  public void setDropdownCsvValue(String dropdownCsvValue) {
    this.dropdownCsvValue = dropdownCsvValue;
  }



  
  public String getDropdownJsonValue() {
    return dropdownJsonValue;
  }



  
  public void setDropdownJsonValue(String dropdownJsonValue) {
    this.dropdownJsonValue = dropdownJsonValue;
  }



  
  public String getDropdownJavaClassValue() {
    return dropdownJavaClassValue;
  }



  
  public void setDropdownJavaClassValue(String dropdownJavaClassValue) {
    this.dropdownJavaClassValue = dropdownJavaClassValue;
  }



  public GshTemplateFormElementType getGshTemplateFormElementType() {
    return gshTemplateFormElementType;
  }


  
  public void setGshTemplateFormElementType(
      GshTemplateFormElementType gshTemplateFormElementType) {
    this.gshTemplateFormElementType = gshTemplateFormElementType;
  }


  
  public GshTemplateDropdownValueFormatType getGshTemplateDropdownValueFormatType() {
    return gshTemplateDropdownValueFormatType;
  }


  
  public void setGshTemplateDropdownValueFormatType(GshTemplateDropdownValueFormatType gshTemplateDropdownValueFormatType) {
    this.gshTemplateDropdownValueFormatType = gshTemplateDropdownValueFormatType;
  }


  public GshTemplateConfig getGshTemplateConfig() {
    return gshTemplateConfig;
  }

  
  public void setGshTemplateConfig(GshTemplateConfig gshTemplateConfig) {
    this.gshTemplateConfig = gshTemplateConfig;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public GshTemplateInputType getGshTemplateInputType() {
    return gshTemplateInputType;
  }

  
  public void setGshTemplateInputType(GshTemplateInputType gshTemplateInputType) {
    this.gshTemplateInputType = gshTemplateInputType;
  }

  
  public GshTemplateInputValidationType getGshTemplateInputValidationType() {
    return gshTemplateInputValidationType;
  }

  
  public void setGshTemplateInputValidationType(
      GshTemplateInputValidationType gshTemplateInputValidationType) {
    this.gshTemplateInputValidationType = gshTemplateInputValidationType;
  }

  
  public String getValidationRegex() {
    return validationRegex;
  }

  
  public void setValidationRegex(String validationRegex) {
    this.validationRegex = validationRegex;
  }

  
  public String getValidationJexl() {
    return validationJexl;
  }

  
  public void setValidationJexl(String validationJexl) {
    this.validationJexl = validationJexl;
  }

  
  public boolean isRequired() {
    return required;
  }

  
  public void setRequired(boolean required) {
    this.required = required;
  }

  
  public String getDefaultValue() {
    return defaultValue;
  }

  
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }


  
  public ValidationBuiltinType getValidationBuiltinType() {
    return validationBuiltinType;
  }


  
  public void setValidationBuiltinType(ValidationBuiltinType validationBuiltinType) {
    this.validationBuiltinType = validationBuiltinType;
  }


  
  public boolean isTrimWhitespace() {
    return trimWhitespace;
  }


  
  public void setTrimWhitespace(boolean trimWhitespace) {
    this.trimWhitespace = trimWhitespace;
  }


  
  public String getValidationMessage() {
    return validationMessage;
  }


  
  public void setValidationMessage(String validationMessage) {
    this.validationMessage = validationMessage;
  }


  
  public String getValidationMessageExternalizedTextKey() {
    return validationMessageExternalizedTextKey;
  }


  
  public void setValidationMessageExternalizedTextKey(
      String validationMessageExternalizedTextKey) {
    this.validationMessageExternalizedTextKey = validationMessageExternalizedTextKey;
  }


  
  public Integer getMaxLength() {
    return maxLength;
  }


  
  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }
  
  
}
