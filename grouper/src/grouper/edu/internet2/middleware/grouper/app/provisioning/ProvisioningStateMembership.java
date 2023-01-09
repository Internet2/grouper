package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningStateMembership extends ProvisioningStateBase {

  private MultiKey groupIdMemberId = null;

  
  
  
  public MultiKey getGroupIdMemberId() {
    return groupIdMemberId;
  }

  
  public void setGroupIdMemberId(MultiKey groupIdMemberId) {
    this.groupIdMemberId = groupIdMemberId;
  }

  private static Set<String> toStringFieldNamesToIgnore = GrouperClientUtils.toSet();
  
  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, toStringFieldNamesToIgnore);
  }

  
}
