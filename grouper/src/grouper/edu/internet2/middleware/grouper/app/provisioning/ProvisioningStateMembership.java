package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningStateMembership extends ProvisioningStateBase {

<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
  
  
  private ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
  
  
  
  
  public ProvisioningMembershipWrapper getProvisioningMembershipWrapper() {
    return provisioningMembershipWrapper;
  }


  
  public void setProvisioningMembershipWrapper(
      ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrapper = provisioningMembershipWrapper;
  }

  private MultiKey groupIdMemberId = null;

  
  
  
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
    return true;
  }
=======
=======
  private ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
  
  
  
  
  public ProvisioningMembershipWrapper getProvisioningMembershipWrapper() {
    return provisioningMembershipWrapper;
  }


  
  public void setProvisioningMembershipWrapper(
      ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrapper = provisioningMembershipWrapper;
  }

>>>>>>> a8d0568 improve logging of new provisioning state
  private MultiKey groupIdMemberId = null;

  
  
  
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

>>>>>>> 252ebc1 restructure how state is stored in provisioning wrappers
  
}
