package edu.internet2.middleware.grouper.app.adobe;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperAdobeConfiguration extends GrouperProvisioningConfiguration {

  private String adobeExternalSystemConfigId;
  
  private boolean deleteAccountWhenDeleteUser;
  
  private String orgId;
  
  private String userTypeOnCreate;

  @Override
  public void configureSpecificSettings() {
    
    this.adobeExternalSystemConfigId = this.retrieveConfigString("adobeExternalSystemConfigId", true);
    this.orgId = this.retrieveConfigString("orgId", true);
    this.userTypeOnCreate = GrouperUtil.defaultIfBlank(this.retrieveConfigString("userTypeOnCreate", false), "FederatedID"); 
    this.deleteAccountWhenDeleteUser = GrouperUtil.booleanValue(this.retrieveConfigString("deleteAccountWhenDeleteUser", false), false);
  }
  
  public String getAdobeExternalSystemConfigId() {
    return adobeExternalSystemConfigId;
  }

  public void setAdobeExternalSystemConfigId(String adobeExternalSystemConfigId) {
    this.adobeExternalSystemConfigId = adobeExternalSystemConfigId;
  }

  public boolean isDeleteAccountWhenDeleteUser() {
    return deleteAccountWhenDeleteUser;
  }

  public void setDeleteAccountWhenDeleteUser(boolean deleteAccountWhenDeleteUser) {
    this.deleteAccountWhenDeleteUser = deleteAccountWhenDeleteUser;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  
  public String getUserTypeOnCreate() {
    return userTypeOnCreate;
  }

  
  public void setUserTypeOnCreate(String userTypeOnCreate) {
    this.userTypeOnCreate = userTypeOnCreate;
  }
  
}
