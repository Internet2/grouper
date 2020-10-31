package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

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

  /**
   * note some entries could be for deleting
   */
  private Map<Object, ProvisioningGroupWrapper> groupMatchingIdToProvisioningGroupWrapper = new HashMap<Object, ProvisioningGroupWrapper>();

  private Map<Object, ProvisioningEntityWrapper> entityMatchingIdToProvisioningEntityWrapper = new HashMap<Object, ProvisioningEntityWrapper>();

  private Map<Object, ProvisioningMembershipWrapper> membershipMatchingIdToProvisioningMembershipWrapper = new HashMap<Object, ProvisioningMembershipWrapper>();
  
  private Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();

  private Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();

  private Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();

  private Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();

  
  
  
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





  public Map<Object, ProvisioningGroupWrapper> getGroupMatchingIdToProvisioningGroupWrapper() {
    return groupMatchingIdToProvisioningGroupWrapper;
  }




  
  public Map<Object, ProvisioningEntityWrapper> getEntityMatchingIdToProvisioningEntityWrapper() {
    return entityMatchingIdToProvisioningEntityWrapper;
  }




  
  public Map<Object, ProvisioningMembershipWrapper> getMembershipMatchingIdToProvisioningMembershipWrapper() {
    return membershipMatchingIdToProvisioningMembershipWrapper;
  }




  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  

}
