package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfiguration;

public class GuiGshTemplateConfiguration {

  private GshTemplateConfiguration gshTemplateConfiguration;

  private GuiGshTemplateConfiguration(GshTemplateConfiguration gshTemplateConfiguration) {
    this.gshTemplateConfiguration = gshTemplateConfiguration;
  }
  
  public GshTemplateConfiguration getGshTemplateConfiguration() {
    return gshTemplateConfiguration;
  }

  public static GuiGshTemplateConfiguration convertFromGshTemplateConfiguration(GshTemplateConfiguration gshTemplateConfiguration) {
    return new GuiGshTemplateConfiguration(gshTemplateConfiguration);
  }
  
  public static List<GuiGshTemplateConfiguration> convertFromGshTemplateConfiguration(List<GshTemplateConfiguration> gshTemplateConfigurations) {
    
    List<GuiGshTemplateConfiguration> guiGshTemplateConfigs = new ArrayList<GuiGshTemplateConfiguration>();
    
    for (GshTemplateConfiguration gshTemplateConfiguration: gshTemplateConfigurations) {
      guiGshTemplateConfigs.add(convertFromGshTemplateConfiguration(gshTemplateConfiguration));
    }
    
    return guiGshTemplateConfigs;
    
  }
  
}
