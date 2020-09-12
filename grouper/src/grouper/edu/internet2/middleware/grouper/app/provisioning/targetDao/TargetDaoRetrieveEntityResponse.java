package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveEntityResponse {

  
  
  public TargetDaoRetrieveEntityResponse(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }


  public TargetDaoRetrieveEntityResponse() {
  }


  public ProvisioningEntity getTargetEntity() {
    return targetEntity;
  }

  
  public void setTargetEntity(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }

  private ProvisioningEntity targetEntity;
}
