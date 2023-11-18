package edu.internet2.middleware.grouper.app.genericProvisioner;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class GrouperGenericConfiguration extends GrouperProvisioningConfiguration {

  private String genericProvisionerDaoClassName;
  
    

  @Override
  public void configureSpecificSettings() {
    
    this.genericProvisionerDaoClassName = this.retrieveConfigString("genericProvisionerDaoClassName", true);
    
  }
  
  public String getGenericProvisionerDaoClassName() {
    return genericProvisionerDaoClassName;
  }
  
  public void setGenericProvisionerDaoClassName(String genericProvisionerDaoClassName) {
    this.genericProvisionerDaoClassName = genericProvisionerDaoClassName;
  }

  
}
