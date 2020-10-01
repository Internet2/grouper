package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningLists {
  
  private List<ProvisioningGroup> provisioningGroups;
  private List<ProvisioningEntity> provisioningEntities;
  private List<ProvisioningMembership> provisioningMemberships;

  public boolean wasWorkDone() {

    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(this.provisioningGroups)) {
      if (GrouperUtil.booleanValue(provisioningGroup.getProvisioned(), false) && provisioningGroup.getException() == null) {
        return true;
      }
    }
    
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(this.provisioningEntities)) {
      if (GrouperUtil.booleanValue(provisioningEntity.getProvisioned(), false) && provisioningEntity.getException() == null) {
        return true;
      }
    }
    
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(this.provisioningMemberships)) {
      if (GrouperUtil.booleanValue(provisioningMembership.getProvisioned(), false) && provisioningMembership.getException() == null) {
        return true;
      }
    }
    
    return false;
  }

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
