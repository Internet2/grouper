package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveEntitiesByValuesResponse {
  
  private Map<Object, ProvisioningEntity> searchValueToTargetEntity;
  
  public Map<Object, ProvisioningEntity> getSearchValueToTargetEntity() {
    return searchValueToTargetEntity;
  }

  
  public void setSearchValueToTargetEntity(
      Map<Object, ProvisioningEntity> searchValueToTargetEntity) {
    this.searchValueToTargetEntity = searchValueToTargetEntity;
  }
  
  

}
