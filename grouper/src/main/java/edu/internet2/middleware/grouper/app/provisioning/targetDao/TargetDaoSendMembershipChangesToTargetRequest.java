package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoSendMembershipChangesToTargetRequest {

  public TargetDaoSendMembershipChangesToTargetRequest() {
  }

  private List<ProvisioningMembership> targetMembershipInserts;
  private List<ProvisioningMembership> targetMembershipUpdates;
  private List<ProvisioningMembership> targetMembershipDeletes;
  private Map<ProvisioningGroup, List<ProvisioningMembership>> targetMembershipReplaces;
  
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
  
  
  
  public Map<ProvisioningGroup, List<ProvisioningMembership>> getTargetMembershipReplaces() {
    return targetMembershipReplaces;
  }

  
  public void setTargetMembershipReplaces(
      Map<ProvisioningGroup, List<ProvisioningMembership>> targetMembershipReplaces) {
    this.targetMembershipReplaces = targetMembershipReplaces;
  }

  public TargetDaoSendMembershipChangesToTargetRequest(
      List<ProvisioningMembership> targetMembershipInserts,
      List<ProvisioningMembership> targetMembershipUpdates,
      List<ProvisioningMembership> targetMembershipDeletes,
      Map<ProvisioningGroup, List<ProvisioningMembership>> targetMembershipReplaces) {
    super();
    this.targetMembershipInserts = targetMembershipInserts;
    this.targetMembershipUpdates = targetMembershipUpdates;
    this.targetMembershipDeletes = targetMembershipDeletes;
    this.targetMembershipReplaces = targetMembershipReplaces;
  }

}
