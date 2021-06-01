package edu.internet2.middleware.grouper.app.tableSync;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

/**
 * 
 * @author mchyzer
 *
 */
public class ProvisioningSyncResult {

  /**
   * number of objects stored (for logging)
   */
  private int syncObjectStoreCount;
  
  /**
   * number of objects stored (for logging)
   * @return number of objects
   */
  public int getSyncObjectStoreCount() {
    return this.syncObjectStoreCount;
  }

  /**
   * number of objects stored (for logging)
   * @param syncObjectStoreCount1
   */
  public void setSyncObjectStoreCount(int syncObjectStoreCount1) {
    this.syncObjectStoreCount = syncObjectStoreCount1;
  }

  /**
   * gc group sync with cache of all objects
   */
  private GcGrouperSync gcGrouperSync;
  
  
  /**
   * gc group sync with cache of all objects
   * @return sync
   */
  public GcGrouperSync getGcGrouperSync() {
    return this.gcGrouperSync;
  }

  /**
   * gc group sync with cache of all objects
   * @param gcGrouperSync1
   */
  public void setGcGrouperSync(GcGrouperSync gcGrouperSync1) {
    this.gcGrouperSync = gcGrouperSync1;
  }

  /**
   * if a group name change, this is the old and new name
   */
  private Set<String> groupIdsWithChangedNames;
  
  /**
   * if a group name change, this is the old and new name
   * @return map
   */
  public Set<String> getGroupIdsWithChangedNames() {
    return this.groupIdsWithChangedNames;
  }

  /**
   * if a group name change, this is the old and new name
   * @param oldNameToGcGrouperSyncGroup1
   */
  public void setGroupIdsWithChangedNames(
      Set<String> oldNameToGcGrouperSyncGroup1) {
    this.groupIdsWithChangedNames = oldNameToGcGrouperSyncGroup1;
  }

  /**
   * if subject id changes
   */
  private Set<String> memberIdsWithChangedSubjectIds;

  

  /**
   * if subject id changes
   * @return
   */
  public Set<String> getMemberIdsWithChangedSubjectIds() {
    return memberIdsWithChangedSubjectIds;
  }

  /**
   * if subject id changes
   * @param memberIdsWithChangedSubjectIds
   */
  public void setMemberIdsWithChangedSubjectIds(
      Set<String> memberIdsWithChangedSubjectIds) {
    this.memberIdsWithChangedSubjectIds = memberIdsWithChangedSubjectIds;
  }

  /**
   * if an id index changes, this is the old and new
   */
  private Set<String> groupIdsWithChangedIdIndexes;
  
  /**
   * if an id index changes, this is the old and new
   * @return the old index with the new metadata
   */
  public Set<String> getGroupIdsWithChangedIdIndexes() {
    return this.groupIdsWithChangedIdIndexes;
  }

  /**
   * if an id index changes, this is the old and new
   * @param oldIndexToGcGrouperSyncGroup1
   */
  public void setGroupIdsWithChangedIdIndexes(
      Set<String> oldIndexToGcGrouperSyncGroup1) {
    this.groupIdsWithChangedIdIndexes = oldIndexToGcGrouperSyncGroup1;
  }

  public ProvisioningSyncResult() {

  }

  /**
   * group ids to delete sync group ids
   */
  private Set<String> groupIdsToDelete;

  /**
   * group ids to delete sync group ids
   * @return group ids
   */
  public Set<String> getGroupIdsToDelete() {
    return this.groupIdsToDelete;
  }

  /**
   * group ids to delete sync group ids
   * @param groupIdsToDelete1
   */
  public void setGroupIdsToDelete(Set<String> groupIdsToDelete1) {
    this.groupIdsToDelete = groupIdsToDelete1;
  }

  /**
   * group ids to update sync group ids
   */
  private Set<String> groupIdsToUpdate;
  
  /**
   * group ids to update sync group ids
   * @return group ids
   */
  public Set<String> getGroupIdsToUpdate() {
    return this.groupIdsToUpdate;
  }

  /**
   * group ids to update sync group ids
   * @param groupIdsToUpdate1
   */
  public void setGroupIdsToUpdate(Set<String> groupIdsToUpdate1) {
    this.groupIdsToUpdate = groupIdsToUpdate1;
  }

  /**
   * group ids to insert sync group ids
   */
  private Set<String> groupIdsToInsert;
  
  /**
   * group ids to insert sync group ids
   * @return group ids
   */
  public Set<String> getGroupIdsToInsert() {
    return this.groupIdsToInsert;
  }

  /**
   * group ids to insert sync group ids
   * @param groupIdsToInsert1
   */
  public void setGroupIdsToInsert(Set<String> groupIdsToInsert1) {
    this.groupIdsToInsert = groupIdsToInsert1;
  }

  /**
   * member ids to delete sync member ids
   */
  private Set<String> memberIdsToDelete;
  /**
   * member ids to insert sync member ids
   */
  private Set<String> memberIdsToInsert;
  /**
   * member ids to update sync member ids
   */
  private Set<String> memberIdsToUpdate;
  /**
   * membership id to delete
   */
  private Set<MultiKey> membershipGroupIdMemberIdsToDelete;
  /**
   * group id member id to insert sync membership ids
   */
  private Set<MultiKey> membershipGroupIdMemberIdsToInsert;
  /**
   * membership ids to update sync membership ids
   */
  private Set<MultiKey> membershipGroupIdMemberIdsToUpdate;
  public Set<String> getMemberIdsToDelete() {
    return memberIdsToDelete;
  }

  
  public void setMemberIdsToDelete(Set<String> memberIdsToDelete) {
    this.memberIdsToDelete = memberIdsToDelete;
  }

  
  public Set<String> getMemberIdsToInsert() {
    return memberIdsToInsert;
  }

  
  public void setMemberIdsToInsert(Set<String> memberIdsToInsert) {
    this.memberIdsToInsert = memberIdsToInsert;
  }

  
  public Set<String> getMemberIdsToUpdate() {
    return memberIdsToUpdate;
  }

  
  public void setMemberIdsToUpdate(Set<String> memberIdsToUpdate) {
    this.memberIdsToUpdate = memberIdsToUpdate;
  }

  
  public Set<MultiKey> getMembershipGroupIdMemberIdsToDelete() {
    return membershipGroupIdMemberIdsToDelete;
  }

  
  public void setMembershipGroupIdMemberIdsToDelete(Set<MultiKey> membershipIdsToDelete) {
    this.membershipGroupIdMemberIdsToDelete = membershipIdsToDelete;
  }

  
  public Set<MultiKey> getMembershipGroupIdMemberIdsToInsert() {
    return membershipGroupIdMemberIdsToInsert;
  }

  
  public void setMembershipGroupIdMemberIdsToInsert(
      Set<MultiKey> membershipGroupIdMemberIdToInsert) {
    this.membershipGroupIdMemberIdsToInsert = membershipGroupIdMemberIdToInsert;
  }

  
  public Set<MultiKey> getMembershipGroupIdMemberIdsToUpdate() {
    return membershipGroupIdMemberIdsToUpdate;
  }

  
  public void setMembershipGroupIdMemberIdsToUpdate(Set<MultiKey> membershipIdsToUpdate) {
    this.membershipGroupIdMemberIdsToUpdate = membershipIdsToUpdate;
  }

  
}
