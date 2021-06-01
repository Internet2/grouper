package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveMembershipsByGroupRequest {

  private ProvisioningGroup targetGroup;

  
  public ProvisioningGroup getTargetGroup() {
    return targetGroup;
  }

  
  public void setTargetGroup(ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }


  public TargetDaoRetrieveMembershipsByGroupRequest() {
  }


  public TargetDaoRetrieveMembershipsByGroupRequest(
      ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }
  
}
