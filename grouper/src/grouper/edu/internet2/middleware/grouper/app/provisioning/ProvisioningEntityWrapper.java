package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class ProvisioningEntityWrapper {
  
  public ProvisioningEntityWrapper() {
    super();
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

  private ProvisioningEntity grouperProvisioningEntity;
  
  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   */
  private ProvisioningEntity grouperProvisioningEntityIncludeDelete;

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public ProvisioningEntity getGrouperProvisioningEntityIncludeDelete() {
    return grouperProvisioningEntityIncludeDelete;
  }

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param grouperProvisioningEntityIncludeDelete
   */
  public void setGrouperProvisioningEntityIncludeDelete(
      ProvisioningEntity grouperProvisioningEntityIncludeDelete) {
    this.grouperProvisioningEntityIncludeDelete = grouperProvisioningEntityIncludeDelete;
  }

  private ProvisioningEntity targetProvisioningEntity;

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   */
  private ProvisioningEntity grouperTargetEntityIncludeDelete;
  
  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public ProvisioningEntity getGrouperTargetEntityIncludeDelete() {
    return grouperTargetEntityIncludeDelete;
  }

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param targetProvisioningEntityIncludeDelete
   */
  public void setGrouperTargetEntityIncludeDelete(
      ProvisioningEntity targetProvisioningEntityIncludeDelete) {
    this.grouperTargetEntityIncludeDelete = targetProvisioningEntityIncludeDelete;
  }

  /**
   * grouper provisioning entity translated for create
   */
  private ProvisioningEntity grouperTargetEntityForCreate;

  
  
  /**
   * grouper provisioning entity translated for create
   * @return
   */
  public ProvisioningEntity getGrouperTargetEntityForCreate() {
    return grouperTargetEntityForCreate;
  }

  /**
   * grouper provisioning entity translated for create
   * @param grouperTargetEntityForCreate
   */
  public void setGrouperTargetEntityForCreate(
      ProvisioningEntity grouperTargetEntityForCreate) {
    this.grouperTargetEntityForCreate = grouperTargetEntityForCreate;
  }

  private ProvisioningEntity grouperTargetEntity;

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

  
  public ProvisioningEntity getGrouperTargetEntity() {
    return grouperTargetEntity;
  }

  
  public void setGrouperTargetEntity(ProvisioningEntity grouperTargetEntity) {
    this.grouperTargetEntity = grouperTargetEntity;
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
