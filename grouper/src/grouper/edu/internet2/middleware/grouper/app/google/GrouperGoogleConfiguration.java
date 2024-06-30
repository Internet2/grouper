package edu.internet2.middleware.grouper.app.google;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeDbCache;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class GrouperGoogleConfiguration extends GrouperProvisioningConfiguration {
  
  private String googleExternalSystemConfigId;
  
  private boolean whoCanAdd;
  private boolean whoCanJoin;
  private boolean whoCanViewMembership;
  private boolean whoCanViewGroup;
  private boolean whoCanInvite;
  private boolean allowExternalMembers;
  private boolean whoCanPostMessage;
  private boolean allowWebPosting;
  
  private String defaultMessageDenyNotificationText;
  private String handleDeletedGroup;
  private String messageModerationLevel;
  private String replyTo;
  private boolean sendMessageDenyNotification;
  private String spamModerationLevel;
  
  private boolean searchedForEmailCache; 
  private GrouperProvisioningConfigurationAttributeDbCache emailCacheBucket; 
  
  private boolean searchedForIdCache; 
  private GrouperProvisioningConfigurationAttributeDbCache entityIdCacheBucket; 
  
  
  public boolean isWhoCanAdd() {
    return whoCanAdd;
  }

  
  public void setWhoCanAdd(boolean whoCanAdd) {
    this.whoCanAdd = whoCanAdd;
  }

  
  public boolean isWhoCanJoin() {
    return whoCanJoin;
  }

  
  public void setWhoCanJoin(boolean whoCanJoin) {
    this.whoCanJoin = whoCanJoin;
  }

  
  public boolean isWhoCanViewMembership() {
    return whoCanViewMembership;
  }

  
  public void setWhoCanViewMembership(boolean whoCanViewMembership) {
    this.whoCanViewMembership = whoCanViewMembership;
  }

  
  public boolean isWhoCanViewGroup() {
    return whoCanViewGroup;
  }

  
  public void setWhoCanViewGroup(boolean whoCanViewGroup) {
    this.whoCanViewGroup = whoCanViewGroup;
  }

  
  public boolean isWhoCanInvite() {
    return whoCanInvite;
  }

  
  public void setWhoCanInvite(boolean whoCanInvite) {
    this.whoCanInvite = whoCanInvite;
  }

  
  public boolean isAllowExternalMembers() {
    return allowExternalMembers;
  }

  
  public void setAllowExternalMembers(boolean allowExternalMembers) {
    this.allowExternalMembers = allowExternalMembers;
  }

  
  public boolean isWhoCanPostMessage() {
    return whoCanPostMessage;
  }

  
  public void setWhoCanPostMessage(boolean whoCanPostMessage) {
    this.whoCanPostMessage = whoCanPostMessage;
  }

  
  public boolean isAllowWebPosting() {
    return allowWebPosting;
  }

  
  public void setAllowWebPosting(boolean allowWebPosting) {
    this.allowWebPosting = allowWebPosting;
  }


  public String getGoogleExternalSystemConfigId() {
    return googleExternalSystemConfigId;
  }
  
  public void setGoogleExternalSystemConfigId(String googleExternalSystemConfigId) {
    this.googleExternalSystemConfigId = googleExternalSystemConfigId;
  }
  
  

  
  public String getDefaultMessageDenyNotificationText() {
    return defaultMessageDenyNotificationText;
  }


  
  public void setDefaultMessageDenyNotificationText(String defaultMessageDenyNotificationText) {
    this.defaultMessageDenyNotificationText = defaultMessageDenyNotificationText;
  }
  

  
  public String getHandleDeletedGroup() {
    return handleDeletedGroup;
  }


  
  public void setHandleDeletedGroup(String handleDeletedGroup) {
    this.handleDeletedGroup = handleDeletedGroup;
  }
  

  
  public String getMessageModerationLevel() {
    return messageModerationLevel;
  }


  
  public void setMessageModerationLevel(String messageModerationLevel) {
    this.messageModerationLevel = messageModerationLevel;
  }
  
  
  public String getReplyTo() {
    return replyTo;
  }


  
  public void setReplyTo(String replyTo) {
    this.replyTo = replyTo;
  }
  
  
  public boolean isSendMessageDenyNotification() {
    return sendMessageDenyNotification;
  }


  
  public void setSendMessageDenyNotification(boolean sendMessageDenyNotification) {
    this.sendMessageDenyNotification = sendMessageDenyNotification;
  }
  
  


  
  public String getSpamModerationLevel() {
    return spamModerationLevel;
  }


  
  public void setSpamModerationLevel(String spamModerationLevel) {
    this.spamModerationLevel = spamModerationLevel;
  }


  @Override
  public void configureSpecificSettings() {
    
    this.googleExternalSystemConfigId = this.retrieveConfigString("googleExternalSystemConfigId", true);
    this.whoCanAdd = GrouperUtil.booleanValue(this.retrieveConfigString("whoCanAdd", false), false);
    this.whoCanJoin = GrouperUtil.booleanValue(this.retrieveConfigString("whoCanJoin", false), false);
    this.whoCanViewMembership = GrouperUtil.booleanValue(this.retrieveConfigString("whoCanViewMembership", false), false);
    this.whoCanViewGroup = GrouperUtil.booleanValue(this.retrieveConfigString("whoCanViewGroup", false), false);
    this.whoCanInvite = GrouperUtil.booleanValue(this.retrieveConfigString("whoCanInvite", false), false);
    this.allowExternalMembers = GrouperUtil.booleanValue(this.retrieveConfigString("allowExternalMembers", false), false);
    this.whoCanPostMessage = GrouperUtil.booleanValue(this.retrieveConfigString("whoCanPostMessage", false), false);
    this.allowWebPosting = GrouperUtil.booleanValue(this.retrieveConfigString("allowWebPosting", false), false);
    this.defaultMessageDenyNotificationText = this.retrieveConfigString("defaultMessageDenyNotificationText", false);
    this.handleDeletedGroup = this.retrieveConfigString("handleDeletedGroup", false);
    this.messageModerationLevel = this.retrieveConfigString("messageModerationLevel", false);
    this.replyTo = this.retrieveConfigString("replyTo", false);
    this.spamModerationLevel = this.retrieveConfigString("spamModerationLevel", false);
    this.sendMessageDenyNotification = GrouperUtil.booleanValue(this.retrieveConfigString("sendMessageDenyNotification", false), false);
    
  }
  
  @Override
  public int getDaoSleepBeforeSelectAfterInsertMillis() {
    return GrouperUtil.intValue(this.retrieveConfigInt("sleepBeforeSelectAfterInsertMillis", false), 15000);
  }
  
  @Override
  public void configureAfterMetadata() {
    super.configureAfterMetadata();
    
    for (String attributeName : new String[] {"whoCanAdd", "whoCanJoin",
        "whoCanViewMembership", "whoCanViewGroup", "whoCanInvite",
        "allowExternalMembers", "whoCanPostMessage", "allowWebPosting"}) {
      
      // if metadata exists
      String metadataName = "md_grouper_" + attributeName;
      if (!this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItemsByName().containsKey(metadataName)) {
        continue;
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


  public GrouperProvisioningConfigurationAttributeDbCache getEmailCacheBucket() {
    if (searchedForEmailCache) {      
      return emailCacheBucket;
    }
    for (GrouperProvisioningConfigurationAttributeDbCache cache : GrouperUtil.nonNull(this.getEntityAttributeDbCaches(), GrouperProvisioningConfigurationAttributeDbCache.class)) {
      if (cache == null) {
        continue;
      }
      String attributeName = cache.getAttributeName();
      if (StringUtils.equals(attributeName, "email")) {
        emailCacheBucket = cache;
        break;
      }
    }
    searchedForEmailCache = true;
    return emailCacheBucket;
  }

  public GrouperProvisioningConfigurationAttributeDbCache getEntityIdCacheBucket() {
    if (searchedForIdCache) {      
      return entityIdCacheBucket;
    }
    for (GrouperProvisioningConfigurationAttributeDbCache cache : GrouperUtil.nonNull(this.getEntityAttributeDbCaches(), GrouperProvisioningConfigurationAttributeDbCache.class)) {
      if (cache == null) {
        continue;
      }
      String attributeName = cache.getAttributeName();
      if (StringUtils.equals(attributeName, "id")) {
        entityIdCacheBucket = cache;
        break;
      }
    }
    searchedForIdCache = true;
    return entityIdCacheBucket;
  }

  
}
