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
  
  private GrouperProvisioningLists grouperTargetObjects = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectInserts = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectUpdates = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectDeletes = new GrouperProvisioningLists();
  
  private Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = null;

  private Map<String, GcGrouperSyncMember> memberUuidToSyncMember = null;

  private Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership = null;
  
  /**
   * note some entries could be for deleting
   */
  private Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper = null;

  private Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper = null;

  private Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper = null;
  
  private Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();

  
  
  
  public Map<String, ProvisioningGroupWrapper> getGroupUuidToProvisioningGroupWrapper() {
    return groupUuidToProvisioningGroupWrapper;
  }





  
  public Map<String, ProvisioningEntityWrapper> getMemberUuidToProvisioningEntityWrapper() {
    return memberUuidToProvisioningEntityWrapper;
  }





  
  public Map<MultiKey, ProvisioningMembershipWrapper> getGroupUuidMemberUuidToProvisioningMembershipWrapper() {
    return groupUuidMemberUuidToProvisioningMembershipWrapper;
  }





  public void setTargetGroupIdToProvisioningGroupWrapper(
      Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper) {
    this.targetGroupIdToProvisioningGroupWrapper = targetGroupIdToProvisioningGroupWrapper;
  }




  
  public void setTargetEntityIdToProvisioningEntityWrapper(
      Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper) {
    this.targetEntityIdToProvisioningEntityWrapper = targetEntityIdToProvisioningEntityWrapper;
  }




  
  public void setTargetMembershipIdToProvisioningMembershipWrapper(
      Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper) {
    this.targetMembershipIdToProvisioningMembershipWrapper = targetMembershipIdToProvisioningMembershipWrapper;
  }




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
  private Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidtoSyncMembershipIncludeRemoved = null;

  
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




  public Map<MultiKey, GcGrouperSyncMembership> getGroupUuidMemberUuidtoSyncMembershipIncludeRemoved() {
    return groupUuidMemberUuidtoSyncMembershipIncludeRemoved;
  }




  
  public void setGroupUuidMemberUuidtoSyncMembershipIncludeRemoved(
      Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidtoSyncMembershipIncludeRemoved) {
    this.groupUuidMemberUuidtoSyncMembershipIncludeRemoved = groupUuidMemberUuidtoSyncMembershipIncludeRemoved;
  }






  
  public Map<Object, ProvisioningGroupWrapper> getTargetGroupIdToProvisioningGroupWrapper() {
    return targetGroupIdToProvisioningGroupWrapper;
  }




  
  public Map<Object, ProvisioningEntityWrapper> getTargetEntityIdToProvisioningEntityWrapper() {
    return targetEntityIdToProvisioningEntityWrapper;
  }




  
  public Map<Object, ProvisioningMembershipWrapper> getTargetMembershipIdToProvisioningMembershipWrapper() {
    return targetMembershipIdToProvisioningMembershipWrapper;
  }




  public Map<MultiKey, GcGrouperSyncMembership> getGroupUuidMemberUuidToSyncMembership() {
    return groupUuidMemberUuidToSyncMembership;
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




  
  public void setGroupUuidMemberUuidToSyncMembership(
      Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership) {
    this.groupUuidMemberUuidToSyncMembership = groupUuidMemberUuidToSyncMembership;
  }




  public void setGrouperProvisioningObjects(
      GrouperProvisioningLists grouperProvisioningObjects) {
    this.grouperProvisioningObjects = grouperProvisioningObjects;
  }

  
  public GrouperProvisioningLists getGrouperTargetObjects() {
    return grouperTargetObjects;
  }

  
  public void setGrouperTargetObjects(GrouperProvisioningLists grouperCommonObjects) {
    this.grouperTargetObjects = grouperCommonObjects;
  }

  
  public GrouperProvisioningLists getTargetProvisioningObjects() {
    return targetProvisioningObjects;
  }

  
  public void setTargetProvisioningObjects(
      GrouperProvisioningLists targetProvisioningObjects) {
    this.targetProvisioningObjects = targetProvisioningObjects;
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
