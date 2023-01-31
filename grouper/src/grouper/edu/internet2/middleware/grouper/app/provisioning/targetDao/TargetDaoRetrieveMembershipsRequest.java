package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsRequest {
  
  private List<ProvisioningGroup> targetGroups;
  
  private List<ProvisioningEntity> targetEntities;
  
  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }

  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }


  private List<ProvisioningMembership> targetMemberships;

  /**
   * @return
   */
  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }

  /**
   * @param targetMemberships
   */
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public TargetDaoRetrieveMembershipsRequest(
      List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }


  public TargetDaoRetrieveMembershipsRequest() {
  }
  
}
