package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisioningData {
  
  public GrouperProvisioningData() {
    this.grouperProvisioningObjectsToDelete.setProvisioningGroups(new ArrayList<ProvisioningGroup>());
    this.grouperProvisioningObjectsToDelete.setProvisioningEntities(new ArrayList<ProvisioningEntity>());
    this.grouperProvisioningObjectsToDelete.setProvisioningMemberships(new ArrayList<ProvisioningMembership>());
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * these are in sync objects but not in grouper
   */
  private GrouperProvisioningLists grouperProvisioningObjectsToDelete = new GrouperProvisioningLists();

  private GrouperProvisioningLists grouperProvisioningObjects = new GrouperProvisioningLists();

  private GrouperProvisioningLists targetProvisioningObjects = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists grouperCommonObjects = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetCommonObjects = new GrouperProvisioningLists();

  private GrouperProvisioningLists commonObjectInserts = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists commonObjectUpdates = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists commonObjectDeletes = new GrouperProvisioningLists();

  private GrouperProvisioningLists targetObjectInserts = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectUpdates = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectDeletes = new GrouperProvisioningLists();
  
  private Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = null;

  private Map<String, GcGrouperSyncMember> memberUuidToSyncMember = null;

  private Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToSyncMembership = null;
  
  /**
   * note some entries could be for deleting
   */
  private Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> groupIdMemberIdToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();
  
  
  
  /**
   * these are in sync objects but not in grouper
   * @return
   */
  public GrouperProvisioningLists getGrouperProvisioningObjectsToDelete() {
    return grouperProvisioningObjectsToDelete;
  }




  /**
   * these are in sync objects but not in grouper
   * @param grouperProvisioningObjectsToDelete
   */
  public void setGrouperProvisioningObjectsToDelete(
      GrouperProvisioningLists grouperProvisioningObjectsToDelete) {
    this.grouperProvisioningObjectsToDelete = grouperProvisioningObjectsToDelete;
  }

  public GcGrouperSync getGcGrouperSync() {
    return this.getGrouperProvisioner().getGcGrouperSync();
  }
  
  /**
   * some sync ids we dont care about since they are assumed to be removed in target
   * keep the full list here
   */
  private Map<String, GcGrouperSyncGroup> groupUuidToSyncGroupIncludeRemoved = null;

  /**
   * some sync ids we dont care about since they are assumed to be removed in target
   * keep the full list here
   */
  private Map<String, GcGrouperSyncMember> memberUuidToSyncMemberIncludeRemoved = null;

  /**
   * some sync ids we dont care about since they are assumed to be removed in target
   * keep the full list here
   */
  private Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdtoSyncMembershipIncludeRemoved = null;

  
  public Map<String, GcGrouperSyncGroup> getGroupUuidToSyncGroupIncludeRemoved() {
    return groupUuidToSyncGroupIncludeRemoved;
  }




  
  public void setGroupUuidToSyncGroupIncludeRemoved(
      Map<String, GcGrouperSyncGroup> groupUuidToSyncGroupIncludeRemoved) {
    this.groupUuidToSyncGroupIncludeRemoved = groupUuidToSyncGroupIncludeRemoved;
  }




  
  
  public Map<String, GcGrouperSyncMember> getMemberUuidToSyncMemberIncludeRemoved() {
    return memberUuidToSyncMemberIncludeRemoved;
  }




  
  public void setMemberUuidToSyncMemberIncludeRemoved(
      Map<String, GcGrouperSyncMember> memberUuidToSyncMemberIncludeRemoved) {
    this.memberUuidToSyncMemberIncludeRemoved = memberUuidToSyncMemberIncludeRemoved;
  }




  public Map<MultiKey, GcGrouperSyncMembership> getGroupIdMemberIdtoSyncMembershipIncludeRemoved() {
    return groupIdMemberIdtoSyncMembershipIncludeRemoved;
  }




  
  public void setGroupIdMemberIdtoSyncMembershipIncludeRemoved(
      Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdtoSyncMembershipIncludeRemoved) {
    this.groupIdMemberIdtoSyncMembershipIncludeRemoved = groupIdMemberIdtoSyncMembershipIncludeRemoved;
  }




  /**
   * note some entries could be for deleting
   * @return
   */
  public Map<String, ProvisioningGroupWrapper> getGroupUuidToProvisioningGroupWrapper() {
    return groupUuidToProvisioningGroupWrapper;
  }



  /**
   * note some entries could be for deleting
   * @return map
   */
  public Map<String, ProvisioningEntityWrapper> getMemberUuidToProvisioningEntityWrapper() {
    return memberUuidToProvisioningEntityWrapper;
  }



  
  public Map<MultiKey, ProvisioningMembershipWrapper> getGroupIdMemberIdToProvisioningMembershipWrapper() {
    return groupIdMemberIdToProvisioningMembershipWrapper;
  }



  public Map<MultiKey, GcGrouperSyncMembership> getGroupIdMemberIdToSyncMembership() {
    return groupIdMemberIdToSyncMembership;
  }



  public Map<String, GcGrouperSyncGroup> getGroupUuidToSyncGroup() {
    return groupUuidToSyncGroup;
  }
  
  
  
  public Map<String, GcGrouperSyncMember> getMemberUuidToSyncMember() {
    return memberUuidToSyncMember;
  }


  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  
  public GrouperProvisioningLists getGrouperProvisioningObjects() {
    return grouperProvisioningObjects;
  }

  
  
  public void setGroupUuidToSyncGroup(
      Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup) {
    this.groupUuidToSyncGroup = groupUuidToSyncGroup;
  }




  
  public void setMemberUuidToSyncMember(
      Map<String, GcGrouperSyncMember> memberUuidToSyncMember) {
    this.memberUuidToSyncMember = memberUuidToSyncMember;
  }




  
  public void setGroupIdMemberIdToSyncMembership(
      Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToSyncMembership) {
    this.groupIdMemberIdToSyncMembership = groupIdMemberIdToSyncMembership;
  }




  public void setGrouperProvisioningObjects(
      GrouperProvisioningLists grouperProvisioningObjects) {
    this.grouperProvisioningObjects = grouperProvisioningObjects;
  }

  
  public GrouperProvisioningLists getGrouperCommonObjects() {
    return grouperCommonObjects;
  }

  
  public void setGrouperCommonObjects(GrouperProvisioningLists grouperCommonObjects) {
    this.grouperCommonObjects = grouperCommonObjects;
  }

  
  public GrouperProvisioningLists getTargetProvisioningObjects() {
    return targetProvisioningObjects;
  }

  
  public void setTargetProvisioningObjects(
      GrouperProvisioningLists targetProvisioningObjects) {
    this.targetProvisioningObjects = targetProvisioningObjects;
  }

  
  public GrouperProvisioningLists getTargetCommonObjects() {
    return targetCommonObjects;
  }

  
  public void setTargetCommonObjects(GrouperProvisioningLists targetCommonObjects) {
    this.targetCommonObjects = targetCommonObjects;
  }

  
  public GrouperProvisioningLists getCommonObjectInserts() {
    return commonObjectInserts;
  }

  
  public void setCommonObjectInserts(GrouperProvisioningLists commonObjectInserts) {
    this.commonObjectInserts = commonObjectInserts;
  }

  
  public GrouperProvisioningLists getCommonObjectUpdates() {
    return commonObjectUpdates;
  }

  
  public void setCommonObjectUpdates(GrouperProvisioningLists commonObjectUpdates) {
    this.commonObjectUpdates = commonObjectUpdates;
  }

  
  public GrouperProvisioningLists getCommonObjectDeletes() {
    return commonObjectDeletes;
  }

  
  public void setCommonObjectDeletes(GrouperProvisioningLists commonObjectDeletes) {
    this.commonObjectDeletes = commonObjectDeletes;
  }

  
  public GrouperProvisioningLists getTargetObjectInserts() {
    return targetObjectInserts;
  }

  
  public void setTargetObjectInserts(GrouperProvisioningLists targetObjectInserts) {
    this.targetObjectInserts = targetObjectInserts;
  }

  
  public GrouperProvisioningLists getTargetObjectUpdates() {
    return targetObjectUpdates;
  }

  
  public void setTargetObjectUpdates(GrouperProvisioningLists targetObjectUpdates) {
    this.targetObjectUpdates = targetObjectUpdates;
  }

  
  public GrouperProvisioningLists getTargetObjectDeletes() {
    return targetObjectDeletes;
  }

  
  public void setTargetObjectDeletes(GrouperProvisioningLists targetObjectDeletes) {
    this.targetObjectDeletes = targetObjectDeletes;
  }
  

}
