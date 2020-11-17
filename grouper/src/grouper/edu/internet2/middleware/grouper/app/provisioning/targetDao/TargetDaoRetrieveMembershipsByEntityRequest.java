package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveMembershipsByEntityRequest {

  private ProvisioningEntity targetEntity;
  
  public ProvisioningEntity getTargetEntity() {
    return targetEntity;
  }

  
  public void setTargetEntity(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }


  public TargetDaoRetrieveMembershipsByEntityRequest(
      ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }


  public TargetDaoRetrieveMembershipsByEntityRequest() {
  }
  
  
  
}
