package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
  
  /**
   * make sure that all the target requests that include memberships also are requesting the group or entities in the "only" lists.
   */
  public void ensureAllMembershipRequestsAreInTheOnlyRequestsAlso() {
    
    if (GrouperUtil.length(targetGroupsForGroupMembershipSync) > 0) {
      
      Set<ProvisioningGroup> targetGroupsForGroupOnlySet = new HashSet<ProvisioningGroup>();
      this.targetGroupsForGroupOnly = GrouperUtil.nonNull(this.targetGroupsForGroupOnly);
      
      // indexing the groups that are already there
      for (ProvisioningGroup targetGroupForGroupOnly: targetGroupsForGroupOnly) {
        targetGroupsForGroupOnlySet.add(targetGroupForGroupOnly);
      }
      
      // if a membership object is not there then add it
      for (ProvisioningGroup targetGroupForGroupMembershipSync: targetGroupsForGroupMembershipSync) {
        if (!targetGroupsForGroupOnlySet.contains(targetGroupForGroupMembershipSync)) {
          this.targetGroupsForGroupOnly.add(targetGroupForGroupMembershipSync);
          targetGroupsForGroupOnlySet.add(targetGroupForGroupMembershipSync);
        }
      }
      
    }
    
    if (GrouperUtil.length(targetEntitiesForEntityMembershipSync) > 0) {
      
      Set<ProvisioningEntity> targetEntitiesForEntityOnlySet = new HashSet<ProvisioningEntity>();
      this.targetEntitiesForEntityOnly = GrouperUtil.nonNull(this.targetEntitiesForEntityOnly);
      
      // indexing the groups that are already there
      for (ProvisioningEntity targetEntityForEntityOnly: targetEntitiesForEntityOnly) {
        targetEntitiesForEntityOnlySet.add(targetEntityForEntityOnly);
      }
      
      // if a membership object is not there then add it
      for (ProvisioningEntity targetEntityForEntityMembershipSync: targetEntitiesForEntityMembershipSync) {
        if (!targetEntitiesForEntityOnlySet.contains(targetEntityForEntityMembershipSync)) {
          this.targetEntitiesForEntityOnly.add(targetEntityForEntityMembershipSync);
          targetEntitiesForEntityOnlySet.add(targetEntityForEntityMembershipSync);
        }
      }
      
    }
    
  }
  
}
