package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.authentication.WsTrustedJwtConfiguration;

public class GuiWsTrustedJwtConfiguration {
  
  private WsTrustedJwtConfiguration wsTrustedJwtConfiguration;

  private GuiWsTrustedJwtConfiguration(WsTrustedJwtConfiguration wsTrustedJwtConfiguration) {
    this.wsTrustedJwtConfiguration = wsTrustedJwtConfiguration;
  }
  
  
  public WsTrustedJwtConfiguration getWsTrustedJwtConfiguration() {
    return wsTrustedJwtConfiguration;
  }

  public static GuiWsTrustedJwtConfiguration convertFromWsTrustedJwtConfiguration(WsTrustedJwtConfiguration wsTrustedJwtConfiguration) {
    return new GuiWsTrustedJwtConfiguration(wsTrustedJwtConfiguration);
  }
  
  public static List<GuiWsTrustedJwtConfiguration> convertFromWsTrustedJwtConfiguration(List<WsTrustedJwtConfiguration> wsTrustedJwtConfigurations) {
    
    List<GuiWsTrustedJwtConfiguration> guiWsTrustedJwtConfigs = new ArrayList<GuiWsTrustedJwtConfiguration>();
    
    for (WsTrustedJwtConfiguration wsTrustedJwtConfig: wsTrustedJwtConfigurations) {
      guiWsTrustedJwtConfigs.add(convertFromWsTrustedJwtConfiguration(wsTrustedJwtConfig));
    }
    
    return guiWsTrustedJwtConfigs;
    
  }

}
