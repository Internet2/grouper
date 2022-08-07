package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveGroupResponse {

  public TargetDaoRetrieveGroupResponse() {
  }

  /**
   * some native representation of the target group, only pass around if needed
   */
  private Object targetNativeGroup;
  
  
  /**
   * some native representation of the target group, only pass around if needed
   * @return
   */
  public Object getTargetNativeGroup() {
    return targetNativeGroup;
  }

  /**
   * some native representation of the target group, only pass around if needed
   * @param targetNativeGroup
   */
  public void setTargetNativeGroup(Object targetNativeGroup) {
    this.targetNativeGroup = targetNativeGroup;
  }

  private ProvisioningGroup targetGroup;

  
  public ProvisioningGroup getTargetGroup() {
    return targetGroup;
  }

  
  public void setTargetGroup(ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }


  public TargetDaoRetrieveGroupResponse(ProvisioningGroup targetGroup) {
    super();
    this.targetGroup = targetGroup;
  }
  
  
}
