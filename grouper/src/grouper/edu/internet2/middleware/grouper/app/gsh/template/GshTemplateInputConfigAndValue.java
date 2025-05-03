package edu.internet2.middleware.grouper.app.gsh.template;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GshTemplateInputConfigAndValue {
  
  private GshTemplateInputConfig gshTemplateInputConfig;
  
  private Object value;

  
  public GshTemplateInputConfig getGshTemplateInputConfig() {
    return gshTemplateInputConfig;
  }

  
  public void setGshTemplateInputConfig(GshTemplateInputConfig gshTemplateInputConfig) {
    this.gshTemplateInputConfig = gshTemplateInputConfig;
  }

  /**
   * @deprecated use getValueObject()
   */
  @Deprecated
  public String getValue() {
    return GrouperUtil.stringValue(value);
  }

  
  public Object getValueObject() {
    return value;
  }

  
  public void setValueObject(Object value) {
    this.value = value;
  }


  public Object getValueObjectOrDefault() {
    if (GrouperUtil.isBlank(value)) {
      return this.gshTemplateInputConfig.getDefaultValue();
    }
    return this.value;
  }
  
  /**
   * @deprecated use setValueObject(Object value)
   */
  @Deprecated
  public void setValue(String value) {
    this.value = value;
  }


  /**
   * @deprecated use getValueObjectOrDefault(Object value)
   * and handle File return types
   */
  @Deprecated
  public String getValueOrDefault() {
    if (GrouperUtil.isBlank(this.value)) {
      return this.gshTemplateInputConfig.getDefaultValue();
    }
    return GrouperUtil.stringValue(this.value);
  }

}
