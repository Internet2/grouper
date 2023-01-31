package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveIncrementalDataResponse {
  
  private List<ProvisioningEntity> provisioningEntities;
  private List<ProvisioningGroup> provisioningGroups;
  
  /**
   * provisioning memberships from target
   */
  private List<ProvisioningMembership> provisioningMemberships;
  
  public List<ProvisioningEntity> getProvisioningEntities() {
    return provisioningEntities;
  }


  public List<ProvisioningGroup> getProvisioningGroups() {
    return provisioningGroups;
  }

  /**
   * provisioning memberships from target
   * @return
   */
  public List<ProvisioningMembership> getProvisioningMemberships() {
    return provisioningMemberships;
  }


  public void setProvisioningEntities(List<ProvisioningEntity> provisioningEntities) {
    this.provisioningEntities = provisioningEntities;
  }


  public void setProvisioningGroups(List<ProvisioningGroup> provisioningGroups) {
    this.provisioningGroups = provisioningGroups;
  }

  /**
   * provisioning memberships from target
   * @param provisioningMemberships
   */
  public void setProvisioningMemberships(
      List<ProvisioningMembership> provisioningMemberships) {
    this.provisioningMemberships = provisioningMemberships;
  }


  public TargetDaoRetrieveIncrementalDataResponse() {
  }

  /**
   * 
   * @param provisioningGroups1
   * @param provisioningEntities1
   * @param provisioningMemberships1 provisioning memberships from target 
   */
  public TargetDaoRetrieveIncrementalDataResponse(List<ProvisioningGroup> provisioningGroups1, 
      List<ProvisioningEntity> provisioningEntities1, 
      List<ProvisioningMembership> provisioningMemberships1) {
    this.provisioningGroups = provisioningGroups1;
    this.provisioningEntities = provisioningEntities1;
    this.provisioningMemberships = provisioningMemberships1;
  }

}
