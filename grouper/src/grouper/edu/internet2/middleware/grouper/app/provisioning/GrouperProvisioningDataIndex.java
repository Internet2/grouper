package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * index objects by uuid, sync id, or matching id.  Generally this is what should be used to process
 * data since it is a complete list of data and easy to look up.  Or the complete list of wrappers is in 
 * GrouperProvisioningData
 * @author mchyzer
 *
 */
public class GrouperProvisioningDataIndex {

  public GrouperProvisioningDataIndex() {
  }
  
  private GrouperProvisioner grouperProvisioner = null;

  private Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();

  private Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();
  
  
  private Map<String, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> targetGroupIdTargetEntityIdToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();

  /**
   * lookup of native-target users by target id. Target ids are expected to be unique within a
   * provisioner, but if the target system has dups the count and a small sample of duplicate ids
   * are recorded in the provisioner debug map under {@code nativeUserTargetIdDupCount} and
   * {@code nativeUserTargetIdDupExamples}; the map only retains one of the duplicates (last wins).
   * Iterate {@link GrouperProvisioningData#getTargetNativeUsers()} for full access.
   */
  private Map<String, GrouperProvisioningTargetNativeUser> targetUserIdToNativeUser = new HashMap<String, GrouperProvisioningTargetNativeUser>();

  /**
   * lookup of native-target groups by target id. Same dup semantics as
   * {@link #targetUserIdToNativeUser} (count + sample under
   * {@code nativeGroupTargetIdDupCount} / {@code nativeGroupTargetIdDupExamples}).
   */
  private Map<String, GrouperProvisioningTargetNativeGroup> targetGroupIdToNativeGroup = new HashMap<String, GrouperProvisioningTargetNativeGroup>();



  public Map<String, ProvisioningGroupWrapper> getTargetGroupIdToProvisioningGroupWrapper() {
    return targetGroupIdToProvisioningGroupWrapper;
  }


  public Map<String, ProvisioningEntityWrapper> getTargetEntityIdToProvisioningEntityWrapper() {
    return targetEntityIdToProvisioningEntityWrapper;
  }


  public Map<MultiKey, ProvisioningMembershipWrapper> getTargetGroupIdTargetEntityIdToProvisioningMembershipWrapper() {
    return targetGroupIdTargetEntityIdToProvisioningMembershipWrapper;
  }

  public Map<String, GrouperProvisioningTargetNativeUser> getTargetUserIdToNativeUser() {
    return targetUserIdToNativeUser;
  }

  public Map<String, GrouperProvisioningTargetNativeGroup> getTargetGroupIdToNativeGroup() {
    return targetGroupIdToNativeGroup;
  }

  /**
   * Build the native target-id Maps from the populated lists on
   * {@link GrouperProvisioningData}, recording duplicate counts and a few examples
   * in the supplied debug map. Failsafe: any anomaly is captured rather than thrown.
   *
   * @param debugMap optional; if non-null, dup counts and examples are written here
   */
  public void buildNativeIndexes(Map<String, Object> debugMap) {

    GrouperProvisioningData grouperProvisioningData = this.grouperProvisioner.retrieveGrouperProvisioningData();

    this.targetUserIdToNativeUser.clear();
    int userDupCount = 0;
    java.util.List<String> userDupExamples = new java.util.ArrayList<String>();
    for (GrouperProvisioningTargetNativeUser grouperProvisioningTargetNativeUser
        : GrouperUtil.nonNull(grouperProvisioningData.getTargetNativeUsers())) {
      String targetId = grouperProvisioningTargetNativeUser.getTargetId();
      if (targetId == null) {
        continue;
      }
      GrouperProvisioningTargetNativeUser prev = this.targetUserIdToNativeUser.put(targetId,
          grouperProvisioningTargetNativeUser);
      if (prev != null) {
        userDupCount++;
        if (userDupExamples.size() < 5) {
          userDupExamples.add(targetId);
        }
      }
    }
    if (debugMap != null && userDupCount > 0) {
      debugMap.put("nativeUserTargetIdDupCount", userDupCount);
      debugMap.put("nativeUserTargetIdDupExamples", userDupExamples);
    }

    this.targetGroupIdToNativeGroup.clear();
    int groupDupCount = 0;
    java.util.List<String> groupDupExamples = new java.util.ArrayList<String>();
    for (GrouperProvisioningTargetNativeGroup grouperProvisioningTargetNativeGroup
        : GrouperUtil.nonNull(grouperProvisioningData.getTargetNativeGroups())) {
      String targetId = grouperProvisioningTargetNativeGroup.getTargetId();
      if (targetId == null) {
        continue;
      }
      GrouperProvisioningTargetNativeGroup prev = this.targetGroupIdToNativeGroup.put(targetId,
          grouperProvisioningTargetNativeGroup);
      if (prev != null) {
        groupDupCount++;
        if (groupDupExamples.size() < 5) {
          groupDupExamples.add(targetId);
        }
      }
    }
    if (debugMap != null && groupDupCount > 0) {
      debugMap.put("nativeGroupTargetIdDupCount", groupDupCount);
      debugMap.put("nativeGroupTargetIdDupExamples", groupDupExamples);
    }
  }



  public boolean isHasIncrementalDataToProcess() {
    
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers().size() > 0) {
      return true;
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers().size() > 0) {
      return true;
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().size() > 0) {
      return true;
    }
    
    return false;

  }

  
  public Map<String, ProvisioningGroupWrapper> getGrouperSyncGroupIdToProvisioningGroupWrapper() {
    return grouperSyncGroupIdToProvisioningGroupWrapper;
  }






  
  public Map<String, ProvisioningEntityWrapper> getGrouperSyncMemberIdToProvisioningEntityWrapper() {
    return grouperSyncMemberIdToProvisioningEntityWrapper;
  }






  
  public Map<MultiKey, ProvisioningMembershipWrapper> getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper() {
    return grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper;
  }






  public Map<String, ProvisioningGroupWrapper> getGroupUuidToProvisioningGroupWrapper() {
    return groupUuidToProvisioningGroupWrapper;
  }





  
  public Map<String, ProvisioningEntityWrapper> getMemberUuidToProvisioningEntityWrapper() {
    return memberUuidToProvisioningEntityWrapper;
  }





  
  public Map<MultiKey, ProvisioningMembershipWrapper> getGroupUuidMemberUuidToProvisioningMembershipWrapper() {
    return groupUuidMemberUuidToProvisioningMembershipWrapper;
  }






  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  

}
