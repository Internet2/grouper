package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoSendMembershipChangesToTargetRequest {

  public TargetDaoSendMembershipChangesToTargetRequest() {
  }

  private List<ProvisioningMembership> targetMembershipInserts;
  private List<ProvisioningMembership> targetMembershipUpdates;
  private List<ProvisioningMembership> targetMembershipDeletes;
  
  public List<ProvisioningMembership> getTargetMembershipInserts() {
    return targetMembershipInserts;
  }
  
  public void setTargetMembershipInserts(
      List<ProvisioningMembership> targetMembershipInserts) {
    this.targetMembershipInserts = targetMembershipInserts;
  }
  
  public List<ProvisioningMembership> getTargetMembershipUpdates() {
    return targetMembershipUpdates;
  }
  
  public void setTargetMembershipUpdates(
      List<ProvisioningMembership> targetMembershipUpdates) {
    this.targetMembershipUpdates = targetMembershipUpdates;
  }
  
  public List<ProvisioningMembership> getTargetMembershipDeletes() {
    return targetMembershipDeletes;
  }
  
  public void setTargetMembershipDeletes(
      List<ProvisioningMembership> targetMembershipDeletes) {
    this.targetMembershipDeletes = targetMembershipDeletes;
  }

  public TargetDaoSendMembershipChangesToTargetRequest(
      List<ProvisioningMembership> targetMembershipInserts,
      List<ProvisioningMembership> targetMembershipUpdates,
      List<ProvisioningMembership> targetMembershipDeletes) {
    super();
    this.targetMembershipInserts = targetMembershipInserts;
    this.targetMembershipUpdates = targetMembershipUpdates;
    this.targetMembershipDeletes = targetMembershipDeletes;
  }

}
