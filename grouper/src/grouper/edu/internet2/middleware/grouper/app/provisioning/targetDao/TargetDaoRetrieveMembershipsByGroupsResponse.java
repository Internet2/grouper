package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

public class TargetDaoRetrieveMembershipsByGroupsResponse {

  public TargetDaoRetrieveMembershipsByGroupsResponse() {
    // TODO Auto-generated constructor stub
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
  public TargetDaoRetrieveMembershipsByGroupsResponse(
      List<Object> targetMemberships) {
    super();
    this.targetMemberships = targetMemberships;
  }
  
}
