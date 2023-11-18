package edu.internet2.middleware.grouper.app.scim2Provisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;

public class GrouperScim2ProvisionerConfiguration extends GrouperProvisioningConfiguration {

  private String bearerTokenExternalSystemConfigId;
  
  private String scimType;
  
  private String acceptHeader;

  @Override
  public void configureSpecificSettings() {
    
    this.bearerTokenExternalSystemConfigId = this.retrieveConfigString("bearerTokenExternalSystemConfigId", true);
    this.scimType = this.retrieveConfigString("scimType", true);
    this.acceptHeader = this.retrieveConfigString("acceptHeader", false);
  }

  public String getBearerTokenExternalSystemConfigId() {
    return bearerTokenExternalSystemConfigId;
  }

  public void setBearerTokenExternalSystemConfigId(String azureExternalSystemConfigId) {
    this.bearerTokenExternalSystemConfigId = azureExternalSystemConfigId;
  }

  
  public String getScimType() {
    return scimType;
  }

  
  public void setScimType(String scimType) {
    this.scimType = scimType;
  }

  
  public String getAcceptHeader() {
    return acceptHeader;
  }

  
  public void setAcceptHeader(String acceptHeader) {
    this.acceptHeader = acceptHeader;
  }
  
  
}