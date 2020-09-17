package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataRequest;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisioningData {

  public GrouperProvisioningData() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * grouper state of the data
   */
  private GrouperProvisioningLists grouperProvisioningObjects = new GrouperProvisioningLists();

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   */
  private GrouperProvisioningLists grouperProvisioningObjectsMissing = new GrouperProvisioningLists();

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   */
  private GrouperProvisioningLists grouperProvisioningObjectsCreatedPass1 = new GrouperProvisioningLists();

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   * @return
   */
  public GrouperProvisioningLists getGrouperProvisioningObjectsCreatedPass1() {
    return grouperProvisioningObjectsCreatedPass1;
  }

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   * @param grouperProvisioningObjectsCreatedPass1
   */
  public void setGrouperProvisioningObjectsCreatedPass1(
      GrouperProvisioningLists grouperProvisioningObjectsCreatedPass1) {
    this.grouperProvisioningObjectsCreatedPass1 = grouperProvisioningObjectsCreatedPass1;
  }

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   * @param grouperProvisioningObjectsMissing
   */
  public void setGrouperProvisioningObjectsMissing(
      GrouperProvisioningLists grouperProvisioningObjectsMissing) {
    this.grouperProvisioningObjectsMissing = grouperProvisioningObjectsMissing;
  }

  /**
   * grouper state of the data but include deletes so that the right stuff can be retrieved from the target
   */
  private GrouperProvisioningLists grouperProvisioningObjectsIncludeDeletes = new GrouperProvisioningLists();

  
  
  /**
   * grouper state of the data but include deletes so that the right stuff can be retrieved from the target
   * @return
   */
  public GrouperProvisioningLists getGrouperProvisioningObjectsIncludeDeletes() {
    return grouperProvisioningObjectsIncludeDeletes;
  }

  /**
   * grouper state of the data but include deletes so that the right stuff can be retrieved from the target
   * @param grouperProvisioningObjectsIncludeDeletes
   */
  public void setGrouperProvisioningObjectsIncludeDeletes(
      GrouperProvisioningLists grouperProvisioningObjectsIncludeDeletes) {
    this.grouperProvisioningObjectsIncludeDeletes = grouperProvisioningObjectsIncludeDeletes;
  }

  private GrouperProvisioningLists targetProvisioningObjects = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists grouperTargetObjects = new GrouperProvisioningLists();

  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   */
  private GrouperProvisioningLists targetProvisioningObjectsMissingCreated = new GrouperProvisioningLists();

  
  
  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   * @return
   */
  public GrouperProvisioningLists getTargetProvisioningObjectsMissingCreated() {
    return targetProvisioningObjectsMissingCreated;
  }

  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   * @param targetProvisioningObjectsMissingCreated
   */
  public void setTargetProvisioningObjectsMissingCreated(
      GrouperProvisioningLists targetProvisioningObjectsMissingCreated) {
    this.targetProvisioningObjectsMissingCreated = targetProvisioningObjectsMissingCreated;
  }

  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   */
  private GrouperProvisioningLists targetProvisioningObjectsMissingRetrieved = new GrouperProvisioningLists();


  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   * @return
   */
  public GrouperProvisioningLists getTargetProvisioningObjectsMissingRetrieved() {
    return targetProvisioningObjectsMissingRetrieved;
  }

  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   * @param grouperTargetObjectsMissingRetrieved
   */
  public void setTargetProvisioningObjectsMissingRetrieved(
      GrouperProvisioningLists grouperTargetObjectsMissingRetrieved) {
    this.targetProvisioningObjectsMissingRetrieved = grouperTargetObjectsMissingRetrieved;
  }

  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   */
  private GrouperProvisioningLists grouperTargetObjectsMissing = new GrouperProvisioningLists();

  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   * @return
   */
  public GrouperProvisioningLists getGrouperTargetObjectsMissing() {
    return grouperTargetObjectsMissing;
  }


  /**
   * in incremental keep track of groups and entities we need to retrieve or create in target before group link
   * @param grouperTargetObjectsMissing
   */
  public void setGrouperTargetObjectsMissing(
      GrouperProvisioningLists grouperTargetObjectsMissing) {
    this.grouperTargetObjectsMissing = grouperTargetObjectsMissing;
  }

  /**
   * grouper state of the data but include deletes so that the right stuff can be retrieved from the target
   */
  private GrouperProvisioningLists grouperTargetObjectsIncludeDeletes = new GrouperProvisioningLists();
  
  /**
   * grouper state of the data but include deletes so that the right stuff can be retrieved from the target
   * @return
   */
  public GrouperProvisioningLists getGrouperTargetObjectsIncludeDeletes() {
    return grouperTargetObjectsIncludeDeletes;
  }

  /**
   * grouper state of the data but include deletes so that the right stuff can be retrieved from the target
   * @param grouperTargetObjectsIncludeDeletes
   */
  public void setGrouperTargetObjectsIncludeDeletes(
      GrouperProvisioningLists grouperTargetObjectsIncludeDeletes) {
    this.grouperTargetObjectsIncludeDeletes = grouperTargetObjectsIncludeDeletes;
  }

  private GrouperProvisioningLists targetObjectInserts = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectUpdates = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectDeletes = new GrouperProvisioningLists();
  
  /**
   * include deletes
   */
  private Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = null;
  
  /**
   * include deletes
   */
  private Map<String, GcGrouperSyncMember> memberUuidToSyncMember = null;

  /**
   * include deletes
   */
  private Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership = null;
  
  /**
   * note some entries could be for deleting
   */
  private Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper = new HashMap<Object, ProvisioningGroupWrapper>();

  private Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper = new HashMap<Object, ProvisioningEntityWrapper>();

  private Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper = new HashMap<Object, ProvisioningMembershipWrapper>();
  
  private Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();

  /**
   * grouper uuids to retrieve from grouper and grouper sync
   */
  private GrouperIncrementalUuidsToRetrieveFromGrouper grouperIncrementalUuidsToRetrieveFromGrouper;

  /**
   * grouper uuids to retrieve from grouper and grouper sync
   * @return
   */
  public GrouperIncrementalUuidsToRetrieveFromGrouper getGrouperIncrementalUuidsToRetrieveFromGrouper() {
    if (this.grouperIncrementalUuidsToRetrieveFromGrouper == null) {
      this.grouperIncrementalUuidsToRetrieveFromGrouper = new GrouperIncrementalUuidsToRetrieveFromGrouper();
    }
    return grouperIncrementalUuidsToRetrieveFromGrouper;
  }

  /**
   * grouper uuids to retrieve from grouper and grouper sync
   * @param grouperIncrementalUuidsToRetrieveFromGrouper
   */
  public void setGrouperIncrementalUuidsToRetrieveFromGrouper(
      GrouperIncrementalUuidsToRetrieveFromGrouper grouperIncrementalUuidsToRetrieveFromGrouper) {
    this.grouperIncrementalUuidsToRetrieveFromGrouper = grouperIncrementalUuidsToRetrieveFromGrouper;
  }

  /**
   * grouper target objects to get from target for incremental sync
   */
  private TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest;
  
  /**
   * grouper target objects to get from target for incremental sync
   * @return target object
   */
  public TargetDaoRetrieveIncrementalDataRequest getTargetDaoRetrieveIncrementalDataRequest() {
    if (this.targetDaoRetrieveIncrementalDataRequest == null) {
      this.targetDaoRetrieveIncrementalDataRequest = new TargetDaoRetrieveIncrementalDataRequest();
    }
    return targetDaoRetrieveIncrementalDataRequest;
  }

  /**
   * grouper target objects to get from target for incremental sync
   * @param grouperIncrementalGroupTargetObjectsToRetrieveFromTarget
   */
  public void setTargetDaoRetrieveIncrementalDataRequest(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest) {
    this.targetDaoRetrieveIncrementalDataRequest = targetDaoRetrieveIncrementalDataRequest;
  }

  
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

  /**
   * grouper state of the data
   * @return
   */
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



  /**
   * grouper state of the data
   * @param grouperProvisioningObjects
   */
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


  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   * @return
   */
  public GrouperProvisioningLists getGrouperProvisioningObjectsMissing() {
    return grouperProvisioningObjectsMissing;
  }

  

}
