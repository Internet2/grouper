package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoDeleteMembershipsRequest {
  private List<ProvisioningMembership> targetMembershipDeletes;

  
  public List<ProvisioningMembership> getTargetMembershipDeletes() {
    return targetMembershipDeletes;
  }

  
  public void setTargetMembershipDeletes(
      List<ProvisioningMembership> targetMembershipDeletes) {
    this.targetMembershipDeletes = targetMembershipDeletes;
  }


  public TargetDaoDeleteMembershipsRequest() {
  }


  public TargetDaoDeleteMembershipsRequest(
      List<ProvisioningMembership> targetMembershipDeletes) {
    this.targetMembershipDeletes = targetMembershipDeletes;
  }
  
}
