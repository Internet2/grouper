package edu.internet2.middleware.grouper.azure;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperAzureConfiguration extends GrouperProvisioningConfigurationBase {

  private String azureExternalSystemConfigId;

  @Override
  public void configureSpecificSettings() {
    
    this.azureExternalSystemConfigId = this.retrieveConfigString("azureExternalSystemConfigId", true);
  }

  public String getAzureExternalSystemConfigId() {
    return azureExternalSystemConfigId;
  }

  public void setAzureExternalSystemConfigId(String azureExternalSystemConfigId) {
    this.azureExternalSystemConfigId = azureExternalSystemConfigId;
  }
}
