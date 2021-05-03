package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoUpdateMembershipRequest {

  public TargetDaoUpdateMembershipRequest() {
    // TODO Auto-generated constructor stub
  }

  private ProvisioningMembership targetMembership;

  
  public ProvisioningMembership getTargetMembership() {
    return targetMembership;
  }

  
  public void setTargetMembership(ProvisioningMembership targetMembership) {
    this.targetMembership = targetMembership;
  }


  public TargetDaoUpdateMembershipRequest(ProvisioningMembership targetMembership) {
    super();
    this.targetMembership = targetMembership;
  }
  
}
