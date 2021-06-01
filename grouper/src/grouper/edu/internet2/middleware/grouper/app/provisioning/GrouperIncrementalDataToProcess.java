package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer-local
 *
 */
public class GrouperIncrementalDataToProcess {

  private GrouperProvisioner grouperProvisioner;

  public boolean isHasIncrementalDataToProcess() {
    if (GrouperUtil.length(this.groupUuidsForGroupMembershipSync) > 0) {
      return true;
    }
    if (GrouperUtil.length(this.groupUuidsForGroupOnly) > 0) {
      return true;
    }
    if (GrouperUtil.length(this.memberUuidsForEntityMembershipSync) > 0) {
      return true;
    }
    if (GrouperUtil.length(this.memberUuidsForEntityOnly) > 0) {
      return true;
    }
    if (GrouperUtil.length(this.groupUuidsMemberUuidsForMembershipSync) > 0) {
      return true;
    }
    return false;
  }

  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }
  
  /**
   * get the group objects and the memberships for these.
   * note, these are also included in groupUuidsForGroupOnly
   */
  private Set<GrouperIncrementalDataItem> groupUuidsForGroupMembershipSync = new HashSet<GrouperIncrementalDataItem>();
  
  
  /**
   * get the group objects and the memberships for these.
   * note, these are also included in groupUuidsForGroupOnly
   * @return
   */
  public Set<GrouperIncrementalDataItem> getGroupUuidsForGroupMembershipSync() {
    return groupUuidsForGroupMembershipSync;
  }


  /**
   * get the group objects and the memberships for these.
   * note, these are also included in groupUuidsForGroupOnly
   * @param groupUuidsForGroupMembershipSync
   */
  public void setGroupUuidsForGroupMembershipSync(
      Set<GrouperIncrementalDataItem> groupUuidsForGroupMembershipSync) {
    this.groupUuidsForGroupMembershipSync = groupUuidsForGroupMembershipSync;
  }


  
  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in memberUuidsForEntityOnly
   */
  private Set<GrouperIncrementalDataItem> memberUuidsForEntityMembershipSync = new HashSet<GrouperIncrementalDataItem>();

  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in memberUuidsForEntityOnly
   * @return the uuids
   */
  public Set<GrouperIncrementalDataItem> getMemberUuidsForEntityMembershipSync() {
    return memberUuidsForEntityMembershipSync;
  }


  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in memberUuidsForEntityOnly
   * @param memberUuidsForEntityMembershipSync
   */
  public void setMemberUuidsForEntityMembershipSync(
      Set<GrouperIncrementalDataItem> memberUuidsForEntityMembershipSync) {
    this.memberUuidsForEntityMembershipSync = memberUuidsForEntityMembershipSync;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   */
  private Set<GrouperIncrementalDataItem> groupUuidsForGroupOnly = new HashSet<GrouperIncrementalDataItem>();
  
  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   * @return
   */
  public Set<GrouperIncrementalDataItem> getGroupUuidsForGroupOnly() {
    return groupUuidsForGroupOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   * @param groupUuidsForGroupOnly
   */
  public void setGroupUuidsForGroupOnly(Set<GrouperIncrementalDataItem> groupUuidsForGroupOnly) {
    this.groupUuidsForGroupOnly = groupUuidsForGroupOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   */
  private Set<GrouperIncrementalDataItem> memberUuidsForEntityOnly = new HashSet<GrouperIncrementalDataItem>();
  
  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   * @return
   */
  public Set<GrouperIncrementalDataItem> getMemberUuidsForEntityOnly() {
    return memberUuidsForEntityOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   * @param memberUuidsForEntityOnly
   */
  public void setMemberUuidsForEntityOnly(Set<GrouperIncrementalDataItem> memberUuidsForEntityOnly) {
    this.memberUuidsForEntityOnly = memberUuidsForEntityOnly;
  }


  
  /**
   * multi key of group uuid, member uuids, field ids for membership sync
   */
  private Set<GrouperIncrementalDataItem> groupUuidsMemberUuidsForMembershipSync = new HashSet<GrouperIncrementalDataItem>();
  
  /**
   * multi key of group uuid, member uuids, field ids for membership sync
   * @return
   */
  public Set<GrouperIncrementalDataItem> getGroupUuidsMemberUuidsForMembershipSync() {
    return groupUuidsMemberUuidsForMembershipSync;
  }


  /**
   * multi key of group uuid, member uuids, field ids for membership sync
   * @param groupUuidsMemberUuidsForMembershipSync
   */
  public void setGroupUuidsMemberUuidsForMembershipSync(
      Set<GrouperIncrementalDataItem> groupUuidsMemberUuidsForMembershipSync) {
    this.groupUuidsMemberUuidsForMembershipSync = groupUuidsMemberUuidsForMembershipSync;
  }

  /**
   * group uuids which are going a group sync
   * initialized in the indexData()
   */
  private Set<String> groupUuidsForGroupMembershipRecalc = new HashSet<String>();

  /**
   * group uuids which are going a group sync
   * @return the set of group uuids
   */
  public Set<String> getGroupUuidsForGroupMembershipRecalc() {
    return groupUuidsForGroupMembershipRecalc;
  }

  /**
   * index data after its determined so we know if something is recalc or not
   */
  public void indexData() {
    
    this.groupUuidsForGroupMembershipRecalc.clear();
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(this.groupUuidsForGroupMembershipSync)) {
      
      String groupId = (String)grouperIncrementalDataItem.getItem();
      this.groupUuidsForGroupMembershipRecalc.add(groupId);
      
    }
    
  }

  
}
