package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

/**
 * 
 * @author mchyzer-local
 *
 */
public class TargetDaoDeleteGroupsRequest {
  private List<ProvisioningGroup> targetGroupDeletes;

  
  public List<ProvisioningGroup> getTargetGroupDeletes() {
    return targetGroupDeletes;
  }

  
  public void setTargetGroupDeletes(List<ProvisioningGroup> targetGroupDeletes) {
    this.targetGroupDeletes = targetGroupDeletes;
  }


  public TargetDaoDeleteGroupsRequest() {
  }


  public TargetDaoDeleteGroupsRequest(List<ProvisioningGroup> targetGroupDeletes) {
    this.targetGroupDeletes = targetGroupDeletes;
  }
  
  
}
