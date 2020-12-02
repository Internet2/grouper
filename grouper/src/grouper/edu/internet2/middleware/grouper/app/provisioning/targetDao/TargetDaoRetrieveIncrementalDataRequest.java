package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class TargetDaoRetrieveIncrementalDataRequest {

  public TargetDaoRetrieveIncrementalDataRequest() {
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
   * multi key of targetGroup, targetEntity, and optionally targetMembership for membership sync
   */
  private List<Object> targetMembershipObjectsForMembershipSync = null;
  
  /**
   * multi key of targetGroup, targetEntity, and optionally targetMembership for membership sync
   * @return
   */
  public List<Object> getTargetMembershipObjectsForMembershipSync() {
    return targetMembershipObjectsForMembershipSync;
  }

  /**
   * multi key of targetGroup, targetEntity, and optionally targetMembership for membership sync
   * @param groupUuidsMemberUuidsForMembershipSync
   */
  public void setTargetMembershipObjectsForMembershipSync(
      List<Object> targetMembershipObjectsForMembershipSync) {
    this.targetMembershipObjectsForMembershipSync = targetMembershipObjectsForMembershipSync;
  }
  
}
