package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoReplaceGroupMembershipsRequest {

  private ProvisioningGroup targetGroup;
  
  public ProvisioningGroup getTargetGroup() {
    return targetGroup;
  }

  public void setTargetGroup(ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }

  private List<ProvisioningMembership> targetMemberships;

  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }

  public void setTargetMemberships(
      List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }

  public TargetDaoReplaceGroupMembershipsRequest() {
    super();
  }

  public TargetDaoReplaceGroupMembershipsRequest(ProvisioningGroup targetGroup, 
      List<ProvisioningMembership> targetMemberships) {
    super();
    this.targetGroup = targetGroup;
    this.targetMemberships = targetMemberships;
  }

}
