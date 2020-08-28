package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

public class ProvisioningGroupWrapper {
  
  private ProvisioningGroup grouperProvisioningGroup;
  
  private ProvisioningGroup targetProvisioningGroup;
  
  private ProvisioningGroup grouperCommonGroup;

  private ProvisioningGroup targetCommonGroup;
  
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

  
  public ProvisioningGroup getGrouperCommonGroup() {
    return grouperCommonGroup;
  }

  
  public void setGrouperCommonGroup(ProvisioningGroup grouperCommonGroup) {
    this.grouperCommonGroup = grouperCommonGroup;
  }

  
  public ProvisioningGroup getTargetCommonGroup() {
    return targetCommonGroup;
  }

  
  public void setTargetCommonGroup(ProvisioningGroup targetCommonGroup) {
    this.targetCommonGroup = targetCommonGroup;
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
  

}
