package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.customUi.CustomUiConfiguration;

public class GuiCustomUiConfiguration {
  
  private CustomUiConfiguration customUiConfiguration;

  private GuiCustomUiConfiguration(CustomUiConfiguration customUiConfiguration) {
    this.customUiConfiguration = customUiConfiguration;
  }
  
  public CustomUiConfiguration getCustomUiConfiguration() {
    return customUiConfiguration;
  }

  public static GuiCustomUiConfiguration convertFromCustomUiConfiguration(CustomUiConfiguration customUiConfiguration) {
    return new GuiCustomUiConfiguration(customUiConfiguration);
  }
  
  public static List<GuiCustomUiConfiguration> convertFromCustomUiConfiguration(List<CustomUiConfiguration> customUiConfigurations) {
    
    List<GuiCustomUiConfiguration> guiCustomUiConfigs = new ArrayList<GuiCustomUiConfiguration>();
    
    for (CustomUiConfiguration gshTemplateConfiguration: customUiConfigurations) {
      guiCustomUiConfigs.add(convertFromCustomUiConfiguration(gshTemplateConfiguration));
    }
    
    return guiCustomUiConfigs;
    
  }

}
