package edu.internet2.middleware.grouper.app.messagingProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;

public class GrouperMessagingConfiguration extends GrouperProvisioningConfigurationBase {

  private String messagingExternalSystemConfigId;

  @Override
  public void configureSpecificSettings() {
    
    this.messagingExternalSystemConfigId = this.retrieveConfigString("messagingExternalSystemConfigId", true);
  }

  public String getMessagingExternalSystemConfigId() {
    return messagingExternalSystemConfigId;
  }

  
  public void setMessagingExternalSystemConfigId(String messagingExternalSystemConfigId) {
    this.messagingExternalSystemConfigId = messagingExternalSystemConfigId;
  }

  
}
