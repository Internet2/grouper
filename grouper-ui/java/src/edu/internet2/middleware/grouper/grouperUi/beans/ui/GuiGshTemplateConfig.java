package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Map;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInputConfigAndValue;

public class GuiGshTemplateConfig {
  
  private GshTemplateConfig gshTemplateConfig;
  
  private Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues;
  
  
  
  public Map<String, GshTemplateInputConfigAndValue> getGshTemplateInputConfigAndValues() {
    return gshTemplateInputConfigAndValues;
  }


  
  public void setGshTemplateInputConfigAndValues(
      Map<String, GshTemplateInputConfigAndValue> gshTemplateInputConfigAndValues) {
    this.gshTemplateInputConfigAndValues = gshTemplateInputConfigAndValues;
  }


  public GshTemplateConfig getGshTemplateConfig() {
    return gshTemplateConfig;
  }

  
  public void setGshTemplateConfig(GshTemplateConfig gshTemplateConfig) {
    this.gshTemplateConfig = gshTemplateConfig;
  }
  

}
