package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveMembershipsByEntitiesRequest {

  public TargetDaoRetrieveMembershipsByEntitiesRequest() {
  }

  private List<ProvisioningEntity> targetEntities;

  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }


  public TargetDaoRetrieveMembershipsByEntitiesRequest(List<ProvisioningEntity> targetEntities) {
    super();
    this.targetEntities = targetEntities;
  }
}
