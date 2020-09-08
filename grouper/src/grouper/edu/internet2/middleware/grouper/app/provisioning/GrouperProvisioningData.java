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
  }
  
  private GrouperProvisioner grouperProvisioner = null;

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




  public GcGrouperSync getGcGrouperSync() {
    return this.getGrouperProvisioner().getGcGrouperSync();
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
