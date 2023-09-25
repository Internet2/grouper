package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class ProvisioningMembershipWrapper extends ProvisioningUpdatableWrapper {

  private ProvisioningStateMembership provisioningStateMembership = new ProvisioningStateMembership();
  
  public ProvisioningStateMembership getProvisioningStateMembership() {
    return provisioningStateMembership;
  }

  public ProvisioningGroupWrapper getProvisioningGroupWrapper() {
    
    if (this.grouperProvisioningMembership != null) {
      if (this.grouperProvisioningMembership.getProvisioningGroup() != null) {
        if (this.grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper() != null) {
          return this.grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
        }
      }
    }

    if (this.grouperTargetMembership != null) {
      if (this.grouperTargetMembership.getProvisioningGroup() != null) {
        if (this.grouperTargetMembership.getProvisioningGroup().getProvisioningGroupWrapper() != null) {
          return this.grouperTargetMembership.getProvisioningGroup().getProvisioningGroupWrapper();
        }
      }
    }

    if (this.targetProvisioningMembership != null) {
      if (this.targetProvisioningMembership.getProvisioningGroup() != null) {
        if (this.targetProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper() != null) {
          return this.targetProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
        }
      }
    }
    if (this.commonProvisionToTargetMembership != null) {
      if (this.commonProvisionToTargetMembership.getProvisioningGroup() != null) {
        if (this.commonProvisionToTargetMembership.getProvisioningGroup().getProvisioningGroupWrapper() != null) {
          return this.commonProvisionToTargetMembership.getProvisioningGroup().getProvisioningGroupWrapper();
        }
      }
    }
    if (this.groupIdMemberId != null) {
      ProvisioningGroupWrapper provisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(this.groupIdMemberId.getKey(0));
      if (provisioningGroupWrapper != null) {
        return provisioningGroupWrapper;
      }
    }
    if (this.syncGroupIdSyncMemberId != null) {
      ProvisioningGroupWrapper provisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper().get(this.syncGroupIdSyncMemberId.getKey(0));
      if (provisioningGroupWrapper != null) {
        return provisioningGroupWrapper;
      }
    }
    return null;
  }

  public ProvisioningEntityWrapper getProvisioningEntityWrapper() {
    if (this.grouperProvisioningMembership != null) {
      if (this.grouperProvisioningMembership.getProvisioningEntity() != null) {
        if (this.grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper() != null) {
          return this.grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
        }
      }
    }
    if (this.grouperTargetMembership != null) {
      if (this.grouperTargetMembership.getProvisioningEntity() != null) {
        if (this.grouperTargetMembership.getProvisioningEntity().getProvisioningEntityWrapper() != null) {
          return this.grouperTargetMembership.getProvisioningEntity().getProvisioningEntityWrapper();
        }
      }
    }
    if (this.targetProvisioningMembership != null) {
      if (this.targetProvisioningMembership.getProvisioningEntity() != null) {
        if (this.targetProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper() != null) {
          return this.targetProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
        }
      }
    }
    if (this.commonProvisionToTargetMembership != null) {
      if (this.commonProvisionToTargetMembership.getProvisioningEntity() != null) {
        if (this.commonProvisionToTargetMembership.getProvisioningEntity().getProvisioningEntityWrapper() != null) {
          return this.commonProvisionToTargetMembership.getProvisioningEntity().getProvisioningEntityWrapper();
        }
      }
    }
    if (this.groupIdMemberId != null) {
      ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(this.groupIdMemberId.getKey(1));
      if (provisioningEntityWrapper != null) {
        return provisioningEntityWrapper;
      }
    }
    if (this.syncGroupIdSyncMemberId != null) {
      ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper().get(this.syncGroupIdSyncMemberId.getKey(1));
      if (provisioningEntityWrapper != null) {
        return provisioningEntityWrapper;
      }
    }

    return null;
    
  }

  private MultiKey groupIdMemberId = null;
  
  private MultiKey syncGroupIdSyncMemberId = null;
  

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
    this.provisioningStateMembership.setProvisioningMembershipWrapper(this);
  }

  /**
   * get grouper target mship if its there, if not, get target provisioning mship
   * @return the target mship
   */
  public ProvisioningMembership getTargetMembership() {
    return GrouperUtil.defaultIfNull(this.grouperTargetMembership, this.targetProvisioningMembership);
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

  public ProvisioningMembership getGrouperProvisioningMembership() {
    return grouperProvisioningMembership;
  }

  
  public void setGrouperProvisioningMembership(
      ProvisioningMembership grouperProvisioningMembership) {
    if (this.grouperProvisioningMembership == grouperProvisioningMembership) {
      return;
    }
    ProvisioningMembership oldGrouperProvisioningMembership = this.grouperProvisioningMembership;
    ProvisioningMembershipWrapper oldProvisioningMembershipWrapper = oldGrouperProvisioningMembership == null ? null : oldGrouperProvisioningMembership.getProvisioningMembershipWrapper();

    this.grouperProvisioningMembership = grouperProvisioningMembership;
    
    if (this.grouperProvisioningMembership != null) {
      this.grouperProvisioningMembership.setProvisioningMembershipWrapper(this);
    }

    if (oldGrouperProvisioningMembership != null) {
      oldGrouperProvisioningMembership.setProvisioningMembershipWrapper(null);
    }
    if (oldProvisioningMembershipWrapper != null && oldProvisioningMembershipWrapper != this) {
      oldProvisioningMembershipWrapper.grouperProvisioningMembership = null;
    }
    this.calculateGroupIdMemberId();
  }

  
  private void calculateGroupIdMemberId() {
    MultiKey newGroupIdMemberId = null;
    if (this.grouperProvisioningMembership != null) {
      newGroupIdMemberId = new MultiKey(this.grouperProvisioningMembership.getProvisioningGroupId(), this.grouperProvisioningMembership.getProvisioningEntityId());

    } else if (this.gcGrouperSyncMembership != null) {
      GcGrouperSyncGroup gcGrouperSyncGroup = this.gcGrouperSyncMembership.getGrouperSyncGroup();
      GcGrouperSyncMember gcGrouperSyncMember = this.gcGrouperSyncMembership.getGrouperSyncMember();
      if (gcGrouperSyncGroup != null && gcGrouperSyncMember != null) {
        newGroupIdMemberId = new MultiKey(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncMember.getMemberId());
      }
    }
    // in incremental this might already be there and shouldnt be removed
    if (newGroupIdMemberId != null) {
      this.groupIdMemberId = newGroupIdMemberId;
    }
  }

  public ProvisioningMembership getTargetProvisioningMembership() {
    return targetProvisioningMembership;
  }

  
  public void setTargetProvisioningMembership(
      ProvisioningMembership targetProvisioningMembership) {

    if (this.targetProvisioningMembership == targetProvisioningMembership) {
      return;
    }

    ProvisioningMembership oldTargetProvisioningMembership = this.targetProvisioningMembership;
    ProvisioningMembershipWrapper oldProvisioningMembershipWrapper = oldTargetProvisioningMembership == null ? null : oldTargetProvisioningMembership.getProvisioningMembershipWrapper();

    this.targetProvisioningMembership = targetProvisioningMembership;
    
    if (this.targetProvisioningMembership != null) {
      ProvisioningMembershipWrapper newTargetMembershipOldWrapper = this.targetProvisioningMembership.getProvisioningMembershipWrapper();
      
      this.targetProvisioningMembership.setProvisioningMembershipWrapper(this);
      
      if (newTargetMembershipOldWrapper != null && newTargetMembershipOldWrapper.getProvisioningStateMembership().isSelectResultProcessed()) {
        this.getProvisioningStateMembership().setSelectResultProcessed(true);
      }

    }

    if (oldTargetProvisioningMembership != null && oldTargetProvisioningMembership != this.targetProvisioningMembership) {
      oldTargetProvisioningMembership.setProvisioningMembershipWrapper(null);
    }
    if (oldProvisioningMembershipWrapper != null && oldProvisioningMembershipWrapper != this) {
      oldProvisioningMembershipWrapper.targetProvisioningMembership = null;
    }
  }

  
  public ProvisioningMembership getGrouperTargetMembership() {
    return grouperTargetMembership;
  }

  
  public void setGrouperTargetMembership(ProvisioningMembership grouperTargetMembership) {
    
    if (this.grouperTargetMembership == grouperTargetMembership) {
      return;
    }
    
    ProvisioningMembership oldGrouperTargetMembership = this.grouperTargetMembership;
    ProvisioningMembershipWrapper oldProvisioningMembershipWrapper = oldGrouperTargetMembership == null ? null : oldGrouperTargetMembership.getProvisioningMembershipWrapper();

    this.grouperTargetMembership = grouperTargetMembership;
    
    if (this.grouperTargetMembership != null) {
      this.grouperTargetMembership.setProvisioningMembershipWrapper(this);
    }

    if (oldGrouperTargetMembership != null && oldGrouperTargetMembership != this.targetProvisioningMembership) {
      oldGrouperTargetMembership.setProvisioningMembershipWrapper(null);
    }
    if (oldProvisioningMembershipWrapper != null && oldProvisioningMembershipWrapper != this) {
      oldProvisioningMembershipWrapper.grouperTargetMembership = null;
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
    this.calculateGroupIdMemberId();

  }


  @Override
  public String objectTypeName() {
    return "membership";
  }

  public String toStringForErrorVerbose() {
    StringBuilder result = new StringBuilder();
    if (this.getGrouperProvisioningMembership() != null && this.getGrouperProvisioningMembership().getProvisioningGroup() != null && this.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper() != null) {
      result.append(this.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper().toStringForErrorVerbose()).append(", ");
    } else if (this.grouperTargetMembership != null && this.grouperTargetMembership.getProvisioningGroup() != null) {
      result.append(this.grouperTargetMembership.getProvisioningGroup()).append(", ");
    }

    if (this.getGrouperProvisioningMembership() != null && this.getGrouperProvisioningMembership().getProvisioningEntity() != null && this.getGrouperProvisioningMembership().getProvisioningEntity().getProvisioningEntityWrapper() != null) {
      result.append(this.getGrouperProvisioningMembership().getProvisioningEntity().getProvisioningEntityWrapper().toStringForErrorVerbose()).append(", ");
    } else if (this.grouperTargetMembership != null && this.grouperTargetMembership.getProvisioningEntity() != null) {
      result.append(this.grouperTargetMembership.getProvisioningEntity());
    }

    if (result.length() > 0) {
      return result.toString();
    }
    return this.toString();
    
  }
  
  public String toStringForError() {
    
    StringBuilder result = new StringBuilder();
    if (this.getGrouperProvisioningMembership() != null && this.getGrouperProvisioningMembership().getProvisioningGroup() != null && this.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper() != null) {
      result.append(this.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper().toStringForError()).append(", ");
    } else if (this.grouperTargetMembership != null && this.grouperTargetMembership.getProvisioningGroup() != null) {
      result.append(this.grouperTargetMembership.getProvisioningGroup()).append(", ");
    }

    if (this.getGrouperProvisioningMembership() != null && this.getGrouperProvisioningMembership().getProvisioningEntity() != null && this.getGrouperProvisioningMembership().getProvisioningEntity().getProvisioningEntityWrapper() != null) {
      result.append(this.getGrouperProvisioningMembership().getProvisioningEntity().getProvisioningEntityWrapper().toStringForError()).append(", ");
    } else if (this.grouperTargetMembership != null && this.grouperTargetMembership.getProvisioningEntity() != null) {
      result.append(this.grouperTargetMembership.getProvisioningEntity());
    }

    if (result.length() > 0) {
      return result.toString();
    }
    return this.toString();
  }


}
