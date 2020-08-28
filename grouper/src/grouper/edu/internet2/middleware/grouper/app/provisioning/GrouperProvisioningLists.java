package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

public class GrouperProvisioningLists {
  
  private List<ProvisioningGroup> provisioningGroups;
  private List<ProvisioningEntity> provisioningEntities;
  private List<ProvisioningMembership> provisioningMemberships;
  
  public List<ProvisioningGroup> getProvisioningGroups() {
    return provisioningGroups;
  }
  
  public void setProvisioningGroups(List<ProvisioningGroup> provisioningGroups) {
    this.provisioningGroups = provisioningGroups;
  }
  
  public List<ProvisioningEntity> getProvisioningEntities() {
    return provisioningEntities;
  }
  
  public void setProvisioningEntities(List<ProvisioningEntity> provisioningEntities) {
    this.provisioningEntities = provisioningEntities;
  }
  
  public List<ProvisioningMembership> getProvisioningMemberships() {
    return provisioningMemberships;
  }
  
  public void setProvisioningMemberships(
      List<ProvisioningMembership> provisioningMemberships) {
    this.provisioningMemberships = provisioningMemberships;
  }
  

}
