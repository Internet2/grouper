package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoUpdateEntityRequest {

  public TargetDaoUpdateEntityRequest() {
    // TODO Auto-generated constructor stub
  }
  private ProvisioningEntity targetEntity;
  
  public ProvisioningEntity getTargetEntity() {
    return targetEntity;
  }
  
  public void setTargetEntity(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }

  public TargetDaoUpdateEntityRequest(ProvisioningEntity targetEntity) {
    super();
    this.targetEntity = targetEntity;
  }
  
}
