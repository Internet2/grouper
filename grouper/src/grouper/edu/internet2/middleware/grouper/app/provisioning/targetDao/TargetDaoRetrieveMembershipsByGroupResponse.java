package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

public class TargetDaoRetrieveMembershipsByGroupResponse {

  public TargetDaoRetrieveMembershipsByGroupResponse() {
  }

  /**
   * some native representation of the target group, only pass around if needed, and only for groupAttributes
   */
  private Object targetNativeGroup;

  
  /**
   * some native representation of the target group, only pass around if needed, and only for groupAttributes
   * @return
   */
  public Object getTargetNativeGroup() {
    return targetNativeGroup;
  }
  
  /**
   * some native representation of the target group, only pass around if needed, and only for groupAttributes
   * @param targetNativeGroup
   */
  public void setTargetNativeGroup(Object targetNativeGroup) {
    this.targetNativeGroup = targetNativeGroup;
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
  public TargetDaoRetrieveMembershipsByGroupResponse(List<Object> targetMemberships) {
    super();
    this.targetMemberships = targetMemberships;
  }
}
