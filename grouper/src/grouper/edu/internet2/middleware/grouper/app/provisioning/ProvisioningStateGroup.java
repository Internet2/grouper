package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningStateGroup extends ProvisioningStateBase {

  /**
   * deletes the attribute on groups who we arent deleting
   * note the sync object can be null if it is from target and grouper doesnt know about it
   */
  private boolean deleteMembershipAttributeValues;

  /**
   * deletes the attribute on groups who we arent deleting
   * note the sync object can be null if it is from target and grouper doesnt know about it
   * @return
   */
  public boolean isDeleteMembershipAttributeValues() {
    return deleteMembershipAttributeValues;
  }

  /**
   * deletes the attribute on groups who we arent deleting
   * note the sync object can be null if it is from target and grouper doesnt know about it
   * @param deleteMembershipAttributeValues
   */
  public void setDeleteMembershipAttributeValues(boolean deleteMembershipAttributeValues) {
    this.deleteMembershipAttributeValues = deleteMembershipAttributeValues;
  }

  public GrouperProvisioner getGrouperProvisioner() {
    return this.getProvisioningGroupWrapper().getGrouperProvisioner();
  }
  
  /**
   * see if loggable if not logging all objects
   * @return
   */
  public boolean isLoggable(boolean strong) {
    
    if (retrieveLoggableCache(strong)) {
      return true;
    }

    // if not filtering based on group
    Set<String> logAllObjectsVerboseForTheseGroupNames = this.getProvisioningGroupWrapper().getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseGroupNames();
    Set<String> logAllObjectsVerboseGroupAttributes = this.getProvisioningGroupWrapper().getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseGroupAttributes();

    String membershipAttribute = null;
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      membershipAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    }

    Set<String> logAllObjectsVerboseForTheseSubjectIds = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseSubjectIds();


    if (!strong && membershipAttribute == null || GrouperUtil.length(logAllObjectsVerboseForTheseSubjectIds) == 0) {
      return false;
    }
    
    if (strong && GrouperUtil.length(logAllObjectsVerboseForTheseGroupNames) == 0) {
      this.assignLoggableCache(strong);
      return true;
    }
    
    if (this.getProvisioningGroupWrapper().getGrouperTargetGroup() != null) {
      if (strong && this.getProvisioningGroupWrapper().getGrouperTargetGroup().matchesAttribute(logAllObjectsVerboseGroupAttributes, logAllObjectsVerboseForTheseGroupNames)) {
        this.assignLoggableCache(strong);
        return true;
      }
      if (!strong && this.getProvisioningGroupWrapper().getGrouperTargetGroup().matchesAttribute(membershipAttribute, logAllObjectsVerboseForTheseSubjectIds)) {
        this.assignLoggableCache(strong);
        return true;
      }
    }

    if (this.getProvisioningGroupWrapper().getTargetProvisioningGroup() != null) {
      if (strong && this.getProvisioningGroupWrapper().getTargetProvisioningGroup().matchesAttribute(logAllObjectsVerboseGroupAttributes, logAllObjectsVerboseForTheseGroupNames)) {
        this.assignLoggableCache(strong);
        return true;
      }
      if (!strong && this.getProvisioningGroupWrapper().getTargetProvisioningGroup().matchesAttribute(membershipAttribute, logAllObjectsVerboseForTheseSubjectIds)) {
        this.assignLoggableCache(strong);
        return true;
      }
    }

    if (this.getProvisioningGroupWrapper().getGrouperProvisioningGroup() != null) {
      if (strong && this.getProvisioningGroupWrapper().getGrouperProvisioningGroup().isLoggableHelper()) {
        this.assignLoggableCache(strong);
        return true;
      }
    }

    if (this.getProvisioningGroupWrapper().getGcGrouperSyncGroup() != null) {
      if (strong && this.getProvisioningGroupWrapper().getGcGrouperSyncGroup().matchesAttribute(logAllObjectsVerboseGroupAttributes, logAllObjectsVerboseForTheseGroupNames)) {
        this.assignLoggableCache(strong);
        return true;
      }
    }
    return false;
  }


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
  
  
  private static Set<String> toStringFieldNamesToIgnore = GrouperClientUtils.toSet("provisioningGroupWrapper", "loggableStrong", "loggableWeak");
  
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
  
  private boolean groupRemovedDueToAttribute;
  
  public boolean isGroupRemovedDueToAttribute() {
    return groupRemovedDueToAttribute;
  }

  
  public void setGroupRemovedDueToAttribute(boolean groupRemovedDueToAttribute) {
    this.groupRemovedDueToAttribute = groupRemovedDueToAttribute;
  }

  /**
   * if this object is for create, and we translate memberships, then dont do it again
   */
  private boolean translatedMemberships = false;

  /**
   * if this object is for create, and we translate memberships, then dont do it again
   * @return
   */
  public boolean isTranslatedMemberships() {
    return translatedMemberships;
  }

  /**
   * if this object is for create, and we translate memberships, then dont do it again
   * @param theTranslatedMemberships
   */
  public void setTranslatedMemberships(boolean theTranslatedMemberships) {
    this.translatedMemberships = theTranslatedMemberships;
  }
  
  private Boolean groupExistsInGrouper = null;

  /**
   * Does this group exist in grouper.  null means it hasn't been checked.
   * @return boolean
   */
  public Boolean isGroupExistsInGrouper() {
    return groupExistsInGrouper;
  }

  /**
   * @param groupExistsInGrouper
   */
  public void setGroupExistsInGrouper(Boolean groupExistsInGrouper) {
    this.groupExistsInGrouper = groupExistsInGrouper;
  }
  
  
}
