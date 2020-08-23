package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;

public class GrouperProvisioningData {
  
  private GrouperProvisioner grouperProvisioner = null;
  
  private Map<String, ProvisioningGroup> grouperTargetGroups;
  private Map<String, TargetEntity> grouperTargetEntities;
  private Map<String, TargetMembership> grouperTargetMemberships;
  
  
  private Map<String, ProvisioningGroup> grouperCommonGroups;
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  
  public Map<String, TargetEntity> getGrouperTargetEntities() {
    return grouperTargetEntities;
  }
  
  public void setGrouperTargetEntities(Map<String, TargetEntity> grouperTargetEntities) {
    this.grouperTargetEntities = grouperTargetEntities;
  }
  
  public Map<String, TargetMembership> getGrouperTargetMemberships() {
    return grouperTargetMemberships;
  }


  
  public Map<String, ProvisioningGroup> getGrouperTargetGroups() {
    return grouperTargetGroups;
  }


  
  public void setGrouperTargetGroups(Map<String, ProvisioningGroup> grouperTargetGroups) {
    this.grouperTargetGroups = grouperTargetGroups;
  }


  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  
  public void setGrouperTargetMemberships(
      Map<String, TargetMembership> grouperTargetMemberships) {
    this.grouperTargetMemberships = grouperTargetMemberships;
  }


  
  public Map<String, ProvisioningGroup> getGrouperCommonGroups() {
    return grouperCommonGroups;
  }


  
  public void setGrouperCommonGroups(Map<String, ProvisioningGroup> grouperCommonGroups) {
    this.grouperCommonGroups = grouperCommonGroups;
  }
  
 
  

}
