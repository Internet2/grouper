package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

public class GrouperProvisioningLogicAlgorithmGroupsOnly extends GrouperProvisioningLogicAlgorithmBase {

  @Override
  public void provision(GrouperProvisioningLogic grouperProvisioningLogic) {
  
    
    
  }
  
  @Override
  public void retrieveDataFromTarget() {
    Map<String, ProvisioningGroup> targetGroups = this.getGrouperProvisioner().retrieveTargetDao().retrieveAllGroups();
    this.getGrouperProvisioner().getGrouperProvisioningData().setGrouperTargetGroups(targetGroups);
  }

}
