package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningStateMembership extends ProvisioningStateBase {

  
  /**
   * see if loggable if not logging all objects
   * @return
   */
  public boolean isLoggable() {
    
    if (this.isLoggableHelper()) {
      return true;
    }

    if (this.getProvisioningMembershipWrapper().getGrouperProvisioningMembership() != null && this.getProvisioningMembershipWrapper().getGrouperProvisioningMembership().isLoggableHelper()) {
      this.setLoggable(true);
      return true;
    }
      
    ProvisioningEntityWrapper provisioningEntityWrapper = this.getProvisioningMembershipWrapper().getProvisioningEntityWrapper();
    if (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isLoggable()) {
      this.setLoggable(true);
      return true;
    }

    ProvisioningGroupWrapper provisioningGroupWrapper = this.getProvisioningMembershipWrapper().getProvisioningGroupWrapper();
    if (provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isLoggable()) {
      this.setLoggable(true);
      return true;
    }

    return false;
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

  private static Set<String> toStringFieldNamesToIgnore = GrouperClientUtils.toSet("provisioningMembershipWrapper");
  
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
  
}
