package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoDeleteEntityRequest {

  private ProvisioningEntity targetEntity;

  
  public ProvisioningEntity getTargetEntity() {
    return targetEntity;
  }

  
  public void setTargetEntity(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }


  public TargetDaoDeleteEntityRequest() {
  }


  public TargetDaoDeleteEntityRequest(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }
  
  
  
}
