package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipRequest {

  public TargetDaoRetrieveMembershipRequest() {
  }
  private ProvisioningMembership targetMembership;
  
  public ProvisioningMembership getTargetMembership() {
    return targetMembership;
  }
  
  public void setTargetMembership(ProvisioningMembership targetMembership) {
    this.targetMembership = targetMembership;
  }

  public TargetDaoRetrieveMembershipRequest(ProvisioningMembership targetMembership) {
    this.targetMembership = targetMembership;
  }
  
  
}
