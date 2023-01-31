package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

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
  
  private List<ProvisioningGroup> targetGroups;
  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   */
  private List<ProvisioningMembership> targetMemberships;

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

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupMemberships, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public TargetDaoRetrieveMembershipsByGroupResponse(List<ProvisioningMembership> targetMemberships) {
    super();
    this.targetMemberships = targetMemberships;
  }
}
