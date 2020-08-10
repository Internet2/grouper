package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

/**
 * @author shilen
 */
public class GrouperProvisioningTranslatorBase {

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
  
  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public Map<String, TargetGroup> translateToTarget(Map<String, TargetGroup> targetGroups, Map<String, TargetEntity> targetEntities, Map<String, TargetMembership> targetMemberships) {
    throw new UnsupportedOperationException();
  }
}