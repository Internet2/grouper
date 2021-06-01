package edu.internet2.middleware.grouper.app.duo;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;

public class GrouperDuoConfiguration extends GrouperProvisioningConfigurationBase {

  private String duoExternalSystemConfigId;

  @Override
  public void configureSpecificSettings() {
    
    this.duoExternalSystemConfigId = this.retrieveConfigString("duoExternalSystemConfigId", true);
  }

  
  public String getDuoExternalSystemConfigId() {
    return duoExternalSystemConfigId;
  }

  
  public void setDuoExternalSystemConfigId(String duoExternalSystemConfigId) {
    this.duoExternalSystemConfigId = duoExternalSystemConfigId;
  }

 
}
