package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoUpdateGroupsRequest {

  public TargetDaoUpdateGroupsRequest() {
  }

  private List<ProvisioningGroup> targetGroups;

  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }


  public TargetDaoUpdateGroupsRequest(List<ProvisioningGroup> targetGroups) {
    super();
    this.targetGroups = targetGroups;
  }
  
}
