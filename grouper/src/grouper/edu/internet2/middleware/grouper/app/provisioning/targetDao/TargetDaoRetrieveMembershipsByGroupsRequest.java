package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveMembershipsByGroupsRequest {
  private List<ProvisioningGroup> targetGroups;

  
  public TargetDaoRetrieveMembershipsByGroupsRequest(
      List<ProvisioningGroup> targetGroups) {
    super();
    this.targetGroups = targetGroups;
  }


  public TargetDaoRetrieveMembershipsByGroupsRequest() {
    super();
    // TODO Auto-generated constructor stub
  }


  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }
}
