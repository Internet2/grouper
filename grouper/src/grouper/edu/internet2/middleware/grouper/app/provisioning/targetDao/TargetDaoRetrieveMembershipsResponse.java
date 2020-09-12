package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsResponse {
  
  /**
   * 
   */
  private List<ProvisioningMembership> targetMemberships;

  
  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }

  
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }


  public TargetDaoRetrieveMembershipsResponse() {
  }


  public TargetDaoRetrieveMembershipsResponse(
      List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }
  
  
  
}
