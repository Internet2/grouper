package edu.internet2.middleware.grouper.app.duo.role;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;

public class GrouperDuoRoleConfiguration extends GrouperProvisioningConfigurationBase {

  private String duoExternalSystemConfigId;
  
  @Override
  public void configureSpecificSettings() {
    this.duoExternalSystemConfigId = this.retrieveConfigString("duoExternalSystemConfigId", true);
  }

  
  public String getDuoExternalSystemConfigId() {
    return duoExternalSystemConfigId;
  }

}
