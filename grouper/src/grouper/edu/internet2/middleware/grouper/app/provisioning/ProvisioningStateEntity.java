package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningStateEntity extends ProvisioningStateBase {

  private String memberId;
  /**
   * if this is incremental, and syncing memberships for this group
   */
  private boolean incrementalSyncMemberships;
  /**
   * if recalcing the entity memberships 
   */
  private boolean recalcEntityMemberships;

  
  public String getMemberId() {
    return memberId;
  }

  
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }
  
  private static Set<String> toStringFieldNamesToIgnore = GrouperClientUtils.toSet();
  
  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, toStringFieldNamesToIgnore);
  }


  /**
   * if this is incremental, and syncing memberships for this group
   * @return
   */
  public boolean isIncrementalSyncMemberships() {
    return incrementalSyncMemberships;
  }


  /**
   * if recalcing the entity memberships 
   * @return
   */
  public boolean isRecalcEntityMemberships() {
    return recalcEntityMemberships;
  }


  /**
   * if this is incremental, and syncing memberships for this group
   * @param incrementalSyncMemberships1
   */
  public void setIncrementalSyncMemberships(boolean incrementalSyncMemberships1) {
    this.incrementalSyncMemberships = incrementalSyncMemberships1;
  }


  /**
   * if recalcing the entity memberships 
   * @param recalcEntityMemberships1
   */
  public void setRecalcEntityMemberships(boolean recalcEntityMemberships1) {
    this.recalcEntityMemberships = recalcEntityMemberships1;
  }

}
