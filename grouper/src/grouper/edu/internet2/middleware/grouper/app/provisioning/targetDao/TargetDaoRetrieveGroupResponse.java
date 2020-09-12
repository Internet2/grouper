package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveGroupResponse {

  public TargetDaoRetrieveGroupResponse() {
  }

  private ProvisioningGroup targetGroup;

  
  public ProvisioningGroup getTargetGroup() {
    return targetGroup;
  }

  
  public void setTargetGroup(ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }
  
  
}
