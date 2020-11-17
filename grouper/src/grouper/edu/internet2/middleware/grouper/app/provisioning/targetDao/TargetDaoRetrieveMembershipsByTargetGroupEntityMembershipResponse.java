package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipResponse {

  public TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipResponse() {
    // TODO Auto-generated constructor stub
  }
  private List<ProvisioningMembership> targetMemberships;
  
  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }
  
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }

  public TargetDaoRetrieveMembershipsByTargetGroupEntityMembershipResponse(
      List<ProvisioningMembership> targetMemberships) {
    super();
    this.targetMemberships = targetMemberships;
  }

  
}
