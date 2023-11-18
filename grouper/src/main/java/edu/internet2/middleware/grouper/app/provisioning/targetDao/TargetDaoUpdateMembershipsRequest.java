package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoUpdateMembershipsRequest {

  public TargetDaoUpdateMembershipsRequest() {
  }

  private List<ProvisioningMembership> targetMemberships;

  
  public TargetDaoUpdateMembershipsRequest(
      List<ProvisioningMembership> targetMemberships) {
    super();
    this.targetMemberships = targetMemberships;
  }


  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }

  
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }
}
