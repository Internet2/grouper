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

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroupInserts) {
    this.targetGroups = targetGroupInserts;
  }


  public TargetDaoUpdateGroupsRequest(List<ProvisioningGroup> targetGroupInserts) {
    super();
    this.targetGroups = targetGroupInserts;
  }
  
}
