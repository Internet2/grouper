package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsByEntitiesResponse {

  public TargetDaoRetrieveMembershipsByEntitiesResponse() {
  }

  private List<ProvisioningMembership> targetMemberships;

  
  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }

  
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }


  public TargetDaoRetrieveMembershipsByEntitiesResponse(
      List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }
  
}
