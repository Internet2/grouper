package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningStateGroup extends ProvisioningStateBase {

  public ProvisioningStateGroup() {
    
  }

  private boolean selectSomeMembershipsResultProcessed;

  
  
  
  public boolean isSelectSomeMembershipsResultProcessed() {
    return selectSomeMembershipsResultProcessed;
  }

  
  public void setSelectSomeMembershipsResultProcessed(
      boolean selectSomeMembershipsResultProcessed) {
    this.selectSomeMembershipsResultProcessed = selectSomeMembershipsResultProcessed;
  }

  /**
   * in incremental, if we're doing group attributes and any membership events for this group 
   * are recalc, then recalc all events for this group in this incremental run and select
   * those memberships from the target. Since this is group attributes, we need to select those via
   * the group 
   */
  private boolean selectSomeMemberships;
  
  /**
   * set it to true if we want to select all memberships from the target for this group.
   * This happens when there are too many events for this group or if it's a new group or a deleted group
   * or manually kicked off from the UI
   */
  private boolean selectAllMemberships;
  
  /**
   * if the memberships for this group was attempted to be selected from target
   */
  private boolean selectAllMembershipsResultProcessed;
  
  /**
   * set it to true if we want to select all memberships from the target for this group.
   * This happens when there are too many events for this group or if it's a new group or a deleted group
   * or manually kicked off from the UI
   */
  public boolean isSelectAllMemberships() {
    return selectAllMemberships;
  }

  /**
   * set it to true if we want to select all memberships from the target for this group.
   * This happens when there are too many events for this group or if it's a new group or a deleted group
   * or manually kicked off from the UI
   */
  public void setSelectAllMemberships(boolean selectMemberships) {
    this.selectAllMemberships = selectMemberships;
  }
  
  /**
   * if the memberships for this group was attempted to be selected from target
   */
  public boolean isSelectAllMembershipsResultProcessed() {
    return selectAllMembershipsResultProcessed;
  }
  
  
  /**
   * in incremental, if we're doing group attributes and any membership events for this group 
   * are recalc, then recalc all events for this group in this incremental run and select
   * those memberships from the target. Since this is group attributes, we need to select those via
   * the group 
   */
  public boolean isSelectSomeMemberships() {
    return selectSomeMemberships;
  }

  /**
   * in incremental, if we're doing group attributes and any membership events for this group 
   * are recalc, then recalc all events for this group in this incremental run and select
   * those memberships from the target. Since this is group attributes, we need to select those via
   * the group 
   */
  public void setSelectSomeMemberships(boolean selectSomeMemberships) {
    this.selectSomeMemberships = selectSomeMemberships;
  }

  /**
   * if the memberships for this group was attempted to be selected from target
   */
  public void setSelectAllMembershipsResultProcessed(boolean selectMembershipResultProcessed) {
    this.selectAllMembershipsResultProcessed = selectMembershipResultProcessed;
  }

  /**
   * if recalcing the groupAttribute memberships 
   */
  private boolean recalcGroupMemberships;
  private String groupId;
  
  
  private static Set<String> toStringFieldNamesToIgnore = GrouperClientUtils.toSet("provisioningGroupWrapper");
  
  private ProvisioningGroupWrapper provisioningGroupWrapper = null;
  
  public ProvisioningGroupWrapper getProvisioningGroupWrapper() {
    return provisioningGroupWrapper;
  }


  
  public void setProvisioningGroupWrapper(
      ProvisioningGroupWrapper provisioningGroupWrapper) {
    this.provisioningGroupWrapper = provisioningGroupWrapper;
  }


  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, toStringFieldNamesToIgnore, "id='" + this.getProvisioningGroupWrapper().getGroupId() + "'");
  }


  /**
   * if recalcing the groupAttribute memberships 
   * @return
   */
  public boolean isRecalcGroupMemberships() {
    return recalcGroupMemberships;
  }


  /**
   * if recalcing the group memberships 
   * @param recalcGroupMemberships1
   */
  public void setRecalcGroupMemberships(boolean recalcGroupMemberships1) {
    this.recalcGroupMemberships = recalcGroupMemberships1;
  }


  public String getGroupId() {
    return groupId;
  }


  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

}
