package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Map;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig;

public class GuiGshTemplateConfig {
  
  private GshTemplateConfig gshTemplateConfig;
  
  private Map<String, GuiGshTemplateInputConfig> guiGshTemplateInputConfigs;
  
  
  
  public Map<String, GuiGshTemplateInputConfig> getGuiGshTemplateInputConfigs() {
    return guiGshTemplateInputConfigs;
  }


  
  public void setGuiGshTemplateInputConfigs(
      Map<String, GuiGshTemplateInputConfig> guiGshTemplateInputConfigs) {
    this.guiGshTemplateInputConfigs = guiGshTemplateInputConfigs;
  }


  public GshTemplateConfig getGshTemplateConfig() {
    return gshTemplateConfig;
  }

  
  public void setGshTemplateConfig(GshTemplateConfig gshTemplateConfig) {
    this.gshTemplateConfig = gshTemplateConfig;
  }
  

}
