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
  
  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * 
   */
  private ProvisioningMembership grouperProvisioningMembershipIncludeDelete;

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public ProvisioningMembership getGrouperProvisioningMembershipIncludeDelete() {
    return grouperProvisioningMembershipIncludeDelete;
  }

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param grouperProvisioningMembershipIncludeDelete
   */
  public void setGrouperProvisioningMembershipIncludeDelete(
      ProvisioningMembership grouperProvisioningMembershipIncludeDelete) {
    this.grouperProvisioningMembershipIncludeDelete = grouperProvisioningMembershipIncludeDelete;
  }

  private ProvisioningMembership targetProvisioningMembership;
  
  private ProvisioningMembership grouperTargetMembership;

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   */
  private ProvisioningMembership grouperTargetMembershipIncludeDelete;

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @return mship
   */
  public ProvisioningMembership getGrouperTargetMembershipIncludeDelete() {
    return grouperTargetMembershipIncludeDelete;
  }

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param grouperTargetMembershipIncludeDelete
   */
  public void setGrouperTargetMembershipIncludeDelete(
      ProvisioningMembership grouperTargetMembershipIncludeDelete) {
    this.grouperTargetMembershipIncludeDelete = grouperTargetMembershipIncludeDelete;
  }

  private Object targetNativeMembership;
  
  private GcGrouperSyncMembership gcGrouperSyncMembership;

  
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

  
  

}
