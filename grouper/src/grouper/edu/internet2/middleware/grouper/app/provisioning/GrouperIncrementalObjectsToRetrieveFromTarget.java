package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * groupertargetObjects to retrieve from target for incremental provisioning
 * @author mchyzer-local
 *
 */
public class GrouperIncrementalObjectsToRetrieveFromTarget {

  private GrouperProvisioner grouperProvisioner;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }
  
  /**
   * generally these arent needed, but if there are no grouper target groups, these might be useful
   * generally just use the grouperTargetGroupsForGroupMembershipSync objects
   */
  private List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupMembershipSync;
  
  
  
  /**
   * generally these arent needed, but if there are no grouper target groups, these might be useful
   * generally just use the grouperTargetGroupsForGroupMembershipSync objects
   * @return the wrappers to retrieve
   */
  public List<ProvisioningGroupWrapper> getProvisioningGroupWrappersForGroupMembershipSync() {
    return provisioningGroupWrappersForGroupMembershipSync;
  }


  /**
   * generally these arent needed, but if there are no grouper target groups, these might be useful
   * generally just use the grouperTargetGroupsForGroupMembershipSync objects
   * @param provisioningGroupWrappersForGroupMembershipSync
   */
  public void setProvisioningGroupWrappersForGroupMembershipSync(
      List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupMembershipSync) {
    this.provisioningGroupWrappersForGroupMembershipSync = provisioningGroupWrappersForGroupMembershipSync;
  }

  /**
   * get the group objects and the memberships for these.
   * note, these are also included in grouperTargetGroupsForGroupOnly
   */
  private List<ProvisioningGroup> grouperTargetGroupsForGroupMembershipSync = null;
  
  
  /**
   * get the group objects and the memberships for these.
   * note, these are also included in grouperTargetGroupsForGroupOnly
   * @return
   */
  public List<ProvisioningGroup> getGrouperTargetGroupsForGroupMembershipSync() {
    return grouperTargetGroupsForGroupMembershipSync;
  }


  /**
   * get the group objects and the memberships for these.
   * note, these are also included in grouperTargetGroupsForGroupOnly
   * @param grouperTargetGroupsForGroupMembershipSync
   */
  public void setGrouperTargetGroupsForGroupMembershipSync(
      List<ProvisioningGroup> grouperTargetGroupsForGroupMembershipSync) {
    this.grouperTargetGroupsForGroupMembershipSync = grouperTargetGroupsForGroupMembershipSync;
  }

  /**
   * get the entity wrappers that need a membership sync on the entity.  generally
   * these arent needed, just use grouperTargetEntitiesForEntityMembershipSync.
   * but maybe they are useful if there are not any target entities
   */
  private List<ProvisioningEntityWrapper> provisioningEntityWrappersforEntityMembershipSync = null;
  
  /**
   * get the entity wrappers that need a membership sync on the entity.  generally
   * these arent needed, just use grouperTargetEntitiesForEntityMembershipSync.
   * but maybe they are useful if there are not any target entities
   */
  public List<ProvisioningEntityWrapper> getProvisioningEntityWrappersforEntityMembershipSync() {
    return provisioningEntityWrappersforEntityMembershipSync;
  }

  /**
   * get the entity wrappers that need a membership sync on the entity.  generally
   * these arent needed, just use grouperTargetEntitiesForEntityMembershipSync.
   * but maybe they are useful if there are not any target entities
   * @param provisioningEntityWrappers
   */
  public void setProvisioningEntityWrappersforEntityMembershipSync(
      List<ProvisioningEntityWrapper> provisioningEntityWrappers) {
    this.provisioningEntityWrappersforEntityMembershipSync = provisioningEntityWrappers;
  }

  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in grouperTargetEntitiesForEntityOnly
   */
  private List<ProvisioningEntity> grouperTargetEntitiesForEntityMembershipSync = null;

  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in grouperTargetEntitiesForEntityOnly
   * @return the uuids
   */
  public List<ProvisioningEntity> getGrouperTargetEntitiesForEntityMembershipSync() {
    return grouperTargetEntitiesForEntityMembershipSync;
  }


  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in grouperTargetEntitiesForEntityOnly
   * @param memberUuidsForEntityMembershipSync
   */
  public void setGrouperTargetEntitiesForEntityMembershipSync(
      List<ProvisioningEntity> memberUuidsForEntityMembershipSync) {
    this.grouperTargetEntitiesForEntityMembershipSync = memberUuidsForEntityMembershipSync;
  }

  /**
   * if there are no grouperTargetGroups then here are the wrappers to retrieve if that helps.
   * generally you dont need this list, you only need grouperTargetGroupsForGroupOnly
   */
  private List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupOnly = null;

  /**
   * if there are no grouperTargetGroups then here are the wrappers to retrieve if that helps.
   * generally you dont need this list, you only need grouperTargetGroupsForGroupOnly
   * @return the group wrappers
   */
  public List<ProvisioningGroupWrapper> getProvisioningGroupWrappersForGroupOnly() {
    return provisioningGroupWrappersForGroupOnly;
  }


  /**
   * if there are no grouperTargetGroups then here are the wrappers to retrieve if that helps.
   * generally you dont need this list, you only need grouperTargetGroupsForGroupOnly
   * @param grouperWrappersForGroupOnly
   */
  public void setProvisioningGroupWrappersForGroupOnly(
      List<ProvisioningGroupWrapper> grouperWrappersForGroupOnly) {
    this.provisioningGroupWrappersForGroupOnly = grouperWrappersForGroupOnly;
  }

  /**
   * do not retrieve all memberships for these unless they are also included in grouperTargetGroupsForGroupMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   */
  private List<ProvisioningGroup> grouperTargetGroupsForGroupOnly = null;
  
  /**
   * do not retrieve all memberships for these unless they are also included in grouperTargetGroupsForGroupMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   * @return
   */
  public List<ProvisioningGroup> getGrouperTargetGroupsForGroupOnly() {
    return grouperTargetGroupsForGroupOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in grouperTargetGroupsForGroupMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   * @param groupUuidsForGroupOnly
   */
  public void setGrouperTargetGroupsForGroupOnly(List<ProvisioningGroup> groupUuidsForGroupOnly) {
    this.grouperTargetGroupsForGroupOnly = groupUuidsForGroupOnly;
  }

  /**
   * entity wrappers for entity only, in case there are target entities to refer to.
   * generally you dont need this list, you only need grouperTargetEntitiesForEntityOnly
   */
  private List<ProvisioningEntityWrapper> provisioningEntityWrappersForEntityOnly;

  /**
   * entity wrappers for entity only, in case there are target entities to refer to.
   * generally you dont need this list, you only need grouperTargetEntitiesForEntityOnly
   * @return wrapper
   */
  public List<ProvisioningEntityWrapper> getProvisioningEntityWrappersForEntityOnly() {
    return provisioningEntityWrappersForEntityOnly;
  }


  /**
   * entity wrappers for entity only, in case there are target entities to refer to.
   * generally you dont need this list, you only need grouperTargetEntitiesForEntityOnly
   * @param entityWrappersForEntityOnly
   */
  public void setProvisioningEntityWrappersForEntityOnly(
      List<ProvisioningEntityWrapper> entityWrappersForEntityOnly) {
    this.provisioningEntityWrappersForEntityOnly = entityWrappersForEntityOnly;
  }

  /**
   * do not retrieve all memberships for these unless they are also included in grouperTargetEntitiesForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   */
  private List<ProvisioningEntity> grouperTargetEntitiesForEntityOnly = null;
  
  /**
   * do not retrieve all memberships for these unless they are also included in grouperTargetEntitiesForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   * @return
   */
  public List<ProvisioningEntity> getGrouperTargetEntitiesForEntityOnly() {
    return grouperTargetEntitiesForEntityOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in grouperTargetEntitiesForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   * @param memberUuidsForEntityOnly
   */
  public void setGrouperTargetEntitiesForEntityOnly(List<ProvisioningEntity> memberUuidsForEntityOnly) {
    this.grouperTargetEntitiesForEntityOnly = memberUuidsForEntityOnly;
  }

  /**
   * generally you can use target representations to know which memberships to get, but if you need it, here are the wrappers to retrieve
   * @return the multikey of group wrapper, entity wrapper, and membership wrapper (if there)
   */
  private List<MultiKey> provisioningGroupMemberMembershipWrappersForMembershipSync;
  
  /**
   * generally you can use target representations to know which memberships to get, but if you need it, here are the wrappers to retrieve
   * @return the multikey of group wrapper, entity wrapper, and membership wrapper (if there)
   * @return
   */
  public List<MultiKey> getProvisioningGroupMemberMembershipWrappersForMembershipSync() {
    return provisioningGroupMemberMembershipWrappersForMembershipSync;
  }

  /**
   * generally you can use target representations to know which memberships to get, but if you need it, here are the wrappers to retrieve
   * @return the multikey of group wrapper, entity wrapper, and membership wrapper (if there)
   * @param provisioningGroupMemberMembershipWrappersForMembershipSync
   */
  public void setProvisioningGroupMemberMembershipWrappersForMembershipSync(
      List<MultiKey> provisioningGroupMemberMembershipWrappersForMembershipSync) {
    this.provisioningGroupMemberMembershipWrappersForMembershipSync = provisioningGroupMemberMembershipWrappersForMembershipSync;
  }

  /**
   * multi key of grouperTargetGroup, grouperTargetEntity, and optionally grouperTargetMembership for membership sync
   */
  private List<MultiKey> grouperTargetGroupsEntitiesMembershipsForMembershipSync = null;
  
  /**
   * multi key of grouperTargetGroup, grouperTargetEntity, and optionally grouperTargetMembership for membership sync
   * @return
   */
  public List<MultiKey> getGrouperTargetGroupsEntitiesMembershipsForMembershipSync() {
    return grouperTargetGroupsEntitiesMembershipsForMembershipSync;
  }

  /**
   * multi key of grouperTargetGroup, grouperTargetEntity, and optionally grouperTargetMembership for membership sync
   * @param groupUuidsMemberUuidsForMembershipSync
   */
  public void setGrouperTargetGroupsEntitiesMembershipsForMembershipSync(
      List<MultiKey> groupUuidsMemberUuidsForMembershipSync) {
    this.grouperTargetGroupsEntitiesMembershipsForMembershipSync = groupUuidsMemberUuidsForMembershipSync;
  }
  
}
