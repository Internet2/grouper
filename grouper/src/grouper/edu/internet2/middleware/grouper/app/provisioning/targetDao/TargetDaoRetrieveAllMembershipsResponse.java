package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveAllMembershipsResponse {

  private List<ProvisioningMembership> targetProvisioningMemberships;

  
  public List<ProvisioningMembership> getTargetProvisioningMemberships() {
    return targetProvisioningMemberships;
  }

  
  public void setTargetProvisioningMemberships(
      List<ProvisioningMembership> targetProvisioningMemberships) {
    this.targetProvisioningMemberships = targetProvisioningMemberships;
  }


  public TargetDaoRetrieveAllMembershipsResponse() {
  }


  public TargetDaoRetrieveAllMembershipsResponse(
      List<ProvisioningMembership> targetProvisioningMemberships) {
    this.targetProvisioningMemberships = targetProvisioningMemberships;
  }
  
  
}
