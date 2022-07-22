package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;

public class GuiProvisionerConfiguration {
  
  /**
   * provisioner configuration this instance is wrapping
   */
  private ProvisioningConfiguration provisionerConfiguration;
  
  private String lastFullSyncTimestamp;
  private String lastIncrementalSyncTimestamp;
  private int groupCount;
  private int userCount;
  private int membershipCount;
  
  private GuiProvisionerConfiguration() {}
  
  public ProvisioningConfiguration getProvisionerConfiguration() {
    return this.provisionerConfiguration;
  }
  
  
  public String getLastFullSyncTimestamp() {
    return lastFullSyncTimestamp;
  }

  
  public String getLastIncrementalSyncTimestamp() {
    return lastIncrementalSyncTimestamp;
  }

  
  public int getGroupCount() {
    return groupCount;
  }

  
  public int getUserCount() {
    return userCount;
  }

  
  public int getMembershipCount() {
    return membershipCount;
  }

  /**
   * convert from provisioner configuration to gui provisioner configuration
   * @param provisionerConfiguration
   * @return
   */
  public static GuiProvisionerConfiguration convertFromProvisioningConfiguration(ProvisioningConfiguration provisioningConfiguration) {
    
    GuiProvisionerConfiguration guiProvisioningConfig = new GuiProvisionerConfiguration();
    guiProvisioningConfig.provisionerConfiguration = provisioningConfiguration;
    return guiProvisioningConfig;
  }
  
  /**
   * convert from list of provisioner configurations to gui provisioner configurations
   * @param provisioningConfigurations
   * @return
   */
  public static List<GuiProvisionerConfiguration> convertFromProvisioningConfiguration(List<ProvisioningConfiguration> provisioningConfigurations) {
    
    List<GuiProvisionerConfiguration> guiProvisioningConfigurations = new ArrayList<GuiProvisionerConfiguration>();
    
    for (ProvisioningConfiguration provisioningConfiguration: provisioningConfigurations) {
      guiProvisioningConfigurations.add(convertFromProvisioningConfiguration(provisioningConfiguration));
    }
    
    return guiProvisioningConfigurations;
    
  }

}
