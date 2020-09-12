package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsByGroupsResponse {

  public TargetDaoRetrieveMembershipsByGroupsResponse() {
    // TODO Auto-generated constructor stub
  }
  private List<ProvisioningMembership> targetMemberships;
  
  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }
  
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }

  public TargetDaoRetrieveMembershipsByGroupsResponse(
      List<ProvisioningMembership> targetMemberships) {
    super();
    this.targetMemberships = targetMemberships;
  }
  
}
