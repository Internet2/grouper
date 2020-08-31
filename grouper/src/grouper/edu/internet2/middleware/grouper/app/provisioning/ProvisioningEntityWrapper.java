package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class ProvisioningEntityWrapper {
  
  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   */
  private ProvisioningEntity commonProvisionToTargetEntity;



  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   * @return 
   */
  public ProvisioningEntity getCommonProvisionToTargetEntity() {
    return commonProvisionToTargetEntity;
  }

  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   * @param commonProvisionToTargetEntity
   */
  public void setCommonProvisionToTargetEntity(
      ProvisioningEntity commonProvisionToTargetEntity) {
    this.commonProvisionToTargetEntity = commonProvisionToTargetEntity;
  }


  private ProvisioningEntity grouperProvisioningEntity;
  
  private ProvisioningEntity grouperProvisioningEntityToDelete;
  
  public ProvisioningEntity getGrouperProvisioningEntityToDelete() {
    return grouperProvisioningEntityToDelete;
  }
  
  public void setGrouperProvisioningEntityToDelete(
      ProvisioningEntity grouperProvisioningEntityToDelete) {
    this.grouperProvisioningEntityToDelete = grouperProvisioningEntityToDelete;
  }


  private ProvisioningEntity targetProvisioningEntity;
  
  private ProvisioningEntity grouperCommonEntity;

  private ProvisioningEntity targetCommonEntity;
  
  private Object targetNativeEntity;
  
  private GcGrouperSyncMember gcGrouperSyncMember;

  
  public ProvisioningEntity getGrouperProvisioningEntity() {
    return grouperProvisioningEntity;
  }

  
  public void setGrouperProvisioningEntity(ProvisioningEntity grouperProvisioningEntity) {
    this.grouperProvisioningEntity = grouperProvisioningEntity;
  }

  
  public ProvisioningEntity getTargetProvisioningEntity() {
    return targetProvisioningEntity;
  }

  
  public void setTargetProvisioningEntity(ProvisioningEntity targetProvisioningEntity) {
    this.targetProvisioningEntity = targetProvisioningEntity;
  }

  
  public ProvisioningEntity getGrouperCommonEntity() {
    return grouperCommonEntity;
  }

  
  public void setGrouperCommonEntity(ProvisioningEntity grouperCommonEntity) {
    this.grouperCommonEntity = grouperCommonEntity;
  }

  
  public ProvisioningEntity getTargetCommonEntity() {
    return targetCommonEntity;
  }

  
  public void setTargetCommonEntity(ProvisioningEntity targetCommonEntity) {
    this.targetCommonEntity = targetCommonEntity;
  }

  
  public Object getTargetNativeEntity() {
    return targetNativeEntity;
  }

  
  public void setTargetNativeEntity(Object targetNativeEntity) {
    this.targetNativeEntity = targetNativeEntity;
  }

  
  public GcGrouperSyncMember getGcGrouperSyncMember() {
    return gcGrouperSyncMember;
  }

  
  public void setGcGrouperSyncMember(GcGrouperSyncMember gcGrouperSyncMember) {
    this.gcGrouperSyncMember = gcGrouperSyncMember;
  }
  
  

}
