package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class GrouperDigitalMarketplaceConfiguration extends GrouperProvisioningConfiguration {

  private String digitalMarketplaceExternalSystemConfigId;

  @Override
  public void configureSpecificSettings() {
    this.digitalMarketplaceExternalSystemConfigId = this.retrieveConfigString("digitalMarketplaceExternalSystemConfigId", true);
  }
  
  public String getDigitalMarketplaceExternalSystemConfigId() {
    return digitalMarketplaceExternalSystemConfigId;
  }

  public void setDigitalMarketplaceExternalSystemConfigId(
      String digitalMarketplaceExternalSystemConfigId) {
    this.digitalMarketplaceExternalSystemConfigId = digitalMarketplaceExternalSystemConfigId;
  }
  
}
