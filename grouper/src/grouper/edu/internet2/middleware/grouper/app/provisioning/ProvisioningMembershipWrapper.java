package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class ProvisioningMembershipWrapper {
  
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
  }

  
  public ProvisioningMembership getTargetProvisioningMembership() {
    return targetProvisioningMembership;
  }

  
  public void setTargetProvisioningMembership(
      ProvisioningMembership targetProvisioningMembership) {
    this.targetProvisioningMembership = targetProvisioningMembership;
  }

  
  public ProvisioningMembership getGrouperTargetMembership() {
    return grouperTargetMembership;
  }

  
  public void setGrouperTargetMembership(ProvisioningMembership grouperTargetMembership) {
    this.grouperTargetMembership = grouperTargetMembership;
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
