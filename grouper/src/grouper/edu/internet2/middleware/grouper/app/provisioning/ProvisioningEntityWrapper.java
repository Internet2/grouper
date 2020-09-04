package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class ProvisioningEntityWrapper {
  
  public ProvisioningEntityWrapper() {
    super();
  }

  /**
   * get grouper common entity if its there, if not, get target common entity
   * @return the common entity
   */
  public ProvisioningEntity getCommonEntity() {
    return GrouperUtil.defaultIfNull(this.grouperCommonEntity, this.targetCommonEntity);
  }

  private GrouperProvisioner grouperProvisioner;
  
  
  
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }



  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  public String toString() {
    return "EntityWrapper@" + Integer.toHexString(hashCode());
  }

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
