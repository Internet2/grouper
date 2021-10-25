package edu.internet2.middleware.grouper.app.azure;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperAzureConfiguration extends GrouperProvisioningConfigurationBase {

  private String azureExternalSystemConfigId;
  
  private boolean allowOnlyMembersToPost;
  private boolean hideGroupInOutlook;
  private boolean subscribeNewGroupMembers;
  private boolean welcomeEmailDisabled;
  
  private boolean resourceProvisioningOptionsTeams;

  @Override
  public void configureSpecificSettings() {
    
    this.azureExternalSystemConfigId = this.retrieveConfigString("azureExternalSystemConfigId", true);
    
    this.allowOnlyMembersToPost = GrouperUtil.booleanValue(this.retrieveConfigString("allowOnlyMembersToPost", false), false);
    this.hideGroupInOutlook = GrouperUtil.booleanValue(this.retrieveConfigString("hideGroupInOutlook", false), false);
    this.subscribeNewGroupMembers = GrouperUtil.booleanValue(this.retrieveConfigString("subscribeNewGroupMembers", false), false);
    this.welcomeEmailDisabled = GrouperUtil.booleanValue(this.retrieveConfigString("welcomeEmailDisabled", false), false);
    this.resourceProvisioningOptionsTeams = GrouperUtil.booleanValue(this.retrieveConfigString("resourceProvisioningOptionsTeams", false), false);
  }

  public String getAzureExternalSystemConfigId() {
    return azureExternalSystemConfigId;
  }

  public void setAzureExternalSystemConfigId(String azureExternalSystemConfigId) {
    this.azureExternalSystemConfigId = azureExternalSystemConfigId;
  }

  
  public boolean isAllowOnlyMembersToPost() {
    return allowOnlyMembersToPost;
  }

  
  public void setAllowOnlyMembersToPost(boolean allowOnlyMembersToPost) {
    this.allowOnlyMembersToPost = allowOnlyMembersToPost;
  }

  public boolean isHideGroupInOutlook() {
    return hideGroupInOutlook;
  }

  
  public void setHideGroupInOutlook(boolean hideGroupInOutlook) {
    this.hideGroupInOutlook = hideGroupInOutlook;
  }

  public boolean isSubscribeNewGroupMembers() {
    return subscribeNewGroupMembers;
  }

  
  public void setSubscribeNewGroupMembers(boolean subscribeNewGroupMembers) {
    this.subscribeNewGroupMembers = subscribeNewGroupMembers;
  }

  
  public boolean isWelcomeEmailDisabled() {
    return welcomeEmailDisabled;
  }

  
  public void setWelcomeEmailDisabled(boolean welcomeEmailDisabled) {
    this.welcomeEmailDisabled = welcomeEmailDisabled;
  }

  
  public boolean isResourceProvisioningOptionsTeams() {
    return resourceProvisioningOptionsTeams;
  }

  
  public void setResourceProvisioningOptionsTeams(boolean resourceProvisioningOptionsTeams) {
    this.resourceProvisioningOptionsTeams = resourceProvisioningOptionsTeams;
  }

  
}
