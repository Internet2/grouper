package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;

public class GrouperProvisioningDataGrouper {

  public GrouperProvisioningDataGrouper() {
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

  
  /**
   * objects that are in grouper but there is no sync object or there is missing link data
   * @return
   */
  public GrouperProvisioningLists getGrouperProvisioningObjectsMissing() {
    return grouperProvisioningObjectsMissing;
  }

  

}
