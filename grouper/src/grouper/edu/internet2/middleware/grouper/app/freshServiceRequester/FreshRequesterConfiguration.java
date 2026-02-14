package edu.internet2.middleware.grouper.app.freshServiceRequester;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class FreshRequesterConfiguration extends GrouperProvisioningConfiguration {
  
  private String freshserviceExternalSystemConfigId;

  @Override
  public void configureSpecificSettings() {
    this.freshserviceExternalSystemConfigId = this.retrieveConfigString("freshserviceExternalSystemConfigId", true);
  }
  
  /**
   * Get the FreshService external system config ID
   * @return the Freshservice external system config ID
   */
  public String getFreshserviceExternalSystemConfigId() {
    return freshserviceExternalSystemConfigId;
  }

  /**
   * Set the FreshService external system config ID
   * @param freshserviceExternalSystemConfigId the external system config ID to set
   */
  public void setFreshserviceExternalSystemConfigId(String freshserviceExternalSystemConfigId) {
    this.freshserviceExternalSystemConfigId = freshserviceExternalSystemConfigId;
  }

}
