package edu.internet2.middleware.grouper.app.provisioning;

/**
 * 
 * @author mchyzer
 *
 */
public abstract class GrouperProvisioningConfigurationBase {

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
   * configure the provisioner, call super if subclassing
   */
  public void configureProvisioner() {
    
  }

  
}
