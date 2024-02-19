package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class ProvisioningEntityWrapper extends ProvisioningUpdatableWrapper {

  private ProvisioningStateEntity provisioningStateEntity = new ProvisioningStateEntity();

  
  public ProvisioningStateEntity getProvisioningStateEntity() {
    return provisioningStateEntity;
  }
  
  public boolean isInGroup(String groupName) {
    if (StringUtils.isBlank(this.getMemberId())) {
      return false;
    }
    return this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator()
        .isInGroup(groupName, this.getMemberId());
  }

  public boolean isHasPrivilege(String groupName, String privilegeName) {
    if (StringUtils.isBlank(this.getMemberId())) {
      return false;
    }
    return this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator()
        .isHasPrivilege(groupName, privilegeName, this.getMemberId());
  }

  /**
   * grouper member id
   */
  private String memberId;
  
  
  
  
  public String getMemberId() {
    return memberId;
  }




  
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }


  /**
   * sync member id
   */
  private String syncMemberId;

  
  
  /**
   * sync member id
   * @return
   */
  public String getSyncMemberId() {
    return syncMemberId;
  }




  /**
   * sync member id
   * @param syncMemberId
   */
  public void setSyncMemberId(String syncMemberId) {
    this.syncMemberId = syncMemberId;
  }




  public ProvisioningEntityWrapper() {
    super();
    this.provisioningStateEntity.setProvisioningEntityWrapper(this);
  }

  public String toString() {
    return "EntityWrapper@" + Integer.toHexString(hashCode());
  }

  private ProvisioningEntity grouperProvisioningEntity;
  
  private ProvisioningEntity targetProvisioningEntity;

  private ProvisioningEntity grouperTargetEntity;

  private Object targetNativeEntity;
  
  private GcGrouperSyncMember gcGrouperSyncMember;

  public ProvisioningEntity getGrouperProvisioningEntity() {
    return grouperProvisioningEntity;
  }

  
  
  public void setGrouperProvisioningEntity(ProvisioningEntity grouperProvisioningEntity) {
    
    if (grouperProvisioningEntity == this.grouperProvisioningEntity) {
      return;
    }
    
    ProvisioningEntity oldGrouperProvisioningEntity = this.grouperProvisioningEntity;
    ProvisioningEntityWrapper oldProvisioningEntityWrapper = oldGrouperProvisioningEntity == null ? null : oldGrouperProvisioningEntity.getProvisioningEntityWrapper();

    this.grouperProvisioningEntity = grouperProvisioningEntity;
    
    if (this.grouperProvisioningEntity != null) {
      this.grouperProvisioningEntity.setProvisioningEntityWrapper(this);
    }

    if (oldGrouperProvisioningEntity != null) {
      oldGrouperProvisioningEntity.setProvisioningEntityWrapper(null);
    }
    if (oldProvisioningEntityWrapper != null && oldProvisioningEntityWrapper != this) {
      oldProvisioningEntityWrapper.grouperProvisioningEntity = null;
    }
    this.calculateMemberId();

  }

  
  private void calculateMemberId() {
    this.memberId = null;
    if (this.grouperProvisioningEntity != null) {
      this.memberId = this.grouperProvisioningEntity.getId();
    } else if (this.gcGrouperSyncMember != null) {
      this.memberId = this.gcGrouperSyncMember.getMemberId();
    }
  }

  public ProvisioningEntity getTargetProvisioningEntity() {
    return targetProvisioningEntity;
  }

  
  public void setTargetProvisioningEntity(ProvisioningEntity targetProvisioningEntity) {
    if (this.targetProvisioningEntity == targetProvisioningEntity) {
      return;
    }
    
    ProvisioningEntity oldTargetProvisioningEntity = this.targetProvisioningEntity;
    ProvisioningEntityWrapper oldProvisioningEntityWrapper = oldTargetProvisioningEntity == null ? null : oldTargetProvisioningEntity.getProvisioningEntityWrapper();

    this.targetProvisioningEntity = targetProvisioningEntity;
    
    if (this.targetProvisioningEntity != null) {
      ProvisioningEntityWrapper newTargetEntityOldWrapper = this.targetProvisioningEntity.getProvisioningEntityWrapper();
      
      this.targetProvisioningEntity.setProvisioningEntityWrapper(this);
      
      if (newTargetEntityOldWrapper != null && newTargetEntityOldWrapper.getProvisioningStateEntity().isSelectResultProcessed()) {
        this.getProvisioningStateEntity().setSelectResultProcessed(true);
      }
      if (newTargetEntityOldWrapper != null && newTargetEntityOldWrapper.getProvisioningStateEntity().isSelectAllMembershipResultProcessed()) {
        this.getProvisioningStateEntity().setSelectAllMembershipResultProcessed(true);
      }

    }

    if (oldTargetProvisioningEntity != null) {
      oldTargetProvisioningEntity.setProvisioningEntityWrapper(null);
    }
    if (oldProvisioningEntityWrapper != null && oldProvisioningEntityWrapper != this) {
      oldProvisioningEntityWrapper.targetProvisioningEntity = null;
    }

  }

  
  public ProvisioningEntity getGrouperTargetEntity() {
    return grouperTargetEntity;
  }

  
  public void setGrouperTargetEntity(ProvisioningEntity grouperTargetEntity) {
    if (this.grouperTargetEntity == grouperTargetEntity) {
      return;
    }
    ProvisioningEntity oldGrouperTargetEntity = this.grouperTargetEntity;
    ProvisioningEntityWrapper oldProvisioningEntityWrapper = oldGrouperTargetEntity == null ? null : oldGrouperTargetEntity.getProvisioningEntityWrapper();

    this.grouperTargetEntity = grouperTargetEntity;
    
    if (this.grouperTargetEntity != null) {
      this.grouperTargetEntity.setProvisioningEntityWrapper(this);
    }

    if (oldGrouperTargetEntity != null && oldGrouperTargetEntity != this.targetProvisioningEntity) {
      oldGrouperTargetEntity.setProvisioningEntityWrapper(null);
    }
    if (oldProvisioningEntityWrapper != null && oldProvisioningEntityWrapper != this) {
      oldProvisioningEntityWrapper.grouperTargetEntity = null;
    }
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
    if (this.gcGrouperSyncMember != null) {
      this.syncMemberId = this.getGcGrouperSyncMember().getId();
    }
    this.calculateMemberId();
  }

  @Override
  public String objectTypeName() {
    return "entity";
  }
  
  public String toStringForError() {
    
    if (this.grouperTargetEntity != null) {
      return "grouperTargetEntity: " + this.grouperTargetEntity;
    }

    if (this.grouperProvisioningEntity != null) {
      return "grouperProvisioningEntity: " + this.grouperProvisioningEntity;
    }

    if (this.targetProvisioningEntity != null) {
      return "targetProvisioningEntity: " + this.targetProvisioningEntity;
    }
    
    if (this.provisioningStateEntity != null) {
      return "provisioningStateEntity: " + this.provisioningStateEntity;
    }

    return this.toString();
  }

  public String toStringForErrorVerbose() {
    
    StringBuilder result = new StringBuilder();
    
    if (this.grouperTargetEntity != null) {
      result.append("grouperTargetEntity: " + this.grouperTargetEntity + ", ");
    }

    if (this.grouperProvisioningEntity != null) {
      result.append("grouperProvisioningEntity: " + this.grouperProvisioningEntity + ", ");
    }

    if (this.targetProvisioningEntity != null) {
      result.append("targetProvisioningEntity: " + this.targetProvisioningEntity + ", ");
    }
    
    if (this.provisioningStateEntity != null) {
      result.append("provisioningStateEntity: " + this.provisioningStateEntity + ", ");
    }

    return this.toString();
  }


}
