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

  
  public Object getValue() {
    return value;
  }

  
  public void setValue(Object value) {
    this.value = value;
  }


  public Object getValueOrDefault() {
    if (GrouperUtil.isBlank(value)) {
      return this.gshTemplateInputConfig.getDefaultValue();
    }
    return this.value;
  }
  

}
