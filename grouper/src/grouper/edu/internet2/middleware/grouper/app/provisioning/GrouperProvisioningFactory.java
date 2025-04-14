package edu.internet2.middleware.grouper.app.provisioning;


/**
 * instead of being a provisioner, this will generate a provisioner
 */
public interface GrouperProvisioningFactory {

  /**
   * generate a grouper provisioner object dynamically given the configId
   * @param configId
   * @return the provisioner instance
   */
  public GrouperProvisioner generateGrouperProvisioner(String configId);
  
}
