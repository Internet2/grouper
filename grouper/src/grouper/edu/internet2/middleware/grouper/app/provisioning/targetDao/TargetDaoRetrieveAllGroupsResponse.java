package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveAllGroupsResponse {

  public TargetDaoRetrieveAllGroupsResponse() {
  }

  private List<ProvisioningGroup> targetProvisioningGroups;

  
  public List<ProvisioningGroup> getTargetProvisioningGroups() {
    return targetProvisioningGroups;
  }

  
  public void setTargetProvisioningGroups(List<ProvisioningGroup> targetGroups) {
    this.targetProvisioningGroups = targetGroups;
  }


  public TargetDaoRetrieveAllGroupsResponse(List<ProvisioningGroup> targetGroups) {
    super();
    this.targetProvisioningGroups = targetGroups;
  }
  
}
