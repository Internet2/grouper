package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

/**
 * 
 * @author mchyzer-local
 *
 */
public class TargetDaoDeleteGroupsRequest {
  private List<ProvisioningGroup> targetGroups;

  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }


  public TargetDaoDeleteGroupsRequest() {
  }


  public TargetDaoDeleteGroupsRequest(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }
  
  
}
