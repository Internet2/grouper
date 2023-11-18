package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;

public class TargetDaoRetrieveEntityResponse {

  public TargetDaoRetrieveEntityResponse() {
  }
  
  /**
   * some native representation of the target entity, only pass around if needed
   */
  private Object targetNativeEntity;
  
  
  /**
   * some native representation of the target entity, only pass around if needed
   * @return
   */
  public Object getTargetNativeEntity() {
    return targetNativeEntity;
  }

  /**
   * some native representation of the target entity, only pass around if needed
   * @param targetNativeEntity
   */
  public void setTargetNativeEntity(Object targetNativeEntity) {
    this.targetNativeEntity = targetNativeEntity;
  }


  public TargetDaoRetrieveEntityResponse(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }


  public ProvisioningEntity getTargetEntity() {
    return targetEntity;
  }

  
  public void setTargetEntity(ProvisioningEntity targetEntity) {
    this.targetEntity = targetEntity;
  }

  private ProvisioningEntity targetEntity;
}
