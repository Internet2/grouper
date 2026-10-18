package edu.internet2.middleware.grouper.app.duo.role;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class GrouperDuoRoleConfiguration extends GrouperProvisioningConfiguration {

  private String duoExternalSystemConfigId;
  
  /**
   * JSON of role id or name to priority, higher wins when an administrator is in several role groups,
   * e.g. {"Tier 2 Support": 350}.  Built-in roles default to 100 (read_only) to 800 (owner)
   */
  private String rolePriority;
  
  @Override
  public void configureSpecificSettings() {
    this.duoExternalSystemConfigId = this.retrieveConfigString("duoExternalSystemConfigId", true);
    this.rolePriority = this.retrieveConfigString("rolePriority", false);
  }

  /**
   * @return JSON of role id or name to priority
   */
  public String getRolePriority() {
    return rolePriority;
  }

  
  public String getDuoExternalSystemConfigId() {
    return duoExternalSystemConfigId;
  }

}
