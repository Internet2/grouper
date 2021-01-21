package edu.internet2.middleware.grouper.app.scim2Provisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;

public class GrouperScim2ProvisionerConfiguration extends GrouperProvisioningConfigurationBase {

  private String bearerTokenExternalSystemConfigId;

  @Override
  public void configureSpecificSettings() {
    
    this.bearerTokenExternalSystemConfigId = this.retrieveConfigString("bearerTokenExternalSystemConfigId", true);
  }

  public String getBearerTokenExternalSystemConfigId() {
    return bearerTokenExternalSystemConfigId;
  }

  public void setBearerTokenExternalSystemConfigId(String azureExternalSystemConfigId) {
    this.bearerTokenExternalSystemConfigId = azureExternalSystemConfigId;
  }
}