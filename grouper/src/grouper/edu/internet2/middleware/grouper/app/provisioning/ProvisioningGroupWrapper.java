package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

public class ProvisioningGroupWrapper {
  
  /**
   * if this object should not be provisioned because there is an error, list it here
   */
  private GcGrouperSyncErrorCode errorCode;
  
  /**
   * if this object should not be provisioned because there is an error, list it here
   * @return
   */
  public GcGrouperSyncErrorCode getErrorCode() {
    return errorCode;
  }

  /**
   * if this object should not be provisioned because there is an error, list it here
   * @param errorCode
   */
  public void setErrorCode(GcGrouperSyncErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  /**
   * if incremental and recalc
   */
  private boolean recalc;
  
  /**
   * if incremental and recalc
   * @return
   */
  public boolean isRecalc() {
    return recalc;
  }

  /**
   * if incremental and recalc
   * @param recalc
   */
  public void setRecalc(boolean recalc) {
    this.recalc = recalc;
  }

  /**
   * if this is incremental, and syncing memberships for this group
   */
  private boolean incrementalSyncMemberships;
  
  /**
   * if this is incremental, and syncing memberships for this group
   * @return
   */
  public boolean isIncrementalSyncMemberships() {
    return incrementalSyncMemberships;
  }

  /**
   * if this is incremental, and syncing memberships for this group
   * @param incrementalSyncMemberships1
   */
  public void setIncrementalSyncMemberships(boolean incrementalSyncMemberships1) {
    this.incrementalSyncMemberships = incrementalSyncMemberships1;
  }

  private Object matchingId = null;
  
  
  public Object getMatchingId() {
    return matchingId;
  }



  
  public void setMatchingId(Object matchingId) {
    this.matchingId = matchingId;
  }

  private String groupId;
  
  
  
  
  public String getGroupId() {
    return groupId;
  }




  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  private String syncGroupId;
  
  


  
  public String getSyncGroupId() {
    return syncGroupId;
  }




  
  public void setSyncGroupId(String syncGroupId) {
    this.syncGroupId = syncGroupId;
  }




  public ProvisioningGroupWrapper() {
    super();
  }

  private GrouperProvisioner grouperProvisioner;
  
  
  
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }



  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  private ProvisioningGroup grouperProvisioningGroup;

  /**
   * if the grrouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   */
  private boolean delete;
  
  
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
   * if the grrouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param delete
   */
  public void setDelete(boolean delete) {
    this.delete = delete;
  }

  private ProvisioningGroup targetProvisioningGroup;
  
  /**
   * grouper side translated for target
   */
  private ProvisioningGroup grouperTargetGroup;

  /**
   * if this is for a create in target
   */
  private boolean create;
  
  /**
   * if this is for a create in target
   * @return
   */
  public boolean isCreate() {
    return create;
  }

  /**
   * if this is for a create in target
   * @param create
   */
  public void setCreate(boolean create) {
    this.create = create;
  }

  private Object targetNativeGroup;
  
  private GcGrouperSyncGroup gcGrouperSyncGroup;

  
  public ProvisioningGroup getGrouperProvisioningGroup() {
    return grouperProvisioningGroup;
  }

  
  public void setGrouperProvisioningGroup(ProvisioningGroup grouperProvisioningGroup) {
    this.grouperProvisioningGroup = grouperProvisioningGroup;
    if (this.grouperProvisioningGroup!=null) {
      this.groupId = this.grouperProvisioningGroup.getId();
      if (this != this.grouperProvisioningGroup.getProvisioningGroupWrapper()) {
        if (this.grouperProvisioningGroup.getProvisioningGroupWrapper() != null) {
          this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().remove(this.grouperProvisioningGroup.getProvisioningGroupWrapper());
        }
        this.grouperProvisioningGroup.setProvisioningGroupWrapper(this);
      }
    }

  }

  
  public ProvisioningGroup getTargetProvisioningGroup() {
    return targetProvisioningGroup;
  }

  
  public void setTargetProvisioningGroup(ProvisioningGroup targetProvisioningGroup) {
    this.targetProvisioningGroup = targetProvisioningGroup;
    if (this.targetProvisioningGroup != null && this != this.targetProvisioningGroup.getProvisioningGroupWrapper()) {
      if (this.targetProvisioningGroup.getProvisioningGroupWrapper() != null) {
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().remove(this.targetProvisioningGroup.getProvisioningGroupWrapper());
      }
      this.targetProvisioningGroup.setProvisioningGroupWrapper(this);
    }
  }

  
  public ProvisioningGroup getGrouperTargetGroup() {
    return grouperTargetGroup;
  }

  
  public void setGrouperTargetGroup(ProvisioningGroup grouperTargetGroup) {
    this.grouperTargetGroup = grouperTargetGroup;
    if (this.grouperTargetGroup != null && this != this.grouperTargetGroup.getProvisioningGroupWrapper()) {
      if (this.grouperTargetGroup.getProvisioningGroupWrapper() != null) {
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().remove(this.grouperTargetGroup.getProvisioningGroupWrapper());
      }
      this.grouperTargetGroup.setProvisioningGroupWrapper(this);
    }
  }

  
  public Object getTargetNativeGroup() {
    return targetNativeGroup;
  }

  
  public void setTargetNativeGroup(Object targetNativeGroup) {
    this.targetNativeGroup = targetNativeGroup;
  }

  
  public GcGrouperSyncGroup getGcGrouperSyncGroup() {
    return gcGrouperSyncGroup;
  }

  
  public void setGcGrouperSyncGroup(GcGrouperSyncGroup gcGrouperSyncGroup) {
    this.gcGrouperSyncGroup = gcGrouperSyncGroup;
    if (this.gcGrouperSyncGroup != null) {
      this.syncGroupId = this.getGcGrouperSyncGroup().getId();
    }

  }
  
  public String toString() {
    return "GroupWrapper@" + Integer.toHexString(hashCode());
  }
}
