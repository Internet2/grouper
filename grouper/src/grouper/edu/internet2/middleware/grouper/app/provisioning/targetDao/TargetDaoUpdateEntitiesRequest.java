package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoUpdateEntitiesRequest {

  private List<ProvisioningEntity> targetEntities;

  
  public TargetDaoUpdateEntitiesRequest() {
    super();
    // TODO Auto-generated constructor stub
  }


  public TargetDaoUpdateEntitiesRequest(List<ProvisioningEntity> targetEntityInserts) {
    super();
    this.targetEntities = targetEntityInserts;
  }


  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(List<ProvisioningEntity> targetEntityUpdates) {
    this.targetEntities = targetEntityUpdates;
  }
  
}
