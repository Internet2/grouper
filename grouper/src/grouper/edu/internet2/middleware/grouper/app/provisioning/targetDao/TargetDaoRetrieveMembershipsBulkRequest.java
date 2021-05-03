package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveMembershipsBulkRequest {

  public TargetDaoRetrieveMembershipsBulkRequest() {
  }

  public TargetDaoRetrieveMembershipsBulkRequest(List<ProvisioningGroup> targetGroups,
      List<ProvisioningEntity> targetEntities,
      List<Object> targetGroupsEntitiesMemberships) {
    super();
    this.targetGroupsForAllMemberships = targetGroups;
    this.targetEntitiesForAllMemberships = targetEntities;
    this.targetMemberships = targetGroupsEntitiesMemberships;
  }

  private List<ProvisioningGroup> targetGroupsForAllMemberships;
  
  private List<ProvisioningEntity> targetEntitiesForAllMemberships;
  
  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   */
  private List<Object> targetMemberships;

  
  public List<ProvisioningGroup> getTargetGroupsForAllMemberships() {
    return targetGroupsForAllMemberships;
  }

  
  public void setTargetGroupsForAllMemberships(List<ProvisioningGroup> targetGroups) {
    this.targetGroupsForAllMemberships = targetGroups;
  }

  
  public List<ProvisioningEntity> getTargetEntitiesForAllMemberships() {
    return targetEntitiesForAllMemberships;
  }

  
  public void setTargetEntitiesForAllMemberships(List<ProvisioningEntity> targetEntities) {
    this.targetEntitiesForAllMemberships = targetEntities;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @return
   */
  public List<Object> getTargetMemberships() {
    return targetMemberships;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetGroupsEntitiesMemberships
   */
  public void setTargetGroupsEntitiesMemberships(
      List<Object> targetGroupsEntitiesMemberships) {
    this.targetMemberships = targetGroupsEntitiesMemberships;
  }


}
