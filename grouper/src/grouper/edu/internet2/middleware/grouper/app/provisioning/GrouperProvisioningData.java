package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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


  /**
   * extract list of non null target provisioning groups
   * @return groups
   */
  public List<ProvisioningGroup> retrieveTargetProvisioningGroups() {
    List<ProvisioningGroup> targetProvisioningGroups = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.provisioningGroupWrappers) {
      ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper.getTargetProvisioningGroup();
      if (targetProvisioningGroup != null) {
        targetProvisioningGroups.add(targetProvisioningGroup);
      }
    }
    return targetProvisioningGroups;
  }

  /**
   * extract list of non null target provisioning entities
   * @return entities
   */
  public List<ProvisioningEntity> retrieveTargetProvisioningEntities() {
    List<ProvisioningEntity> targetProvisioningEntities = new ArrayList<ProvisioningEntity>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.provisioningEntityWrappers) {
      ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
      if (targetProvisioningEntity != null) {
        targetProvisioningEntities.add(targetProvisioningEntity);
      }
    }
    return targetProvisioningEntities;
  }

  /**
   * extract list of non null target provisioning memberships
   * @return memberships
   */
  public List<ProvisioningMembership> retrieveTargetProvisioningMemberships() {
    List<ProvisioningMembership> targetProvisioningMemberships = new ArrayList<ProvisioningMembership>();
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.provisioningMembershipWrappers) {
      ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();
      if (targetProvisioningMembership != null) {
        targetProvisioningMemberships.add(targetProvisioningMembership);
      }
    }
    return targetProvisioningMemberships;
  }

  /**
   * extract list of non null grouper target groups
   * @return groups
   */
  public List<ProvisioningGroup> retrieveGrouperTargetGroups() {
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.provisioningGroupWrappers) {
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      if (grouperTargetGroup != null) {
        grouperTargetGroups.add(grouperTargetGroup);
      }
    }
    return grouperTargetGroups;
  }

  /**
   * extract list of non null grouper target entities
   * @return groups
   */
  public List<ProvisioningEntity> retrieveGrouperTargetEntities() {
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.provisioningEntityWrappers) {
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      if (grouperTargetEntity != null) {
        grouperTargetEntities.add(grouperTargetEntity);
      }
    }
    return grouperTargetEntities;
  }

  /**
   * extract list of non null grouper target memberships
   * @return groups
   */
  public List<ProvisioningMembership> retrieveGrouperTargetMemberships() {
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.provisioningMembershipWrappers) {
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
      if (grouperTargetMembership != null) {
        grouperTargetMemberships.add(grouperTargetMembership);
      }
    }
    return grouperTargetMemberships;
  }

  /**
   * extract list of non null grouper provisioning groups
   * @return groups
   */
  public List<ProvisioningGroup> retrieveGrouperProvisioningGroups() {
    List<ProvisioningGroup> grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.provisioningGroupWrappers) {
      ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      if (grouperProvisioningGroup != null) {
        grouperProvisioningGroups.add(grouperProvisioningGroup);
      }
    }
    return grouperProvisioningGroups;
  }

  /**
   * extract list of non null grouper provisioning membership
   * @return memberships
   */
  public List<ProvisioningMembership> retrieveGrouperProvisioningMemberships() {
    List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.provisioningMembershipWrappers) {
      ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
      if (grouperProvisioningMembership != null) {
        grouperProvisioningMemberships.add(grouperProvisioningMembership);
      }
    }
    return grouperProvisioningMemberships;
  }

  /**
   * extract list of non null grouper provisioning entities
   * @return groups
   */
  public List<ProvisioningEntity> retrieveGrouperProvisioningEntities() {
    List<ProvisioningEntity> grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.provisioningEntityWrappers) {
      ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      if (grouperProvisioningEntity != null) {
        grouperProvisioningEntities.add(grouperProvisioningEntity);
      }
    }
    return grouperProvisioningEntities;
  }

  

}
