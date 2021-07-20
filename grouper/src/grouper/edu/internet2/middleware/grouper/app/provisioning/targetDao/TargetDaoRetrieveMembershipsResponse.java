package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

public class TargetDaoRetrieveMembershipsResponse {
  
  /**
   * <pre>
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * If there:
   * ProvisioningGroup {
   *   name: cn=a:b:c,ou=groups,ou=institution,dc=edu
   *   attribute: member
   *      value: cn=jsmith,ou=users,ou=institution,dc=edu
   * }
   * If not there, not in results
   * </pre>
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


  public TargetDaoRetrieveMembershipsResponse() {
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public TargetDaoRetrieveMembershipsResponse(
      List<Object> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }
  
  
  
}
