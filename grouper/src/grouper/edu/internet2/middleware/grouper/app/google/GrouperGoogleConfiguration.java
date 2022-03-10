package edu.internet2.middleware.grouper.app.google;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
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
    
  }
  
}
