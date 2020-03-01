package edu.internet2.middleware.grouper.app.tableSync;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

/**
 * 
 * @author mchyzer
 *
 */
public class ProvisioningSyncResult {

  
  /**
   * gc grouper sync groups that were updated
   */
  private Set<GcGrouperSyncGroup> gcGrouperSyncGroupsToUpdate;
  
  
  
  
  public Set<GcGrouperSyncGroup> getGcGrouperSyncGroupsToUpdate() {
    return gcGrouperSyncGroupsToUpdate;
  }

  
  public void setGcGrouperSyncGroupsToUpdate(
      Set<GcGrouperSyncGroup> gcGrouperSyncGroupsToUpdate) {
    this.gcGrouperSyncGroupsToUpdate = gcGrouperSyncGroupsToUpdate;
  }

  /**
   * group that is in target, and is provisionable, no changes
   */
  private Set<GcGrouperSyncGroup> gcGrouperSyncGroupsNoChange;
  
  /**
   * group that is in target, and is provisionable, no changes
   */
  public Set<GcGrouperSyncGroup> getGcGrouperSyncGroupsNoChange() {
    return this.gcGrouperSyncGroupsNoChange;
  }
  
  /**
   * group that is in target, and is provisionable, no changes
   */
  public void setGcGrouperSyncGroupsNoChange(
      Set<GcGrouperSyncGroup> gcGrouperSyncGroupsNoChange1) {
    this.gcGrouperSyncGroupsNoChange = gcGrouperSyncGroupsNoChange1;
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
   * groups with provisioning attributes
   */
  private Set<Group> groupsProvisioned = null;
  
  /**
   * groups with provisioning attributes
   * @return groups provisioned
   */
  public Set<Group> getGroupsProvisioned() {
    return this.groupsProvisioned;
  }
  
  /**
   * groups with provisioning attributes
   * @param groupsProvisioned1
   */
  public void setGroupsProvisioned(Set<Group> groupsProvisioned1) {
    this.groupsProvisioned = groupsProvisioned1;
  }

  /**
   * map of group id to group
   */
  private Map<String, Group> mapGroupIdToGroup = null;

  /**
   * map of group id to group
   * @return map
   */
  public Map<String, Group> getMapGroupIdToGroup() {
    return this.mapGroupIdToGroup;
  }

  /**
   * map of group id to group
   * @param mapGroupIdToGroup1
   */
  public void setMapGroupIdToGroup(Map<String, Group> mapGroupIdToGroup1) {
    this.mapGroupIdToGroup = mapGroupIdToGroup1;
  }

  /**
   * sync group objects
   */
  private List<GcGrouperSyncGroup> gcGrouperSyncGroups = null;
  
  /**
   * @return sync group objects
   */
  public List<GcGrouperSyncGroup> getGcGrouperSyncGroups() {
    return this.gcGrouperSyncGroups;
  }

  /**
   * sync group objects
   * @param gcGrouperSyncGroups1
   */
  public void setGcGrouperSyncGroups(List<GcGrouperSyncGroup> gcGrouperSyncGroups1) {
    this.gcGrouperSyncGroups = gcGrouperSyncGroups1;
  }

  /**
   * map of group id to grouper sync group objects
   */
  private Map<String, GcGrouperSyncGroup> mapGroupIdToGcGrouperSyncGroup = null;

  /**
   * map of group id to grouper sync group objects
   * @return map
   */
  public Map<String, GcGrouperSyncGroup> getMapGroupIdToGcGrouperSyncGroup() {
    return mapGroupIdToGcGrouperSyncGroup;
  }

  /**
   * map of group id to grouper sync group objects
   * @param mapGroupIdToGcGrouperSyncGroup1
   */
  public void setMapGroupIdToGcGrouperSyncGroup(
      Map<String, GcGrouperSyncGroup> mapGroupIdToGcGrouperSyncGroup1) {
    this.mapGroupIdToGcGrouperSyncGroup = mapGroupIdToGcGrouperSyncGroup1;
  }

  
}
