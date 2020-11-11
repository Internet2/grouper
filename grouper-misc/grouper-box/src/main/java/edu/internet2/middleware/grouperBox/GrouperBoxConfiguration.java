package edu.internet2.middleware.grouperBox;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperBoxConfiguration extends GrouperProvisioningConfigurationBase {

  private String boxExternalSystemConfigId;
  private String invitabilityLevel;
  private String memberViewabilityLevel;
  
  @Override
  public void configureSpecificSettings() {
    
    this.boxExternalSystemConfigId = this.retrieveConfigString("boxExternalSystemConfigId", true);
    this.invitabilityLevel = GrouperUtil.defaultIfNull(this.retrieveConfigString("invitabilityLevel", false), "admins_and_members");
    this.memberViewabilityLevel = GrouperUtil.defaultIfNull(this.retrieveConfigString("memberViewabilityLevel", false), "admins_and_members");
  }

  
  
  public String getBoxExternalSystemConfigId() {
    return boxExternalSystemConfigId;
  }


  
  public void setBoxExternalSystemConfigId(String boxExternalSystemConfigId) {
    this.boxExternalSystemConfigId = boxExternalSystemConfigId;
  }


  public String getInvitabilityLevel() {
    return invitabilityLevel;
  }

  
  public void setInvitabilityLevel(String invitabilityLevel) {
    this.invitabilityLevel = invitabilityLevel;
  }

  
  public String getMemberViewabilityLevel() {
    return memberViewabilityLevel;
  }

  
  public void setMemberViewabilityLevel(String memberViewabilityLevel) {
    this.memberViewabilityLevel = memberViewabilityLevel;
  }
}
