package edu.internet2.middleware.grouper.app.remedy;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;

public class GrouperRemedyConfiguration extends GrouperProvisioningConfigurationBase {
  
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
