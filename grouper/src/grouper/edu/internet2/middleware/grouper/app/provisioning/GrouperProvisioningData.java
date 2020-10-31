package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashSet;
import java.util.Set;

/**
 * main list of wrapper beans
 * @author mchyzer
 *
 */
public class GrouperProvisioningData {

  public GrouperProvisioningData() {
  }
  
  /**
   * all group wrappers
   */
  private Set<ProvisioningGroupWrapper> provisioningGroupWrappers = new HashSet<ProvisioningGroupWrapper>();

  /**
   * all entity wrappers
   */
  private Set<ProvisioningEntityWrapper> provisioningEntityWrappers = new HashSet<ProvisioningEntityWrapper>();

  /**
   * all membership wrappers
   */
  private Set<ProvisioningMembershipWrapper> provisioningMembershipWrappers = new HashSet<ProvisioningMembershipWrapper>();

  private GrouperProvisioner grouperProvisioner;

  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  
  public Set<ProvisioningGroupWrapper> getProvisioningGroupWrappers() {
    return provisioningGroupWrappers;
  }


  
  public void setProvisioningGroupWrappers(
      Set<ProvisioningGroupWrapper> provisioningGroupWrappers) {
    this.provisioningGroupWrappers = provisioningGroupWrappers;
  }


  
  public Set<ProvisioningEntityWrapper> getProvisioningEntityWrappers() {
    return provisioningEntityWrappers;
  }


  
  public void setProvisioningEntityWrappers(
      Set<ProvisioningEntityWrapper> provisioningEntityWrappers) {
    this.provisioningEntityWrappers = provisioningEntityWrappers;
  }


  
  public Set<ProvisioningMembershipWrapper> getProvisioningMembershipWrappers() {
    return provisioningMembershipWrappers;
  }


  
  public void setProvisioningMembershipWrappers(
      Set<ProvisioningMembershipWrapper> provisioningMembershipWrappers) {
    this.provisioningMembershipWrappers = provisioningMembershipWrappers;
  }

  

}
