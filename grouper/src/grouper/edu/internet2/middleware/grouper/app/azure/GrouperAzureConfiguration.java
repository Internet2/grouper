package edu.internet2.middleware.grouper.app.azure;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperAzureConfiguration extends GrouperProvisioningConfiguration {

  private String azureExternalSystemConfigId;
  
  private boolean allowOnlyMembersToPost;
  private boolean assignableToRole;
  private boolean hideGroupInOutlook;
  private boolean subscribeNewGroupMembers;
  private boolean welcomeEmailDisabled;
  private boolean azureGroupType;
  private boolean groupOwners;
  
  private boolean resourceProvisioningOptionsTeam;

  @Override
  public void configureAfterMetadata() {
    super.configureAfterMetadata();
    
    for (String attributeName : new String[] {"assignableToRole", "azureGroupType", 
        "groupOwners", "allowOnlyMembersToPost", "hideGroupInOutlook",
        "subscribeNewGroupMembers", "welcomeEmailDisabled", "resourceProvisioningOptionsTeam"}) {
      
      // if metadata exists
      String metadataName = "md_grouper_" + attributeName;
      if (!this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItemsByName().containsKey(metadataName)) {
        continue;
      }
      
      if (StringUtils.equals(attributeName, "assignableToRole")) {
        attributeName = "isAssignableToRole";
      }
      if (StringUtils.equals(attributeName, "azureGroupType")) {
        attributeName = "groupType";
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.getTargetGroupAttributeNameToConfig().get(attributeName);
      
      if (grouperProvisioningConfigurationAttribute != null) {
        continue;
      }

      // add an attribute
      GrouperProvisioningConfigurationAttribute nameConfigurationAttribute = new GrouperProvisioningConfigurationAttribute();
      nameConfigurationAttribute.setGrouperProvisioner(this.getGrouperProvisioner());
      nameConfigurationAttribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
      nameConfigurationAttribute.setName(attributeName);
      nameConfigurationAttribute.setConfigIndex(this.getTargetGroupAttributeNameToConfig().size());
      this.getTargetGroupAttributeNameToConfig().put(attributeName, nameConfigurationAttribute);
    }
  }
    

  @Override
  public void configureSpecificSettings() {
    
    this.azureExternalSystemConfigId = this.retrieveConfigString("azureExternalSystemConfigId", true);
    
    this.assignableToRole = GrouperUtil.booleanValue(this.retrieveConfigString("assignableToRole", false), false);
    this.azureGroupType = GrouperUtil.booleanValue(this.retrieveConfigString("azureGroupType", false), false);
    this.groupOwners = GrouperUtil.booleanValue(this.retrieveConfigString("groupOwners", false), false);
    this.allowOnlyMembersToPost = GrouperUtil.booleanValue(this.retrieveConfigString("allowOnlyMembersToPost", false), false);
    this.hideGroupInOutlook = GrouperUtil.booleanValue(this.retrieveConfigString("hideGroupInOutlook", false), false);
    this.subscribeNewGroupMembers = GrouperUtil.booleanValue(this.retrieveConfigString("subscribeNewGroupMembers", false), false);
    this.welcomeEmailDisabled = GrouperUtil.booleanValue(this.retrieveConfigString("welcomeEmailDisabled", false), false);
    this.resourceProvisioningOptionsTeam = GrouperUtil.booleanValue(this.retrieveConfigString("resourceProvisioningOptionsTeam", false), false);
  }

  public String getAzureExternalSystemConfigId() {
    return azureExternalSystemConfigId;
  }

  public void setAzureExternalSystemConfigId(String azureExternalSystemConfigId) {
    this.azureExternalSystemConfigId = azureExternalSystemConfigId;
  }
  
  
  
  public boolean isAssignableToRole() {
    return assignableToRole;
  }

  
  public void setAssignableToRole(boolean assignableToRole) {
    this.assignableToRole = assignableToRole;
  }

  public boolean isAzureGroupType() {
    return azureGroupType;
  }

  
  public void setAzureGroupType(boolean azureGroupType) {
    this.azureGroupType = azureGroupType;
  }
  
  public boolean isGroupOwners() {
    return groupOwners;
  }
  
  public void setGroupOwners(boolean groupOwners) {
    this.groupOwners = groupOwners;
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

  
  public boolean isResourceProvisioningOptionsTeam() {
    return resourceProvisioningOptionsTeam;
  }

  
  public void setResourceProvisioningOptionsTeams(boolean resourceProvisioningOptionsTeam) {
    this.resourceProvisioningOptionsTeam = resourceProvisioningOptionsTeam;
  }

  @Override
  public int getDaoSleepBeforeSelectAfterInsertMillis() {
   return 3000;
  }
  
  
}
