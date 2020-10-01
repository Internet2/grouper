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

  public boolean wasWorkDone() {
    if (this.targetObjectInserts.wasWorkDone()) {
      return true;
    }
    if (this.targetObjectUpdates.wasWorkDone()) {
      return true;
    }
    if (this.targetObjectDeletes.wasWorkDone()) {
      return true;
    }
    // maybe group or entity inserts
    if (this.grouperTargetObjectsMissing.wasWorkDone()) {
      return true;
    }
    return false;
  }
  

  public GrouperProvisioningData() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * grouper state of the data at first retrieve
   */
  private GrouperProvisioningLists grouperProvisioningObjects = new GrouperProvisioningLists();

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   */
  private GrouperProvisioningLists grouperProvisioningObjectsMissing = new GrouperProvisioningLists();

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   */
  private GrouperProvisioningLists grouperProvisioningObjectsCreated = new GrouperProvisioningLists();

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   * @return
   */
  public GrouperProvisioningLists getGrouperProvisioningObjectsCreated() {
    return grouperProvisioningObjectsCreated;
  }

  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   * @param grouperProvisioningObjectsCreatedPass1
   */
  public void setGrouperProvisioningObjectsCreated(
      GrouperProvisioningLists grouperProvisioningObjectsCreatedPass1) {
    this.grouperProvisioningObjectsCreated = grouperProvisioningObjectsCreatedPass1;
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

  /**
   * target data from first pass data retrieve
   */
  private GrouperProvisioningLists targetProvisioningObjects = new GrouperProvisioningLists();
  
  /**
   * normal translation (not for insert)
   */
  private GrouperProvisioningLists grouperTargetObjects = new GrouperProvisioningLists();

  /**
   * objects which were changed in link
   */
  private GrouperProvisioningLists grouperTargetObjectsChangedInLink = new GrouperProvisioningLists();

  /**
   * objects which were changed in link
   * @return
   */
  public GrouperProvisioningLists getGrouperTargetObjectsChangedInLink() {
    return grouperTargetObjectsChangedInLink;
  }

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
   * insert translation
   */
  private GrouperProvisioningLists grouperTargetObjectsMissing = new GrouperProvisioningLists();

  /**
   * insert translation
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
  private Map<Object, ProvisioningGroupWrapper> groupTargetIdToProvisioningGroupWrapper = new HashMap<Object, ProvisioningGroupWrapper>();

  private Map<Object, ProvisioningEntityWrapper> entityTargetIdToProvisioningEntityWrapper = new HashMap<Object, ProvisioningEntityWrapper>();

  private Map<Object, ProvisioningMembershipWrapper> membershipTargetIdToProvisioningMembershipWrapper = new HashMap<Object, ProvisioningMembershipWrapper>();
  
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





  public void setGroupTargetIdToProvisioningGroupWrapper(
      Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper) {
    this.groupTargetIdToProvisioningGroupWrapper = targetGroupIdToProvisioningGroupWrapper;
  }




  
  public void setEntityTargetIdToProvisioningEntityWrapper(
      Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper) {
    this.entityTargetIdToProvisioningEntityWrapper = targetEntityIdToProvisioningEntityWrapper;
  }




  
  public void setMembershipTargetIdToProvisioningMembershipWrapper(
      Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper) {
    this.membershipTargetIdToProvisioningMembershipWrapper = targetMembershipIdToProvisioningMembershipWrapper;
  }




  public GcGrouperSync getGcGrouperSync() {
    return this.getGrouperProvisioner().getGcGrouperSync();
  }
  
  public Map<Object, ProvisioningGroupWrapper> getGroupTargetIdToProvisioningGroupWrapper() {
    return groupTargetIdToProvisioningGroupWrapper;
  }




  
  public Map<Object, ProvisioningEntityWrapper> getEntityTargetIdToProvisioningEntityWrapper() {
    return entityTargetIdToProvisioningEntityWrapper;
  }




  
  public Map<Object, ProvisioningMembershipWrapper> getMembershipTargetIdToProvisioningMembershipWrapper() {
    return membershipTargetIdToProvisioningMembershipWrapper;
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
   * grouper state of the data at first retrieve
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
   * grouper state of the data at first retrieve
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

  /**
   * target data from first pass data retrieve
   * @return
   */
  public GrouperProvisioningLists getTargetProvisioningObjects() {
    return targetProvisioningObjects;
  }

  /**
   * target data from first pass data retrieve
   * @param targetProvisioningObjects
   */
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
