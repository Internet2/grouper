package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoSendGroupChangesToTargetRequest {

  public TargetDaoSendGroupChangesToTargetRequest() {
    
  }

  private List<ProvisioningGroup> targetGroupInserts;
  private List<ProvisioningGroup> targetGroupUpdates;
  private List<ProvisioningGroup> targetGroupDeletes;
  
  public List<ProvisioningGroup> getTargetGroupInserts() {
    return targetGroupInserts;
  }
  
  public void setTargetGroupInserts(List<ProvisioningGroup> targetGroupInserts) {
    this.targetGroupInserts = targetGroupInserts;
  }
  
  public List<ProvisioningGroup> getTargetGroupUpdates() {
    return targetGroupUpdates;
  }
  
  public void setTargetGroupUpdates(List<ProvisioningGroup> targetGroupUpdates) {
    this.targetGroupUpdates = targetGroupUpdates;
  }
  
  public List<ProvisioningGroup> getTargetGroupDeletes() {
    return targetGroupDeletes;
  }
  
  public void setTargetGroupDeletes(List<ProvisioningGroup> targetGroupDeletes) {
    this.targetGroupDeletes = targetGroupDeletes;
  }

  public TargetDaoSendGroupChangesToTargetRequest(
      List<ProvisioningGroup> targetGroupInserts,
      List<ProvisioningGroup> targetGroupUpdates,
      List<ProvisioningGroup> targetGroupDeletes) {
    super();
    this.targetGroupInserts = targetGroupInserts;
    this.targetGroupUpdates = targetGroupUpdates;
    this.targetGroupDeletes = targetGroupDeletes;
  }

}
