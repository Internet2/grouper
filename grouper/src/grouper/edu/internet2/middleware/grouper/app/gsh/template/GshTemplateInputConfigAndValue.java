package edu.internet2.middleware.grouper.app.gsh.template;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfig;

public class GshTemplateInputConfigAndValue {
  
  private GshTemplateInputConfig gshTemplateInputConfig;
  
  private String value;

  
  public GshTemplateInputConfig getGshTemplateInputConfig() {
    return gshTemplateInputConfig;
  }

  
  public void setGshTemplateInputConfig(GshTemplateInputConfig gshTemplateInputConfig) {
    this.gshTemplateInputConfig = gshTemplateInputConfig;
  }

  
  public String getValue() {
    return value;
  }

  
  public void setValue(String value) {
    this.value = value;
  }


  public String getValueOrDefault() {
    if (StringUtils.isBlank(this.value)) {
      return this.gshTemplateInputConfig.getDefaultValue();
    }
    return this.value;
  }
  

}
