package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoDeleteMembershipRequest {
  private ProvisioningMembership targetMembership;

  
  public ProvisioningMembership getTargetMembership() {
    return targetMembership;
  }

  
  public void setTargetMembership(ProvisioningMembership targetMembership) {
    this.targetMembership = targetMembership;
  }


  public TargetDaoDeleteMembershipRequest() {
  }


  public TargetDaoDeleteMembershipRequest(ProvisioningMembership targetMembership) {
    this.targetMembership = targetMembership;
  }
  
}
