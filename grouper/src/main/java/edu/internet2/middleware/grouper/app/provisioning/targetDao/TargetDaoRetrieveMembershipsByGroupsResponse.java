package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveMembershipsByGroupsResponse {

  public TargetDaoRetrieveMembershipsByGroupsResponse() {
    // TODO Auto-generated constructor stub
  }
  
  /**
   * map of retrieved group to target native group, optional, only if the target native group is needed later on
   */
  private Map<ProvisioningGroup, Object> targetGroupToTargetNativeGroup = new HashMap<ProvisioningGroup, Object>();
  
  /**
   * map of retrieved group to target native group, optional, only if the target native group is needed later on
   * @return
   */
  public Map<ProvisioningGroup, Object> getTargetGroupToTargetNativeGroup() {
    return targetGroupToTargetNativeGroup;
  }

  /**
   * map of retrieved group to target native group, optional, only if the target native group is needed later on
   * @param targetGroupToTargetNativeGroup
   */
  public void setTargetGroupToTargetNativeGroup(Map<ProvisioningGroup, Object> targetGroupToTargetNativeGroup) {
    this.targetGroupToTargetNativeGroup = targetGroupToTargetNativeGroup;
  }

  /**
   * targetGroups
   */
  private List<ProvisioningGroup> targetGroups;
  
  
  public List<ProvisioningGroup> getTargetGroups() {
    return targetGroups;
  }

  
  public void setTargetGroups(List<ProvisioningGroup> targetGroups) {
    this.targetGroups = targetGroups;
  }

  /**
   * ProvisioningMemberships
   */
  private List<ProvisioningMembership> targetMemberships;
  
  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupAttributes, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @return
   */
  public List<ProvisioningMembership> getTargetMemberships() {
    return targetMemberships;
  }
  
  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupAttributes, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public void setTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    this.targetMemberships = targetMemberships;
  }

  /**
   * depends on type of membership provisioning.  This is ProvisioningGroup if groupAttributes, ProvisioningEntity if entityAttributes, and ProvisioningMembership if memberships
   * @param targetMemberships
   */
  public TargetDaoRetrieveMembershipsByGroupsResponse(
      List<ProvisioningMembership> targetMemberships) {
    super();
    this.targetMemberships = targetMemberships;
  }
  
}
