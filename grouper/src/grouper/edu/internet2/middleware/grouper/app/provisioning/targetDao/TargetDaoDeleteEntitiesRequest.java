package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoDeleteEntitiesRequest {

  private List<ProvisioningEntity> targetEntityDeletes;

  
  public List<ProvisioningEntity> getTargetEntityDeletes() {
    return targetEntityDeletes;
  }

  
  public void setTargetEntityDeletes(List<ProvisioningEntity> targetEntityDeletes) {
    this.targetEntityDeletes = targetEntityDeletes;
  }


  public TargetDaoDeleteEntitiesRequest(List<ProvisioningEntity> targetEntityDeletes) {
    this.targetEntityDeletes = targetEntityDeletes;
  }


  public TargetDaoDeleteEntitiesRequest() {
  }
  
}
