package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerConfiguration;

public class GuiProvisionerConfiguration {
  
  /**
   * provisioner configuration this instance is wrapping
   */
  private ProvisionerConfiguration provisionerConfiguration;
  
  private GuiProvisionerConfiguration(ProvisionerConfiguration provisionerConfiguration) {
    this.provisionerConfiguration = provisionerConfiguration;
  }
  
  public ProvisionerConfiguration getProvisionerConfiguration() {
    return this.provisionerConfiguration;
  }

  /**
   * convert from provisioner configuration to gui provisioner configuration
   * @param provisionerConfiguration
   * @return
   */
  public static GuiProvisionerConfiguration convertFromProvisionerConfiguration(ProvisionerConfiguration provisionerConfiguration) {
    return new GuiProvisionerConfiguration(provisionerConfiguration);
  }
  
  /**
   * convert from list of provisioner configurations to gui provisioner configurations
   * @param provisionerConfigurations
   * @return
   */
  public static List<GuiProvisionerConfiguration> convertFromProvisionerConfiguration(List<ProvisionerConfiguration> provisionerConfigurations) {
    
    List<GuiProvisionerConfiguration> guiProvisionerConfigurations = new ArrayList<GuiProvisionerConfiguration>();
    
    for (ProvisionerConfiguration provisionerConfiguration: provisionerConfigurations) {
      guiProvisionerConfigurations.add(convertFromProvisionerConfiguration(provisionerConfiguration));
    }
    
    return guiProvisionerConfigurations;
    
  }

}
