package edu.internet2.middleware.grouper.app.genericProvisioner;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class GrouperGenericConfiguration extends GrouperProvisioningConfiguration {

  private String genericProvisionerDaoClassName;
  private String genericLoaderClassName;
  
  public String getGenericLoaderClassName() {
    return genericLoaderClassName;
  }
  
  public void setGenericLoaderClassName(String genericLoaderClassName) {
    this.genericLoaderClassName = genericLoaderClassName;
  }

  @Override
  public void configureSpecificSettings() {
    
    this.genericProvisionerDaoClassName = this.retrieveConfigString("genericProvisionerDaoClassName", true);
    
    this.genericLoaderClassName = this.retrieveConfigString("genericLoaderClassName", false);
  }
  
  public String getGenericProvisionerDaoClassName() {
    return genericProvisionerDaoClassName;
  }
  
  public void setGenericProvisionerDaoClassName(String genericProvisionerDaoClassName) {
    this.genericProvisionerDaoClassName = genericProvisionerDaoClassName;
  }

  
}
