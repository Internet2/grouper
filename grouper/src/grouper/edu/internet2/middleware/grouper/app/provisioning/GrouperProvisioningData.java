package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;

public class GrouperProvisioningData {

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

  
  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   * @return
   */
  public GrouperProvisioningLists getGrouperProvisioningObjectsMissing() {
    return grouperProvisioningObjectsMissing;
  }

  

}
