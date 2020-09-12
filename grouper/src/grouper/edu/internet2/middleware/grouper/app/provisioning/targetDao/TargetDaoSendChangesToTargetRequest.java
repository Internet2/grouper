package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;

public class TargetDaoSendChangesToTargetRequest {

  public TargetDaoSendChangesToTargetRequest() {
    
  }

  private GrouperProvisioningLists targetObjectDeletes;
  
  private GrouperProvisioningLists targetObjectInserts;

  private GrouperProvisioningLists targetObjectUpdates;

  
  public GrouperProvisioningLists getTargetObjectDeletes() {
    return targetObjectDeletes;
  }

  
  public void setTargetObjectDeletes(GrouperProvisioningLists targetObjectDeletes) {
    this.targetObjectDeletes = targetObjectDeletes;
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


  public TargetDaoSendChangesToTargetRequest(
      GrouperProvisioningLists targetObjectInserts,
      GrouperProvisioningLists targetObjectUpdates, GrouperProvisioningLists targetObjectDeletes) {
    this.targetObjectDeletes = targetObjectDeletes;
    this.targetObjectInserts = targetObjectInserts;
    this.targetObjectUpdates = targetObjectUpdates;
  }
  
}
