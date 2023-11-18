package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipRequest {

  public TargetDaoRetrieveMembershipRequest() {
  }
  private ProvisioningMembership targetMembership;
  
  /**
   * if doing group attributes, then these are groups with some values that should be retrieved (not all)
   */
  private ProvisioningGroup targetGroup;
  
  /**
   * if doing entity attributes, then these are entities with some values that should be retrieved (not all)
   */
  private ProvisioningEntity targetEntity;
  
  /**
   * if doing group attributes, then these are groups with some values that should be retrieved (not all)
   * @return
   */
  public ProvisioningGroup getTargetGroup() {
    return targetGroup;
  }

  /**
   * if doing group attributes, then these are groups with some values that should be retrieved (not all)
   * @param targetGroup
   */
  public void setTargetGroup(ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }

  /**
   * if doing entity attributes, then these are entities with some values that should be retrieved (not all)
   * @return
   */
  public ProvisioningEntity getTargetEntity() {
    return targetEntity;
  }

  /**
   * if doing entity attributes, then these are entities with some values that should be retrieved (not all)
   * @param targetEntity
   */
  public void setTargetEntity(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }

  /**
   * @return
   */
  public ProvisioningMembership getTargetMembership() {
    return targetMembership;
  }
  
  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMembership
   */
  public void setTargetMembership(ProvisioningMembership targetMembership) {
    this.targetMembership = targetMembership;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMembership
   */
  public TargetDaoRetrieveMembershipRequest(ProvisioningMembership targetMembership) {
    this.targetMembership = targetMembership;
  }
  
  
}
