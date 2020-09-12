package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveAllEntitiesResponse {
  
  private List<ProvisioningEntity> targetEntities;



  public TargetDaoRetrieveAllEntitiesResponse() {
    super();
  }

  public TargetDaoRetrieveAllEntitiesResponse(
      List<ProvisioningEntity> targetProvisioningEntities) {
    super();
    this.targetEntities = targetProvisioningEntities;
  }

  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(
      List<ProvisioningEntity> targetProvisioningEntities) {
    this.targetEntities = targetProvisioningEntities;
  }

  
}
