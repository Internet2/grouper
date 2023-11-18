package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsResponse {
  
  private List<ProvisioningMembership> targetMemberships;
  
  private List<ProvisioningGroup> targetGroups;
  
  private List<ProvisioningEntity> targetEntities;

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @return
   */
  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }


  public TargetDaoRetrieveMembershipsResponse() {
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public TargetDaoRetrieveMembershipsResponse(
      List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }

  
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
  
}
