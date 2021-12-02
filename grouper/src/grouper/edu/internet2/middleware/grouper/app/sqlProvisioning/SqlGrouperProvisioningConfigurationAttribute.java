package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;

public class SqlGrouperProvisioningConfigurationAttribute extends GrouperProvisioningConfigurationAttribute {
  
  private String storageType;

  
  public String getStorageType() {
    return storageType;
  }

  
  public void setStorageType(String storageType) {
    this.storageType = storageType;
  }
  
}
