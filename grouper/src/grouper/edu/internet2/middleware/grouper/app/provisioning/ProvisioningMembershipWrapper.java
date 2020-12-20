package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class ProvisioningMembershipWrapper {
  
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
   * if this is an incremental action without recalc, then this is the action that occurred in Grouper
   */
  private GrouperIncrementalDataAction grouperIncrementalDataAction;
  
  /**
   * if this is an incremental action without recalc, then this is the action that occurred in Grouper
   * @return
   */
  public GrouperIncrementalDataAction getGrouperIncrementalDataAction() {
    return grouperIncrementalDataAction;
  }

  /**
   * if this is an incremental action without recalc, then this is the action that occurred in Grouper
   * @param grouperIncrementalDataAction
   */
  public void setGrouperIncrementalDataAction(
      GrouperIncrementalDataAction grouperIncrementalDataAction) {
    this.grouperIncrementalDataAction = grouperIncrementalDataAction;
  }

  private MultiKey groupIdMemberId = null;
  
  private MultiKey syncGroupIdSyncMemberId = null;
  
  private Object matchingId = null;
  
  
  public Object getMatchingId() {
    return matchingId;
  }



  
  public void setMatchingId(Object matchingId) {
    this.matchingId = matchingId;
  }



  public MultiKey getGroupIdMemberId() {
    return groupIdMemberId;
  }


  
  public void setGroupIdMemberId(MultiKey groupIdMemberId) {
    this.groupIdMemberId = groupIdMemberId;
  }


  
  public MultiKey getSyncGroupIdSyncMemberId() {
    return syncGroupIdSyncMemberId;
  }


  
  public void setSyncGroupIdSyncMemberId(MultiKey syncGroupIdSyncMemberId) {
    this.syncGroupIdSyncMemberId = syncGroupIdSyncMemberId;
  }


  public ProvisioningMembershipWrapper() {
    super();
  }

  private GrouperProvisioner grouperProvisioner;
  
  
  /**
   * get grouper target mship if its there, if not, get target provisioning mship
   * @return the target mship
   */
  public ProvisioningMembership getTargetMembership() {
    return GrouperUtil.defaultIfNull(this.grouperTargetMembership, this.targetProvisioningMembership);
  }

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }



  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }



  public String toString() {
    return "MshipWrapper@" + Integer.toHexString(hashCode());
  }

  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   */
  private ProvisioningMembership commonProvisionToTargetMembership;
  


  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   * @return
   */
  public ProvisioningMembership getCommonProvisionToTargetMembership() {
    return commonProvisionToTargetMembership;
  }



  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   * @param commonProvisionToTargetMembership
   */
  public void setCommonProvisionToTargetMembership(
      ProvisioningMembership commonProvisionToTargetMembership) {
    this.commonProvisionToTargetMembership = commonProvisionToTargetMembership;
  }


  private ProvisioningMembership grouperProvisioningMembership;
  
  private ProvisioningMembership targetProvisioningMembership;
  
  private ProvisioningMembership grouperTargetMembership;

  private Object targetNativeMembership;
  
  private GcGrouperSyncMembership gcGrouperSyncMembership;

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

  
  public ProvisioningMembership getGrouperProvisioningMembership() {
    return grouperProvisioningMembership;
  }

  
  public void setGrouperProvisioningMembership(
      ProvisioningMembership grouperProvisioningMembership) {
    this.grouperProvisioningMembership = grouperProvisioningMembership;
    if (this.grouperProvisioningMembership!=null) {
      this.groupIdMemberId = new MultiKey(this.grouperProvisioningMembership.getProvisioningGroupId(), this.grouperProvisioningMembership.getProvisioningEntityId());
      if (this != this.grouperProvisioningMembership.getProvisioningMembershipWrapper()) {
        if (this.grouperProvisioningMembership.getProvisioningMembershipWrapper() != null) {
          this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().remove(this.grouperProvisioningMembership.getProvisioningMembershipWrapper());
        }
        this.grouperProvisioningMembership.setProvisioningMembershipWrapper(this);
      }
    }
  }

  
  public ProvisioningMembership getTargetProvisioningMembership() {
    return targetProvisioningMembership;
  }

  
  public void setTargetProvisioningMembership(
      ProvisioningMembership targetProvisioningMembership) {
    this.targetProvisioningMembership = targetProvisioningMembership;
    if (this.targetProvisioningMembership != null && this != this.targetProvisioningMembership.getProvisioningMembershipWrapper()) {
      if (this.targetProvisioningMembership.getProvisioningMembershipWrapper() != null) {
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().remove(this.targetProvisioningMembership.getProvisioningMembershipWrapper());
      }
      this.targetProvisioningMembership.setProvisioningMembershipWrapper(this);
    }
  }

  
  public ProvisioningMembership getGrouperTargetMembership() {
    return grouperTargetMembership;
  }

  
  public void setGrouperTargetMembership(ProvisioningMembership grouperTargetMembership) {
    this.grouperTargetMembership = grouperTargetMembership;
    if (this.grouperTargetMembership != null && this != this.grouperTargetMembership.getProvisioningMembershipWrapper()) {
      if (this.grouperTargetMembership.getProvisioningMembershipWrapper() != null) {
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().remove(this.grouperTargetMembership.getProvisioningMembershipWrapper());
      }
      this.grouperTargetMembership.setProvisioningMembershipWrapper(this);
    }
  }

  
  public Object getTargetNativeMembership() {
    return targetNativeMembership;
  }

  
  public void setTargetNativeMembership(Object targetNativeMembership) {
    this.targetNativeMembership = targetNativeMembership;
  }

  
  public GcGrouperSyncMembership getGcGrouperSyncMembership() {
    return gcGrouperSyncMembership;
  }

  
  public void setGcGrouperSyncMembership(GcGrouperSyncMembership gcGrouperSyncMembership) {
    this.gcGrouperSyncMembership = gcGrouperSyncMembership;
    if (gcGrouperSyncMembership != null) {
      this.syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId());
    }
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
