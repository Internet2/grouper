package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningStateMembership extends ProvisioningStateBase {

  
  /**
   * see if loggable if not logging all objects
   * @return
   */
  public boolean isLoggable(boolean strong) {
    
    if (this.retrieveLoggableCache(strong)) {
      return true;
    }

    if (this.getProvisioningMembershipWrapper().getGrouperProvisioningMembership() != null 
        && this.getProvisioningMembershipWrapper().getGrouperProvisioningMembership().isLoggableHelper(strong)) {
      this.assignLoggableCache(strong);
      return true;
    }

    boolean entityMatches = false;
    
    ProvisioningEntityWrapper provisioningEntityWrapper = this.getProvisioningMembershipWrapper().getProvisioningEntityWrapper();
    if (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isLoggable(true)) {
      entityMatches = true;
    }
    if (provisioningEntityWrapper == null) {
      MultiKey syncGroupIdSyncMemberId = this.getProvisioningMembershipWrapper().getSyncGroupIdSyncMemberId();
      if (syncGroupIdSyncMemberId != null) {
        String syncMemberId = (String)syncGroupIdSyncMemberId.getKey(1);
        GcGrouperSyncMember gcGrouperSyncMember = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveById(syncMemberId);
        if (gcGrouperSyncMember != null) {
          Set<String> logAllObjectsVerboseForTheseEntityNames = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseSubjectIds();
          Set<String> logAllObjectsVerboseEntityAttributes = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseEntityAttributes();

          if (gcGrouperSyncMember.matchesAttribute(logAllObjectsVerboseEntityAttributes, logAllObjectsVerboseForTheseEntityNames)) {
            entityMatches = true;
          }            
        }
      }
    }

    if (!strong && entityMatches) {
      this.assignLoggableCache(strong);
      return true;
    }

    boolean groupMatches = false;

    ProvisioningGroupWrapper provisioningGroupWrapper = this.getProvisioningMembershipWrapper().getProvisioningGroupWrapper();
    if (provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isLoggable(true)) {
      groupMatches = true;
    }
    if (provisioningGroupWrapper == null) {
      MultiKey syncGroupIdSyncMemberId = this.getProvisioningMembershipWrapper().getSyncGroupIdSyncMemberId();
      if (syncGroupIdSyncMemberId != null) {
        String syncGroupId = (String)syncGroupIdSyncMemberId.getKey(0);
        GcGrouperSyncGroup gcGrouperSyncGroup = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveById(syncGroupId);
        if (gcGrouperSyncGroup != null) {
          Set<String> logAllObjectsVerboseForTheseGroupNames = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseGroupNames();
          Set<String> logAllObjectsVerboseGroupAttributes = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseGroupAttributes();

          if (gcGrouperSyncGroup.matchesAttribute(logAllObjectsVerboseGroupAttributes, logAllObjectsVerboseForTheseGroupNames)) {
            groupMatches = true;
          }            
        }
      }
    }

    if (!groupMatches) {
      return false;
    }

    if (strong && !entityMatches) {
      return false;
    }

    this.assignLoggableCache(strong);
    return true;
  }


  private ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
  
  
  
  
  public ProvisioningMembershipWrapper getProvisioningMembershipWrapper() {
    return provisioningMembershipWrapper;
  }


  
  public void setProvisioningMembershipWrapper(
      ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrapper = provisioningMembershipWrapper;
  }

  private MultiKey groupIdMemberId = null;
  private boolean valueExistsInGrouper;

  
  
  
  public MultiKey getGroupIdMemberId() {
    return groupIdMemberId;
  }

  
  public void setGroupIdMemberId(MultiKey groupIdMemberId) {
    this.groupIdMemberId = groupIdMemberId;
  }

  private static Set<String> toStringFieldNamesToIgnore = GrouperClientUtils.toSet("provisioningMembershipWrapper", "loggableStrong", "loggableWeak");
  
  /**
   * 
   */
  @Override
  public String toString() {
    String ids = null;
    if (this.getProvisioningMembershipWrapper().getGroupIdMemberId() != null) {
      ids = "groupId='" + this.getProvisioningMembershipWrapper().getGroupIdMemberId().getKey(0) + "', memberId='" + this.getProvisioningMembershipWrapper().getGroupIdMemberId().getKey(1) + "'";
    } else {
      ids = "groupId='null', memberId='null'";
    }
    return GrouperClientUtils.toStringReflection(this, toStringFieldNamesToIgnore, ids);
  }
  
  /**
   * this must be called after retrieving data from grouper
   * @return
   */
  public boolean isExistInGrouper() {
    if (this.getProvisioningMembershipWrapper().getGrouperProvisioningMembership() == null) {
      return false;
    }
    if(this.getProvisioningMembershipWrapper().getProvisioningStateMembership().isDelete()) {
      return false;
    }
    if(!this.getProvisioningMembershipWrapper().getProvisioningStateMembership().isInGrouper()) {
      return false;
    }
    return true;
  }



  public void setValueExistsInGrouper(boolean valueExistsInGrouper) {
    this.valueExistsInGrouper = valueExistsInGrouper;
  }

  public boolean isValueExistsInGrouper() {
    return valueExistsInGrouper;
  }



  public GrouperProvisioner getGrouperProvisioner() {
    return this.getProvisioningMembershipWrapper().getGrouperProvisioner();
  }
  
}
