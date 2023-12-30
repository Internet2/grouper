package edu.internet2.middleware.grouper.app.teamDynamix;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class TeamDynamixProvisioningConfiguration extends GrouperProvisioningConfiguration {

  private String teamDynamixExternalSystemConfigId;
  
  @Override
  public void configureSpecificSettings() {
    this.teamDynamixExternalSystemConfigId = this.retrieveConfigString("teamDynamixExternalSystemConfigId", true);
  }

  public String getTeamDynamixExternalSystemConfigId() {
    return teamDynamixExternalSystemConfigId;
  }

  public void setTeamDynamixExternalSystemConfigId(String teamDynamixExternalSystemConfigId) {
    this.teamDynamixExternalSystemConfigId = teamDynamixExternalSystemConfigId;
  }

  @Override
  public void setThreadPoolSize(int threadPoolSize) {
    super.setThreadPoolSize(1);
  }

}
