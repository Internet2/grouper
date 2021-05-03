package edu.internet2.middleware.grouper.app.provisioning;

/**
 * data retrieved from target
 * @author mchyzer
 *
 */
public class GrouperProvisioningDataTarget {

  public GrouperProvisioningDataTarget() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * target data from first pass data retrieve
   */
  private GrouperProvisioningLists targetProvisioningObjects = new GrouperProvisioningLists();
  
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

  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
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

  

}
