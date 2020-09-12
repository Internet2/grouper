package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoInsertMembershipsRequest {

  private List<ProvisioningMembership> targetMembershipInserts;

  
  public List<ProvisioningMembership> getTargetMembershipInserts() {
    return targetMembershipInserts;
  }

  
  public void setTargetMembershipInserts(
      List<ProvisioningMembership> targetMembershipInserts) {
    this.targetMembershipInserts = targetMembershipInserts;
  }


  public TargetDaoInsertMembershipsRequest() {
    super();
  }


  public TargetDaoInsertMembershipsRequest(
      List<ProvisioningMembership> targetMembershipInserts) {
    super();
    this.targetMembershipInserts = targetMembershipInserts;
  }
  
  
}
