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
  
  private ProvisioningEntity targetProvisioningEntity;

  private ProvisioningEntity grouperTargetEntity;

  private Object targetNativeEntity;
  
  private GcGrouperSyncMember gcGrouperSyncMember;

  /**
   * if this is for a create in target
   */
  private boolean create;

  /**
   * if the grrouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   */
  private boolean delete;

  
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

  /**
   * if this is for a create in target
   * @return
   */
  public boolean isCreate() {
    return create;
  }

  /**
   * if the grrouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public boolean isDelete() {
    return delete;
  }

  /**
   * if this is for a create in target
   * @param create
   */
  public void setCreate(boolean create) {
    this.create = create;
  }

  /**
   * if the grrouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param delete
   */
  public void setDelete(boolean delete) {
    this.delete = delete;
  }
  
  

}
