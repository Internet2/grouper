package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveIncrementalDataResponse {
  private List<ProvisioningEntity> provisioningEntities;
  private List<ProvisioningGroup> provisioningGroups;
  
  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   */
  private List<Object> provisioningMemberships;

  
  public List<ProvisioningEntity> getProvisioningEntities() {
    return provisioningEntities;
  }


  public List<ProvisioningGroup> getProvisioningGroups() {
    return provisioningGroups;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @return
   */
  public List<Object> getProvisioningMemberships() {
    return provisioningMemberships;
  }


  public void setProvisioningEntities(List<ProvisioningEntity> provisioningEntities) {
    this.provisioningEntities = provisioningEntities;
  }


  public void setProvisioningGroups(List<ProvisioningGroup> provisioningGroups) {
    this.provisioningGroups = provisioningGroups;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param provisioningMemberships
   */
  public void setProvisioningMemberships(
      List<Object> provisioningMemberships) {
    this.provisioningMemberships = provisioningMemberships;
  }


  public TargetDaoRetrieveIncrementalDataResponse() {
  }

  /**
   * 
   * @param provisioningGroups1
   * @param provisioningEntities1
   * @param provisioningMemberships1 depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, 
   * ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   */
  public TargetDaoRetrieveIncrementalDataResponse(List<ProvisioningGroup> provisioningGroups1, 
      List<ProvisioningEntity> provisioningEntities1, 
      List<Object> provisioningMemberships1) {
    this.provisioningGroups = provisioningGroups1;
    this.provisioningEntities = provisioningEntities1;
    this.provisioningMemberships = provisioningMemberships1;
  }

}
