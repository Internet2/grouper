package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoSendEntityChangesToTargetRequest {

  public TargetDaoSendEntityChangesToTargetRequest() {
  }

  private List<ProvisioningEntity> targetEntityInserts;
  private List<ProvisioningEntity> targetEntityUpdates;
  private List<ProvisioningEntity> targetEntityDeletes;
  
  public List<ProvisioningEntity> getTargetEntityInserts() {
    return targetEntityInserts;
  }
  
  public void setTargetEntityInserts(List<ProvisioningEntity> targetEntityInserts) {
    this.targetEntityInserts = targetEntityInserts;
  }
  
  public List<ProvisioningEntity> getTargetEntityUpdates() {
    return targetEntityUpdates;
  }
  
  public void setTargetEntityUpdates(List<ProvisioningEntity> targetEntityUpdates) {
    this.targetEntityUpdates = targetEntityUpdates;
  }
  
  public List<ProvisioningEntity> getTargetEntityDeletes() {
    return targetEntityDeletes;
  }
  
  public void setTargetEntityDeletes(List<ProvisioningEntity> targetEntityDeletes) {
    this.targetEntityDeletes = targetEntityDeletes;
  }

  public TargetDaoSendEntityChangesToTargetRequest(
      List<ProvisioningEntity> targetEntityInserts,
      List<ProvisioningEntity> targetEntityUpdates,
      List<ProvisioningEntity> targetEntityDeletes) {
    super();
    this.targetEntityInserts = targetEntityInserts;
    this.targetEntityUpdates = targetEntityUpdates;
    this.targetEntityDeletes = targetEntityDeletes;
  }

  
}
