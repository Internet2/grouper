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
  
  
  
  public Map<String, ProvisioningGroupWrapper> getTargetGroupIdToProvisioningGroupWrapper() {
    return targetGroupIdToProvisioningGroupWrapper;
  }

  
  public Map<String, ProvisioningEntityWrapper> getTargetEntityIdToProvisioningEntityWrapper() {
    return targetEntityIdToProvisioningEntityWrapper;
  }


  public Map<MultiKey, ProvisioningMembershipWrapper> getTargetGroupIdTargetEntityIdToProvisioningMembershipWrapper() {
    return targetGroupIdTargetEntityIdToProvisioningMembershipWrapper;
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
