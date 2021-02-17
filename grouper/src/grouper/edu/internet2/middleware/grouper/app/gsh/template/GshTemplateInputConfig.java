package edu.internet2.middleware.grouper.app.gsh.template;


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
  
  
}
