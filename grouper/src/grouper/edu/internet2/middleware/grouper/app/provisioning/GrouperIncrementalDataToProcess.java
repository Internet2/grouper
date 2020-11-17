package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * 
 * @author mchyzer-local
 *
 */
public class GrouperIncrementalDataToProcess {

  private GrouperProvisioner grouperProvisioner;

  
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
  private Set<String> groupUuidsForGroupMembershipSync = new TreeSet<String>();
  
  
  /**
   * get the group objects and the memberships for these.
   * note, these are also included in groupUuidsForGroupOnly
   * @return
   */
  public Set<String> getGroupUuidsForGroupMembershipSync() {
    return groupUuidsForGroupMembershipSync;
  }


  /**
   * get the group objects and the memberships for these.
   * note, these are also included in groupUuidsForGroupOnly
   * @param groupUuidsForGroupMembershipSync
   */
  public void setGroupUuidsForGroupMembershipSync(
      Set<String> groupUuidsForGroupMembershipSync) {
    this.groupUuidsForGroupMembershipSync = groupUuidsForGroupMembershipSync;
  }


  
  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in memberUuidsForEntityOnly
   */
  private Set<String> memberUuidsForEntityMembershipSync = new TreeSet<String>();

  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in memberUuidsForEntityOnly
   * @return the uuids
   */
  public Set<String> getMemberUuidsForEntityMembershipSync() {
    return memberUuidsForEntityMembershipSync;
  }


  /**
   * get the entity objects and the memberships for these.
   * note, these are also included in memberUuidsForEntityOnly
   * @param memberUuidsForEntityMembershipSync
   */
  public void setMemberUuidsForEntityMembershipSync(
      Set<String> memberUuidsForEntityMembershipSync) {
    this.memberUuidsForEntityMembershipSync = memberUuidsForEntityMembershipSync;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   */
  private Set<String> groupUuidsForGroupOnly = new TreeSet<String>();
  
  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   * @return
   */
  public Set<String> getGroupUuidsForGroupOnly() {
    return groupUuidsForGroupOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.
   * Just get the group objects.
   * these are for group metadata or referenced in a membership
   * @param groupUuidsForGroupOnly
   */
  public void setGroupUuidsForGroupOnly(Set<String> groupUuidsForGroupOnly) {
    this.groupUuidsForGroupOnly = groupUuidsForGroupOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   */
  private Set<String> memberUuidsForEntityOnly = new TreeSet<String>();
  
  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   * @return
   */
  public Set<String> getMemberUuidsForEntityOnly() {
    return memberUuidsForEntityOnly;
  }


  /**
   * do not retrieve all memberships for these unless they are also included in memberUuidsForEntityMembershipSync.  
   * Just get the entity objects.
   * these are for entity metadata or referenced in a membership
   * @param memberUuidsForEntityOnly
   */
  public void setMemberUuidsForEntityOnly(Set<String> memberUuidsForEntityOnly) {
    this.memberUuidsForEntityOnly = memberUuidsForEntityOnly;
  }


  
  /**
   * multi key of group uuid, member uuids, field ids for membership sync
   */
  private Set<MultiKey> groupUuidsMemberUuidsFieldIdsForMembershipSync = new HashSet<MultiKey>();
  
  /**
   * multi key of group uuid, member uuids, field ids for membership sync
   * @return
   */
  public Set<MultiKey> getGroupUuidsMemberUuidsFieldIdsForMembershipSync() {
    return groupUuidsMemberUuidsFieldIdsForMembershipSync;
  }


  /**
   * multi key of group uuid, member uuids, field ids for membership sync
   * @param groupUuidsMemberUuidsForMembershipSync
   */
  public void setGroupUuidsMemberUuidsFieldIdsForMembershipSync(
      Set<MultiKey> groupUuidsMemberUuidsForMembershipSync) {
    this.groupUuidsMemberUuidsFieldIdsForMembershipSync = groupUuidsMemberUuidsForMembershipSync;
  }

  
}
