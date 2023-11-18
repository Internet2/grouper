package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;

public class TargetDaoRetrieveIncrementalDataRequest {

  public TargetDaoRetrieveIncrementalDataRequest() {
  }

  /**
   * get the group objects and all the memberships for these.
   * note, these are also included in targetGroupsForGroupOnly
   */
  private List<ProvisioningGroup> targetGroupsForGroupAllMembershipSync = null;
  
  /**
   * get the group objects and some of the memberships for these.
   * let's say a group has 100 net ids as memberships in the group attribute 
   * and we only want to fetch 5 because they changed and one or more is recalc
   *  when one is recalc, all 5 become recalc
   * the netId will be the attribute based on which we want to fetch
   * note, these are also included in targetGroupsForGroupOnly
   */
  private List<ProvisioningGroup> targetGroupsForGroupSomeMembershipSync = null;
  
  /**
   * get the group objects and some of the memberships for these.
   * let's say a group has 100 net ids as memberships in the group attribute 
   * and we only want to fetch 5 because they changed and one or more is recalc
   * the netId will be the attribute based on which we want to fetch
   * note, these are also included in targetGroupsForGroupOnly
   */
  public List<ProvisioningGroup> getTargetGroupsForGroupSomeMembershipSync() {
    return targetGroupsForGroupSomeMembershipSync;
  }

  /**
   * get the group objects and some of the memberships for these.
   * let's say a group has 100 net ids as memberships in the group attribute 
   * and we only want to fetch 5 because they changed and one or more is recalc
   * the netId will be the attribute based on which we want to fetch
   * note, these are also included in targetGroupsForGroupOnly
   */
  public void setTargetGroupsForGroupSomeMembershipSync(
      List<ProvisioningGroup> targetGroupsForGroupSomeMembershipSync) {
    this.targetGroupsForGroupSomeMembershipSync = targetGroupsForGroupSomeMembershipSync;
  }


  /**
   * get the group objects and the memberships for these.
   * note, these are also included in targetGroupsForGroupOnly
   * @return
   */
  public List<ProvisioningGroup> getTargetGroupsForGroupAllMembershipSync() {
    return targetGroupsForGroupAllMembershipSync;
  }


  /**
   * get the group objects and the memberships for these.
   * note, these are also included in targetGroupsForGroupOnly
   * @param targetGroupsForGroupMembershipSync
   */
  public void setTargetGroupsForGroupAllMembershipSync(
      List<ProvisioningGroup> targetGroupsForGroupMembershipSync) {
    this.targetGroupsForGroupAllMembershipSync = targetGroupsForGroupMembershipSync;
  }

  /**
   * get the entity objects and all the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   */
  private List<ProvisioningEntity> targetEntitiesForEntityAllMembershipSync = null;

  /**
   * get the entity objects and some of the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   */
  private List<ProvisioningEntity> targetEntitiesForEntitySomeMembershipSync = null;
  
  /**
   * get the entity objects and some of the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   */
  public List<ProvisioningEntity> getTargetEntitiesForEntitySomeMembershipSync() {
    return targetEntitiesForEntitySomeMembershipSync;
  }

  /**
   * get the entity objects and some of the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   */
  public void setTargetEntitiesForEntitySomeMembershipSync(
      List<ProvisioningEntity> targetEntitiesForEntitySomeMembershipSync) {
    this.targetEntitiesForEntitySomeMembershipSync = targetEntitiesForEntitySomeMembershipSync;
  }

  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   * @return the uuids
   */
  public List<ProvisioningEntity> getTargetEntitiesForEntityAllMembershipSync() {
    return targetEntitiesForEntityAllMembershipSync;
  }


  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in targetEntitiesForEntityOnly
   * @param memberUuidsForEntityMembershipSync
   */
  public void setTargetEntitiesForEntityAllMembershipSync(
      List<ProvisioningEntity> memberUuidsForEntityMembershipSync) {
    this.targetEntitiesForEntityAllMembershipSync = memberUuidsForEntityMembershipSync;
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
   * targetMembership for membership sync
   */
  private List<ProvisioningMembership> targetMembershipObjectsForMembershipSync = null;
  
  /**
   * targetGroup for membership sync
   */
  private List<ProvisioningGroup> targetGroupsForSomeMembershipSync = null;
  
  /**
   * targetGroup for membership sync
   */
  public List<ProvisioningGroup> getTargetGroupsForSomeMembershipSync() {
    return targetGroupsForSomeMembershipSync;
  }


  /**
   * targetGroup for membership sync
   */
  public void setTargetGroupsForSomeMembershipSync(
      List<ProvisioningGroup> targetGroupsForSomeMembershipSync) {
    this.targetGroupsForSomeMembershipSync = targetGroupsForSomeMembershipSync;
  }

  /**
   * multi key of targetGroup, targetEntity, and optionally targetMembership for membership sync
   * @return
   */
  public List<ProvisioningMembership> getTargetMembershipObjectsForMembershipSync() {
    return targetMembershipObjectsForMembershipSync;
  }

  /**
   * multi key of targetGroup, targetEntity, and optionally targetMembership for membership sync
   * @param groupUuidsMemberUuidsForMembershipSync
   */
  public void setTargetMembershipObjectsForMembershipSync(
      List<ProvisioningMembership> targetMembershipObjectsForMembershipSync) {
    this.targetMembershipObjectsForMembershipSync = targetMembershipObjectsForMembershipSync;
  }
  
}
