package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerConfiguration;

public class GuiProvisionerConfiguration {
  
  private ProvisionerConfiguration provisionerConfiguration;
  
  private GuiProvisionerConfiguration(ProvisionerConfiguration provisionerConfiguration) {
    this.provisionerConfiguration = provisionerConfiguration;
  }
  
  public ProvisionerConfiguration getProvisionerConfiguration() {
    return this.provisionerConfiguration;
  }

  public static GuiProvisionerConfiguration convertFromProvisionerConfiguration(ProvisionerConfiguration provisionerConfiguration) {
    return new GuiProvisionerConfiguration(provisionerConfiguration);
  }
  
  public static List<GuiProvisionerConfiguration> convertFromProvisionerConfiguration(List<ProvisionerConfiguration> provisionerConfigurations) {
    
    List<GuiProvisionerConfiguration> guiProvisionerConfigurations = new ArrayList<GuiProvisionerConfiguration>();
    
    for (ProvisionerConfiguration provisionerConfiguration: provisionerConfigurations) {
      guiProvisionerConfigurations.add(convertFromProvisionerConfiguration(provisionerConfiguration));
    }
    
    return guiProvisionerConfigurations;
    
  }

}
