package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class ProvisioningMembershipWrapper {
  
  public ProvisioningMembershipWrapper() {
    super();
  }

  private GrouperProvisioner grouperProvisioner;
  
  
  /**
   * get grouper common mship if its there, if not, get target common mship
   * @return the common mship
   */
  public ProvisioningMembership getCommonMembership() {
    return GrouperUtil.defaultIfNull(this.grouperCommonMembership, this.targetCommonMembership);
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
  
  private ProvisioningMembership grouperProvisioningMembershipToDelete;

  
  public ProvisioningMembership getGrouperProvisioningMembershipToDelete() {
    return grouperProvisioningMembershipToDelete;
  }


  
  public void setGrouperProvisioningMembershipToDelete(
      ProvisioningMembership grouperProvisioningMembershipToDelete) {
    this.grouperProvisioningMembershipToDelete = grouperProvisioningMembershipToDelete;
  }


  private ProvisioningMembership targetProvisioningMembership;
  
  private ProvisioningMembership grouperCommonMembership;

  private ProvisioningMembership targetCommonMembership;
  
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

  
  public ProvisioningMembership getGrouperCommonMembership() {
    return grouperCommonMembership;
  }

  
  public void setGrouperCommonMembership(ProvisioningMembership grouperCommonMembership) {
    this.grouperCommonMembership = grouperCommonMembership;
  }

  
  public ProvisioningMembership getTargetCommonMembership() {
    return targetCommonMembership;
  }

  
  public void setTargetCommonMembership(ProvisioningMembership targetCommonMembership) {
    this.targetCommonMembership = targetCommonMembership;
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
