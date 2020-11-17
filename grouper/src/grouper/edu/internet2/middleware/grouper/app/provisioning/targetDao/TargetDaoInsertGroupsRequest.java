package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoInsertGroupsRequest {

  private List<ProvisioningGroup> targetGroups;

  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }


  public TargetDaoInsertGroupsRequest() {
  }


  public TargetDaoInsertGroupsRequest(List<ProvisioningGroup> targetGroupInserts) {
    this.targetGroups = targetGroupInserts;
  }
  
  
}
