package edu.internet2.middleware.grouper.app.remedyV2;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class GrouperRemedyConfiguration extends GrouperProvisioningConfiguration {

  private String remedyExternalSystemConfigId;

  @Override
  public void configureSpecificSettings() {
    this.remedyExternalSystemConfigId = this.retrieveConfigString("remedyExternalSystemConfigId", true);
  }

  public String getRemedyExternalSystemConfigId() {
    return remedyExternalSystemConfigId;
  }

  public void setRemedyExternalSystemConfigId(String remedyExternalSystemConfigId) {
    this.remedyExternalSystemConfigId = remedyExternalSystemConfigId;
  }
  
}
