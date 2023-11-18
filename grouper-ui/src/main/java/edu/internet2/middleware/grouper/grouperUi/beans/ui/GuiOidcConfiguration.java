package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.oidc.OidcConfiguration;

public class GuiOidcConfiguration {
  
  private OidcConfiguration oidcConfiguration;

  private GuiOidcConfiguration(OidcConfiguration oidcConfiguration) {
    this.oidcConfiguration = oidcConfiguration;
  }
  
  
  public OidcConfiguration getOidcConfiguration() {
    return oidcConfiguration;
  }



  public static GuiOidcConfiguration convertFromOidcConfiguration(OidcConfiguration oidcConfiguration) {
    return new GuiOidcConfiguration(oidcConfiguration);
  }
  
  public static List<GuiOidcConfiguration> convertFromOidcConfiguration(List<OidcConfiguration> oidcConfigurations) {
    
    List<GuiOidcConfiguration> guiOidcConfigs = new ArrayList<GuiOidcConfiguration>();
    
    for (OidcConfiguration oidcConfig: oidcConfigurations) {
      guiOidcConfigs.add(convertFromOidcConfiguration(oidcConfig));
    }
    
    return guiOidcConfigs;
    
  }

}
