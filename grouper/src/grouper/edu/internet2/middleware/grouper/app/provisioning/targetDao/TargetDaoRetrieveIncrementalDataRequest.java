package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class TargetDaoRetrieveIncrementalDataRequest {

  public TargetDaoRetrieveIncrementalDataRequest() {
  }

  /**
   * generally these arent needed, but if there are no grouper target groups, these might be useful
   * generally just use the targetGroupsForGroupMembershipSync objects
   */
  private List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupMembershipSync;
  
  
  
  /**
   * generally these arent needed, but if there are no grouper target groups, these might be useful
   * generally just use the targetGroupsForGroupMembershipSync objects
   * @return the wrappers to retrieve
   */
  public List<ProvisioningGroupWrapper> getProvisioningGroupWrappersForGroupMembershipSync() {
    return provisioningGroupWrappersForGroupMembershipSync;
  }


  /**
   * generally these arent needed, but if there are no grouper target groups, these might be useful
   * generally just use the targetGroupsForGroupMembershipSync objects
   * @param provisioningGroupWrappersForGroupMembershipSync
   */
  public void setProvisioningGroupWrappersForGroupMembershipSync(
      List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupMembershipSync) {
    this.provisioningGroupWrappersForGroupMembershipSync = provisioningGroupWrappersForGroupMembershipSync;
  }

  /**
   * get the group objects and the memberships for these.
   * note, these are also included in targetGroupsForGroupOnly
   */
  private List<ProvisioningGroup> targetGroupsForGroupMembershipSync = null;
  
  
  /**
   * get the group objects and the memberships for these.
   * note, these are also included in targetGroupsForGroupOnly
   * @return
   */
  public List<ProvisioningGroup> getTargetGroupsForGroupMembershipSync() {
    return targetGroupsForGroupMembershipSync;
  }


  /**
   * get the group objects and the memberships for these.
   * note, these are also included in targetGroupsForGroupOnly
   * @param targetGroupsForGroupMembershipSync
   */
  public void setTargetGroupsForGroupMembershipSync(
      List<ProvisioningGroup> targetGroupsForGroupMembershipSync) {
    this.targetGroupsForGroupMembershipSync = targetGroupsForGroupMembershipSync;
  }

  /**
   * get the entity wrappers that need a membership sync on the entity.  generally
   * these arent needed, just use targetEntitiesForEntityMembershipSync.
   * but maybe they are useful if there are not any target entities
   */
  private List<ProvisioningEntityWrapper> provisioningEntityWrappersforEntityMembershipSync = null;
  
  /**
   * get the entity wrappers that need a membership sync on the entity.  generally
   * these arent needed, just use targetEntitiesForEntityMembershipSync.
   * but maybe they are useful if there are not any target entities
   */
  public List<ProvisioningEntityWrapper> getProvisioningEntityWrappersforEntityMembershipSync() {
    return provisioningEntityWrappersforEntityMembershipSync;
  }

  /**
   * get the entity wrappers that need a membership sync on the entity.  generally
   * these arent needed, just use targetEntitiesForEntityMembershipSync.
   * but maybe they are useful if there are not any target entities
   * @param provisioningEntityWrappers
   */
  public void setProvisioningEntityWrappersforEntityMembershipSync(
      List<ProvisioningEntityWrapper> provisioningEntityWrappers) {
    this.provisioningEntityWrappersforEntityMembershipSync = provisioningEntityWrappers;
  }

  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   */
  private List<ProvisioningEntity> targetEntitiesForEntityMembershipSync = null;

  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   * @return the uuids
   */
  public List<ProvisioningEntity> getTargetEntitiesForEntityMembershipSync() {
    return targetEntitiesForEntityMembershipSync;
  }


  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   * @param memberUuidsForEntityMembershipSync
   */
  public void setTargetEntitiesForEntityMembershipSync(
      List<ProvisioningEntity> memberUuidsForEntityMembershipSync) {
    this.targetEntitiesForEntityMembershipSync = memberUuidsForEntityMembershipSync;
  }

  /**
   * if there are no targetGroups then here are the wrappers to retrieve if that helps.
   * generally you dont need this list, you only need targetGroupsForGroupOnly
   */
  private List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupOnly = null;

  /**
   * if there are no targetGroups then here are the wrappers to retrieve if that helps.
   * generally you dont need this list, you only need targetGroupsForGroupOnly
   * @return the group wrappers
   */
  public List<ProvisioningGroupWrapper> getProvisioningGroupWrappersForGroupOnly() {
    return provisioningGroupWrappersForGroupOnly;
  }


  /**
   * if there are no targetGroups then here are the wrappers to retrieve if that helps.
   * generally you dont need this list, you only need targetGroupsForGroupOnly
   * @param provisioningGroupWrappersForGroupOnly
   */
  public void setProvisioningGroupWrappersForGroupOnly(
      List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupOnly) {
    this.provisioningGroupWrappersForGroupOnly = provisioningGroupWrappersForGroupOnly;
  }

  /**
   * do not retrieve all memberships for these unless they are also included in targetGroupsForGroupMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   */
  private List<ProvisioningGroup> targetGroupsForGroupOnly = null;
  
  /**
   * do not retrieve all memberships for these unless they are also included in targetGroupsForGroupMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   * @return
   */
  public List<ProvisioningGroup> getTargetGroupsForGroupOnly() {
    return targetGroupsForGroupOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in targetGroupsForGroupMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   * @param groupUuidsForGroupOnly
   */
  public void setTargetGroupsForGroupOnly(List<ProvisioningGroup> groupUuidsForGroupOnly) {
    this.targetGroupsForGroupOnly = groupUuidsForGroupOnly;
  }

  /**
   * entity wrappers for entity only, in case there are target entities to refer to.
   * generally you dont need this list, you only need targetEntitiesForEntityOnly
   */
  private List<ProvisioningEntityWrapper> provisioningEntityWrappersForEntityOnly;

  /**
   * entity wrappers for entity only, in case there are target entities to refer to.
   * generally you dont need this list, you only need targetEntitiesForEntityOnly
   * @return wrapper
   */
  public List<ProvisioningEntityWrapper> getProvisioningEntityWrappersForEntityOnly() {
    return provisioningEntityWrappersForEntityOnly;
  }


  /**
   * entity wrappers for entity only, in case there are target entities to refer to.
   * generally you dont need this list, you only need targetEntitiesForEntityOnly
   * @param entityWrappersForEntityOnly
   */
  public void setProvisioningEntityWrappersForEntityOnly(
      List<ProvisioningEntityWrapper> entityWrappersForEntityOnly) {
    this.provisioningEntityWrappersForEntityOnly = entityWrappersForEntityOnly;
  }

  /**
   * do not retrieve all memberships for these unless they are also included in targetEntitiesForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   */
  private List<ProvisioningEntity> targetEntitiesForEntityOnly = null;
  
  /**
   * do not retrieve all memberships for these unless they are also included in targetEntitiesForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   * @return
   */
  public List<ProvisioningEntity> getTargetEntitiesForEntityOnly() {
    return targetEntitiesForEntityOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in targetEntitiesForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   * @param memberUuidsForEntityOnly
   */
  public void setTargetEntitiesForEntityOnly(List<ProvisioningEntity> memberUuidsForEntityOnly) {
    this.targetEntitiesForEntityOnly = memberUuidsForEntityOnly;
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
   * multi key of targetGroup, targetEntity, and optionally targetMembership for membership sync
   */
  private List<MultiKey> targetGroupsEntitiesMembershipsForMembershipSync = null;
  
  /**
   * multi key of targetGroup, targetEntity, and optionally targetMembership for membership sync
   * @return
   */
  public List<MultiKey> getTargetGroupsEntitiesMembershipsForMembershipSync() {
    return targetGroupsEntitiesMembershipsForMembershipSync;
  }

  /**
   * multi key of targetGroup, targetEntity, and optionally targetMembership for membership sync
   * @param groupUuidsMemberUuidsForMembershipSync
   */
  public void setTargetGroupsEntitiesMembershipsForMembershipSync(
      List<MultiKey> groupUuidsMemberUuidsForMembershipSync) {
    this.targetGroupsEntitiesMembershipsForMembershipSync = groupUuidsMemberUuidsForMembershipSync;
  }
  
}
