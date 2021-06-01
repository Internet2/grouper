package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveAllEntitiesResponse {
  
  private List<ProvisioningEntity> targetEntities;



  public TargetDaoRetrieveAllEntitiesResponse() {
    super();
  }

  public TargetDaoRetrieveAllEntitiesResponse(
      List<ProvisioningEntity> targetEntities) {
    super();
    this.targetEntities = targetEntities;
  }

  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(
      List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }

  
}
