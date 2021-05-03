package edu.internet2.middleware.grouper.app.provisioning;

public class GrouperProvisioningDataGrouperTarget {

  public GrouperProvisioningDataGrouperTarget() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

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

  public GrouperProvisioningLists getGrouperTargetObjects() {
    return grouperTargetObjects;
  }

  
  public void setGrouperTargetObjects(GrouperProvisioningLists grouperCommonObjects) {
    this.grouperTargetObjects = grouperCommonObjects;
  }

  

}
