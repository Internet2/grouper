package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

public abstract class GrouperProvisionerTargetDaoBase {

  
  public Map<String, ProvisioningGroup> retrieveAllGroups() {
    throw new UnsupportedOperationException();
  }
  
  /**
   * @param targetGroup
   * @return true if created, false if existed and updated
   */
  public boolean createGroup(ProvisioningGroup targetGroup) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * @param grouperTranslatedTargetGroup
   * @param actualTargetGroup
   * @return true if updated
   */
  public boolean updateGroupIfNeeded(ProvisioningGroup grouperTranslatedTargetGroup, ProvisioningGroup actualTargetGroup) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * @paProvisioningGrouproup
   */
  public void deleteGroup(ProvisioningGroup targetGroup) {
    throw new UnsupportedOperationException();
  }

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }
  
  
  
}
