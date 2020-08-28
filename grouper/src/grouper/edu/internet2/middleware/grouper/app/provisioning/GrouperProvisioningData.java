package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisioningData {
  
  private GrouperProvisioner grouperProvisioner = null;
  
  private GrouperProvisioningLists grouperProvisioningObjects;

  private GrouperProvisioningLists targetProvisioningObjects;
  
  private GrouperProvisioningLists grouperCommonObjects;
  
  private GrouperProvisioningLists targetCommonObjects;

  private GrouperProvisioningLists commonObjectInserts;
  
  private GrouperProvisioningLists commonObjectUpdates;
  
  private GrouperProvisioningLists commonObjectDeletes;

  private GrouperProvisioningLists targetObjectInserts;
  
  private GrouperProvisioningLists targetObjectUpdates;
  
  private GrouperProvisioningLists targetObjectDeleles;
  
  private Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();

  private Map<String, GcGrouperSyncMember> memberUuidToSyncMember = new HashMap<String, GcGrouperSyncMember>();

  private Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToSyncMembership = new HashMap<MultiKey, GcGrouperSyncMembership>();
  
  
  
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

  
  public GrouperProvisioningLists getTargetObjectDeleles() {
    return targetObjectDeleles;
  }

  
  public void setTargetObjectDeleles(GrouperProvisioningLists targetObjectDeleles) {
    this.targetObjectDeleles = targetObjectDeleles;
  }
  

}
