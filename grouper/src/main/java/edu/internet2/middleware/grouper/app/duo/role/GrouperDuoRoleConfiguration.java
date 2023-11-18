package edu.internet2.middleware.grouper.app.duo.role;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class GrouperDuoRoleConfiguration extends GrouperProvisioningConfiguration {

  private String duoExternalSystemConfigId;
  
  @Override
  public void configureSpecificSettings() {
    this.duoExternalSystemConfigId = this.retrieveConfigString("duoExternalSystemConfigId", true);
  }

  
  public String getDuoExternalSystemConfigId() {
    return duoExternalSystemConfigId;
  }

}
