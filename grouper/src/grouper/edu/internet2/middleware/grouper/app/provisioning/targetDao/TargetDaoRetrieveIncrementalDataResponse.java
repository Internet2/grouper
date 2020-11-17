package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;

public class TargetDaoRetrieveIncrementalDataResponse {
  private GrouperProvisioningLists targetData;

  
  public GrouperProvisioningLists getTargetData() {
    return targetData;
  }

  
  public void setTargetData(GrouperProvisioningLists targetData) {
    this.targetData = targetData;
  }


  public TargetDaoRetrieveIncrementalDataResponse() {
  }

  public TargetDaoRetrieveIncrementalDataResponse(
      GrouperProvisioningLists targetData) {
    this.targetData = targetData;
  }

}
