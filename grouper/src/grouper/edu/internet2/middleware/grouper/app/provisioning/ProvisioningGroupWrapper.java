package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

public class ProvisioningGroupWrapper extends ProvisioningUpdatableWrapper {
  
  private ProvisioningStateGroup provisioningStateGroup = new ProvisioningStateGroup();


  public ProvisioningStateGroup getProvisioningStateGroup() {
    return provisioningStateGroup;
  }

  private boolean grouperTargetGroupFromCacheInitted = false;
  private ProvisioningGroup grouperTargetGroupFromCache;

  //TODO finish this for cached objects
  public ProvisioningGroup getGrouperTargetGroupFromCache() {
    if (grouperTargetGroupFromCacheInitted 
        || this.gcGrouperSyncGroup == null || this.getGrouperProvisioner() == null) {
      return grouperTargetGroupFromCache;
    }
    
    // see if there is an object cached
    for (GrouperProvisioningConfigurationAttributeDbCache cache :
      this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
      if (cache == null 
          || cache.getSource() != GrouperProvisioningConfigurationAttributeDbCacheSource.grouper 
          || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
        continue;
      }
      
    }
    return grouperTargetGroupFromCache;
  }

  private boolean targetProvisioningGroupFromCacheInitted = false;
  private ProvisioningGroup targetProvisioningGroupFromCache;

  
    
  public ProvisioningGroup getTargetProvisioningGroupFromCache() {
    return targetProvisioningGroupFromCache;
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
    this.provisioningStateGroup.setProvisioningGroupWrapper(this);
  }

  /**
   * this is the representation of grouper side that grouper retrieves from its database 
   */
  private ProvisioningGroup grouperProvisioningGroup;

  /**
   * this is what is retrieved from the target and structured in the target representation
   */
  private ProvisioningGroup targetProvisioningGroup;
  
  /**
   * grouper side translated for target
   */
  private ProvisioningGroup grouperTargetGroup;

  /**
   * this comes from the commands class and is target specific bean
   */
  private Object targetNativeGroup;
  
  private GcGrouperSyncGroup gcGrouperSyncGroup;

  
  public ProvisioningGroup getGrouperProvisioningGroup() {
    return grouperProvisioningGroup;
  }

  private void calculateGroupId() {
    this.groupId = null;
    if (this.grouperProvisioningGroup != null) {
      this.groupId = this.grouperProvisioningGroup.getId();
    } else if (this.gcGrouperSyncGroup != null) {
      this.groupId = this.gcGrouperSyncGroup.getGroupId();
    }
  }
  
  public void setGrouperProvisioningGroup(ProvisioningGroup grouperProvisioningGroup) {
    if (this.grouperProvisioningGroup == grouperProvisioningGroup) {
      return;
    }
    ProvisioningGroup oldGrouperProvisioningGroup = this.grouperProvisioningGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldGrouperProvisioningGroup == null ? null : oldGrouperProvisioningGroup.getProvisioningGroupWrapper();

    this.grouperProvisioningGroup = grouperProvisioningGroup;
    
    if (this.grouperProvisioningGroup != null) {
      this.grouperProvisioningGroup.setProvisioningGroupWrapper(this);
    }

    if (oldGrouperProvisioningGroup != null) {
      oldGrouperProvisioningGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.grouperProvisioningGroup = null;
    }
    this.calculateGroupId();
  }

  
  public ProvisioningGroup getTargetProvisioningGroup() {
    return targetProvisioningGroup;
  }

  
  public void setTargetProvisioningGroup(ProvisioningGroup targetProvisioningGroup) {
    
    if (this.targetProvisioningGroup == targetProvisioningGroup) {
      return;
    }
    ProvisioningGroup oldTargetProvisioningGroup = this.targetProvisioningGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldTargetProvisioningGroup == null ? null : oldTargetProvisioningGroup.getProvisioningGroupWrapper();

    this.targetProvisioningGroup = targetProvisioningGroup;
    
    if (this.targetProvisioningGroup != null) {
      this.targetProvisioningGroup.setProvisioningGroupWrapper(this);
    }

    if (oldTargetProvisioningGroup != null) {
      oldTargetProvisioningGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.targetProvisioningGroup = null;
    }

  }

  
  public ProvisioningGroup getGrouperTargetGroup() {
    return grouperTargetGroup;
  }

  
  public void setGrouperTargetGroup(ProvisioningGroup grouperTargetGroup) {
    
    if (this.grouperTargetGroup == grouperTargetGroup) {
      return;
    }
    
    ProvisioningGroup oldGrouperTargetGroup = this.grouperTargetGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldGrouperTargetGroup == null ? null : oldGrouperTargetGroup.getProvisioningGroupWrapper();

    this.grouperTargetGroup = grouperTargetGroup;
    
    if (this.grouperTargetGroup != null) {
      this.grouperTargetGroup.setProvisioningGroupWrapper(this);
    }

    if (oldGrouperTargetGroup != null) {
      oldGrouperTargetGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.grouperTargetGroup = null;
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
    this.calculateGroupId();

  }
  
  public String toString() {
    return "GroupWrapper@" + Integer.toHexString(hashCode());
  }
  
  public String toStringForError() {
    
    if (this.grouperTargetGroup != null) {
      return "grouperTargetGroup: " + this.grouperTargetGroup;
    }

    if (this.grouperProvisioningGroup != null) {
      return "grouperProvisioningGroup: " + this.grouperProvisioningGroup;
    }

    if (this.targetProvisioningGroup != null) {
      return "targetProvisioningGroup: " + this.targetProvisioningGroup;
    }
    
    if (this.provisioningStateGroup != null) {
      return "provisioningStateGroup: " + this.provisioningStateGroup;
    }

    return this.toString();
  }

  @Override
  public String objectTypeName() {
    return "group";
  }
  

}
