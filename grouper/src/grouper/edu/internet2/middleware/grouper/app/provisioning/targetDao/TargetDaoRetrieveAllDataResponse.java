package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;

public class TargetDaoRetrieveAllDataResponse {
  private GrouperProvisioningLists targetData;

  
  public GrouperProvisioningLists getTargetData() {
    return targetData;
  }

  
  public void setTargetData(GrouperProvisioningLists targetData) {
    this.targetData = targetData;
  }


  public TargetDaoRetrieveAllDataResponse() {
  }


  public TargetDaoRetrieveAllDataResponse(
      GrouperProvisioningLists targetData) {
    this.targetData = targetData;
  }
  
  
  
}
