package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.attr.resolver.GlobalAttributeResolverConfiguration;

public class GuiGlobalAttributeResolverConfiguration {
  
  private GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration;

  private GuiGlobalAttributeResolverConfiguration(GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration) {
    this.globalAttributeResolverConfiguration = globalAttributeResolverConfiguration;
  }
  

  public static GuiGlobalAttributeResolverConfiguration convertFromGlobalAttributeResolverConfiguration(GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration) {
    return new GuiGlobalAttributeResolverConfiguration(globalAttributeResolverConfiguration);
  }
  
  public static List<GuiGlobalAttributeResolverConfiguration> convertFromWsTrustedJwtConfiguration(List<GlobalAttributeResolverConfiguration> globalAttributeResolverConfigurations) {
    
    List<GuiGlobalAttributeResolverConfiguration> guiGlobalAttributeResolverConfigs = new ArrayList<GuiGlobalAttributeResolverConfiguration>();
    
    for (GlobalAttributeResolverConfiguration globalAttributeResolverConfig: globalAttributeResolverConfigurations) {
      guiGlobalAttributeResolverConfigs.add(convertFromGlobalAttributeResolverConfiguration(globalAttributeResolverConfig));
    }
    
    return guiGlobalAttributeResolverConfigs;
    
  }

  public GlobalAttributeResolverConfiguration getGlobalAttributeResolverConfiguration() {
    return globalAttributeResolverConfiguration;
  }
  
}
