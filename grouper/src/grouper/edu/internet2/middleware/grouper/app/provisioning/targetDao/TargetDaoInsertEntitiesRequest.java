package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoInsertEntitiesRequest {

  private List<ProvisioningEntity> targetEntityInserts;

  
  public List<ProvisioningEntity> getTargetEntityInserts() {
    return targetEntityInserts;
  }

  
  public void setTargetEntityInserts(List<ProvisioningEntity> targetEntityInserts) {
    this.targetEntityInserts = targetEntityInserts;
  }


  public TargetDaoInsertEntitiesRequest() {
  }


  public TargetDaoInsertEntitiesRequest(List<ProvisioningEntity> targetEntityInserts) {
    this.targetEntityInserts = targetEntityInserts;
  }
  
  
}
