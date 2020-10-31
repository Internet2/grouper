package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;

/**
 * contains changes for the target
 * @author mchyzer
 *
 */
public class GrouperProvisioningDataChanges {

  public GrouperProvisioningDataChanges() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  private GrouperProvisioningLists targetObjectInserts = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectUpdates = new GrouperProvisioningLists();
  
  private GrouperProvisioningLists targetObjectDeletes = new GrouperProvisioningLists();

  /**
   * insert translation
   */
  private GrouperProvisioningLists grouperTargetObjectsMissing = new GrouperProvisioningLists();
  
  public GcGrouperSync getGcGrouperSync() {
    return this.getGrouperProvisioner().getGcGrouperSync();
  }
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  public GrouperProvisioningLists getTargetObjectInserts() {
    return targetObjectInserts;
  }

  
  public GrouperProvisioningLists getTargetObjectUpdates() {
    return targetObjectUpdates;
  }

  
  public GrouperProvisioningLists getTargetObjectDeletes() {
    return targetObjectDeletes;
  }

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

  

}
