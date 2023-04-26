package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveAllDataResponse {
  private GrouperProvisioningLists targetData;

  
  public GrouperProvisioningLists getTargetData() {
    return targetData;
  }

  
  public void setTargetData(GrouperProvisioningLists targetData) {
    this.targetData = targetData;
  }


  public TargetDaoRetrieveAllDataResponse() {
  }


  public TargetDaoRetrieveAllDataResponse(
      GrouperProvisioningLists targetData) {
    this.targetData = targetData;
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

  
}
