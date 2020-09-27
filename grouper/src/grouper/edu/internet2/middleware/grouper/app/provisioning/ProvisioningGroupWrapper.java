package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

public class ProvisioningGroupWrapper {
  
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

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   */
  private ProvisioningGroup grouperProvisioningGroupIncludeDelete;

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public ProvisioningGroup getGrouperProvisioningGroupIncludeDelete() {
    return grouperProvisioningGroupIncludeDelete;
  }

  /**
   * incremental state of data that includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param grouperProvisioningGroupIncludeDelete
   */
  public void setGrouperProvisioningGroupIncludeDelete(
      ProvisioningGroup grouperProvisioningGroupIncludeDelete) {
    this.grouperProvisioningGroupIncludeDelete = grouperProvisioningGroupIncludeDelete;
  }

  private ProvisioningGroup targetProvisioningGroup;
  
  private ProvisioningGroup grouperTargetGroup;

  /**
   * target state that includes deleted data, which is used to retrieve data from the target
   */
  private ProvisioningGroup grouperTargetGroupIncludeDelete;

  /**
   * target state that includes deleted data, which is used to retrieve data from the target
   * @return
   */
  public ProvisioningGroup getGrouperTargetGroupIncludeDelete() {
    return grouperTargetGroupIncludeDelete;
  }

  /**
   * target state that includes deleted data, which is used to retrieve data from the target
   * @param grouperTargetGroupIncludeDelete
   */
  public void setGrouperTargetGroupIncludeDelete(
      ProvisioningGroup grouperTargetGroupIncludeDelete) {
    this.grouperTargetGroupIncludeDelete = grouperTargetGroupIncludeDelete;
  }

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
