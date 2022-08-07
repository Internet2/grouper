package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsByEntityResponse {

  public TargetDaoRetrieveMembershipsByEntityResponse() {
  }

  /**
   * some native representation of the target entity, only pass around if needed
   */
  private Object targetNativeEntity;
  
  
  /**
   * some native representation of the target entity, only pass around if needed
   * @return
   */
  public Object getTargetNativeEntity() {
    return targetNativeEntity;
  }

  /**
   * some native representation of the target entity, only pass around if needed
   * @param targetNativeEntity
   */
  public void setTargetNativeEntity(Object targetNativeEntity) {
    this.targetNativeEntity = targetNativeEntity;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   */
  private List<Object> targetMemberships;

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @return
   */
  public List<Object> getTargetMemberships() {
    return targetMemberships;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public void setTargetMemberships(List<Object> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public TargetDaoRetrieveMembershipsByEntityResponse(
      List<Object> targetMemberships) {
    super();
    this.targetMemberships = targetMemberships;
  }
  
  
  
}
