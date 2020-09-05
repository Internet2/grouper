package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

public class ProvisioningGroupWrapper {
  
  public ProvisioningGroupWrapper() {
    super();
  }

  /**
   * get grouper target group if its there, if not, get target provisioning group
   * @return the target group
   */
  public ProvisioningGroup getTargetGroup() {
    return GrouperUtil.defaultIfNull(this.grouperTargetGroup, this.targetProvisioningGroup);
  }
  
  private GrouperProvisioner grouperProvisioner;
  
  
  
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }



  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   */
  private ProvisioningGroup commonProvisionToTargetGroup;

  
  
  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   * @return
   */
  public ProvisioningGroup getCommonProvisionToTargetGroup() {
    return commonProvisionToTargetGroup;
  }

  /**
   * crud operation goes from grouper to common to target since its an insert/update/or delete
   * @param commonProvisionToTargetGroup
   */
  public void setCommonProvisionToTargetGroup(
      ProvisioningGroup commonProvisionToTargetGroup) {
    this.commonProvisionToTargetGroup = commonProvisionToTargetGroup;
  }


  private ProvisioningGroup grouperProvisioningGroup;

  private ProvisioningGroup grouperProvisioningGroupToDelete;

  public ProvisioningGroup getGrouperProvisioningGroupToDelete() {
    return grouperProvisioningGroupToDelete;
  }
  
  public void setGrouperProvisioningGroupToDelete(
      ProvisioningGroup grouperProvisioningGroupToDelete) {
    this.grouperProvisioningGroupToDelete = grouperProvisioningGroupToDelete;
  }


  private ProvisioningGroup targetProvisioningGroup;
  
  private ProvisioningGroup grouperTargetGroup;

  private Object targetNativeGroup;
  
  private GcGrouperSyncGroup gcGrouperSyncGroup;

  
  public ProvisioningGroup getGrouperProvisioningGroup() {
    return grouperProvisioningGroup;
  }

  
  public void setGrouperProvisioningGroup(ProvisioningGroup grouperProvisioningGroup) {
    this.grouperProvisioningGroup = grouperProvisioningGroup;
  }

  
  public ProvisioningGroup getTargetProvisioningGroup() {
    return targetProvisioningGroup;
  }

  
  public void setTargetProvisioningGroup(ProvisioningGroup targetProvisioningGroup) {
    this.targetProvisioningGroup = targetProvisioningGroup;
  }

  
  public ProvisioningGroup getGrouperTargetGroup() {
    return grouperTargetGroup;
  }

  
  public void setGrouperTargetGroup(ProvisioningGroup grouperTargetGroup) {
    this.grouperTargetGroup = grouperTargetGroup;
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
  }
  
  public String toString() {
    return "GroupWrapper@" + Integer.toHexString(hashCode());
  }
}
