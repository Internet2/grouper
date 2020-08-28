package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisioningData {
  
  private GrouperProvisioner grouperProvisioner = null;
  
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
  
  private Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();

  private Map<String, GcGrouperSyncMember> memberUuidToSyncMember = new HashMap<String, GcGrouperSyncMember>();

  private Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToSyncMembership = new HashMap<MultiKey, GcGrouperSyncMembership>();
  
  private Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> groupIdMemberIdToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();
  
  
  
  public Map<String, ProvisioningGroupWrapper> getGroupUuidToProvisioningGroupWrapper() {
    return groupUuidToProvisioningGroupWrapper;
  }



  
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
