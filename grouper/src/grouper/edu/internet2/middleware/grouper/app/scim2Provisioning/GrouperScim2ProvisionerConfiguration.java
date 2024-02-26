package edu.internet2.middleware.grouper.app.scim2Provisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperScim2ProvisionerConfiguration extends GrouperProvisioningConfiguration {

  private String bearerTokenExternalSystemConfigId;
  
  private String scimType;
  
  private String acceptHeader;
  
  private boolean disableGroupsInsteadOfDelete = false;
  
  private boolean disableEntitiesInsteadOfDelete = false;

  
  public boolean isDisableGroupsInsteadOfDelete() {
    return disableGroupsInsteadOfDelete;
  }

  
  public void setDisableGroupsInsteadOfDelete(boolean disableGroupsInsteadOfDelete) {
    this.disableGroupsInsteadOfDelete = disableGroupsInsteadOfDelete;
  }

  
  public boolean isDisableEntitiesInsteadOfDelete() {
    return disableEntitiesInsteadOfDelete;
  }

  
  public void setDisableEntitiesInsteadOfDelete(boolean disableEntitiesInsteadOfDelete) {
    this.disableEntitiesInsteadOfDelete = disableEntitiesInsteadOfDelete;
  }

  @Override
  public void configureSpecificSettings() {
    
    this.bearerTokenExternalSystemConfigId = this.retrieveConfigString("bearerTokenExternalSystemConfigId", true);
    this.scimType = this.retrieveConfigString("scimType", true);
    this.acceptHeader = this.retrieveConfigString("acceptHeader", false);
    this.disableGroupsInsteadOfDelete = GrouperUtil.booleanValue(this.retrieveConfigBoolean("disableGroupsInsteadOfDelete", false), false);
    this.disableEntitiesInsteadOfDelete = GrouperUtil.booleanValue(this.retrieveConfigBoolean("disableEntitiesInsteadOfDelete", false), false);
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