package edu.internet2.middleware.grouper.app.provisioning;


public abstract class GrouperProvisioningLogicAlgorithmBase {
  
  private GrouperProvisioner grouperProvisioner;
  
  public abstract void provision(GrouperProvisioningLogic grouperProvisioningLogic);

  public abstract void retrieveDataFromTarget();
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }
  
  public void syncGrouperTranslatedGroupsToTarget() {
    
    GrouperProvisioningData grouperProvisioningData = this.getGrouperProvisioner().getGrouperProvisioningData();
    
    for (String key : grouperProvisioningData.getGrouperTargetGroups().keySet()) {
      ProvisioningGroup actualTargetGroup = grouperProvisioningData.getGrouperTargetGroups().get(key);
      if (!grouperProvisioningData.getGrouperCommonGroups().containsKey(key)) {
        this.grouperProvisioner.retrieveTargetDao().deleteGroup(actualTargetGroup);
      }
    }
    
    for (String key : grouperProvisioningData.getGrouperCommonGroups().keySet()) {
      ProvisioningGroup targetGroup = grouperProvisioningData.getGrouperCommonGroups().get(key);
      if (!grouperProvisioningData.getGrouperTargetGroups().containsKey(key)) {
        this.grouperProvisioner.retrieveTargetDao().createGroup(targetGroup);
      } else {
        this.grouperProvisioner.retrieveTargetDao().updateGroupIfNeeded(targetGroup, grouperProvisioningData.getGrouperTargetGroups().get(key));
      }
    }
    
    
  }

  public void syncGrouperTranslatedMembershipsToTarget() { }
  

}
