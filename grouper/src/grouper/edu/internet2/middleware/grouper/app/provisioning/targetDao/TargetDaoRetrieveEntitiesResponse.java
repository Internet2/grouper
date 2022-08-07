package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoRetrieveEntitiesResponse {

  public TargetDaoRetrieveEntitiesResponse() {
  }

  /**
   * map of retrieved entity to target native entity, optional, only if the target native entity is needed later on
   */
  private Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity = new HashMap<ProvisioningEntity, Object>();

  
  /**
   * map of retrieved entity to target native entity, optional, only if the target native entity is needed later on
   * @return
   */
  public Map<ProvisioningEntity, Object> getTargetEntityToTargetNativeEntity() {
    return targetEntityToTargetNativeEntity;
  }

  /**
   * map of retrieved entity to target native entity, optional, only if the target native entity is needed later on
   * @param targetEntityToTargetNativeEntity
   */
  public void setTargetEntityToTargetNativeEntity(Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity) {
    this.targetEntityToTargetNativeEntity = targetEntityToTargetNativeEntity;
  }

  private List<ProvisioningEntity> targetEntities;

  
  public List<ProvisioningEntity> getTargetEntities() {
    return targetEntities;
  }

  
  public void setTargetEntities(List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }



  public TargetDaoRetrieveEntitiesResponse(List<ProvisioningEntity> targetEntities) {
    this.targetEntities = targetEntities;
  }
  
  
  
}
