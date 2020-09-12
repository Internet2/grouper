package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;

public class TargetDaoRetrieveIncrementalDataResponse {
  private GrouperProvisioningLists targetProvisioningData;

  
  public GrouperProvisioningLists getTargetProvisioningData() {
    return targetProvisioningData;
  }

  
  public void setTargetProvisioningData(GrouperProvisioningLists targetProvisioningData) {
    this.targetProvisioningData = targetProvisioningData;
  }


  public TargetDaoRetrieveIncrementalDataResponse() {
  }

  public TargetDaoRetrieveIncrementalDataResponse(
      GrouperProvisioningLists targetProvisioningData) {
    this.targetProvisioningData = targetProvisioningData;
  }

}
