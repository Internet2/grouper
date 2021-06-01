package edu.internet2.middleware.grouper.app.provisioning.targetDao;

public class TargetDaoRetrieveMembershipResponse {

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   */
  private Object targetMembership;

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @return
   */
  public Object getTargetMembership() {
    return targetMembership;
  }
  
  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMembership
   */
  public void setTargetMembership(Object targetMembership) {
    this.targetMembership = targetMembership;
  }
  
  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   */
  public TargetDaoRetrieveMembershipResponse() {
  }
  
  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMembership
   */
  public TargetDaoRetrieveMembershipResponse(Object targetMembership) {
    this.targetMembership = targetMembership;
  }
  
}
