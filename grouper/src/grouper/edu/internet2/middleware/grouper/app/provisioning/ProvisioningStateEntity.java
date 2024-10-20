package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class ProvisioningStateEntity extends ProvisioningStateBase {
  
  /**
   * deletes the attribute on users who we arent deleting
   * note the sync object can be null if it is from target and grouper doesnt know about it
   */
  private boolean deleteMembershipAttributeValues;

  /**
   * deletes the attribute on users who we arent deleting
   * note the sync object can be null if it is from target and grouper doesnt know about it
   * @return
   */
  public boolean isDeleteMembershipAttributeValues() {
    return deleteMembershipAttributeValues;
  }

  /**
   * deletes the attribute on users who we arent deleting
   * note the sync object can be null if it is from target and grouper doesnt know about it
   * @param deleteMembershipAttributeValues
   */
  public void setDeleteMembershipAttributeValues(boolean deleteMembershipAttributeValues) {
    this.deleteMembershipAttributeValues = deleteMembershipAttributeValues;
  }

  public GrouperProvisioner getGrouperProvisioner() {
    return this.getProvisioningEntityWrapper().getGrouperProvisioner();
  }

  /**
   * see if loggable if not logging all objects
   * @return
   */
  public boolean isLoggable(boolean strong) {
    
    if (this.retrieveLoggableCache(strong)) {
      return true;
    }

    // if not filtering based on group
    Set<String> logAllObjectsVerboseForTheseEntityNames = this.getProvisioningEntityWrapper().getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseSubjectIds();
    Set<String> logAllObjectsVerboseEntityAttributes = this.getProvisioningEntityWrapper().getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseEntityAttributes();

    String membershipAttribute = null;
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      membershipAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    }

    Set<String> logAllObjectsVerboseForTheseGroupNames = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getLogAllObjectsVerboseForTheseGroupNames();


    if (!strong && membershipAttribute == null || GrouperUtil.length(logAllObjectsVerboseForTheseGroupNames) == 0) {
      return false;
    }

    if (strong && GrouperUtil.length(logAllObjectsVerboseForTheseEntityNames) == 0) {
      this.assignLoggableCache(true);
      return true;
    }
    
    if (this.getProvisioningEntityWrapper().getGrouperTargetEntity() != null) {
      if (strong && this.getProvisioningEntityWrapper().getGrouperTargetEntity().matchesAttribute(logAllObjectsVerboseEntityAttributes, logAllObjectsVerboseForTheseEntityNames)) {
        this.assignLoggableCache(true);
        return true;
      }
      if (!strong && this.getProvisioningEntityWrapper().getGrouperTargetEntity().matchesAttribute(membershipAttribute, logAllObjectsVerboseForTheseGroupNames)) {
        this.assignLoggableCache(strong);
        return true;
      }

    }

    if (this.getProvisioningEntityWrapper().getTargetProvisioningEntity() != null) {
      if (strong && this.getProvisioningEntityWrapper().getTargetProvisioningEntity().matchesAttribute(logAllObjectsVerboseEntityAttributes, logAllObjectsVerboseForTheseEntityNames)) {
        this.assignLoggableCache(true);
        return true;
      }
      if (!strong && this.getProvisioningEntityWrapper().getTargetProvisioningEntity().matchesAttribute(membershipAttribute, logAllObjectsVerboseForTheseGroupNames)) {
        this.assignLoggableCache(strong);
        return true;
      }
    }

    if (this.getProvisioningEntityWrapper().getGrouperProvisioningEntity() != null) {
      if (strong && this.getProvisioningEntityWrapper().getGrouperProvisioningEntity().isLoggableHelper()) {
        this.assignLoggableCache(true);
        return true;
      }
    }

    if (this.getProvisioningEntityWrapper().getGcGrouperSyncMember() != null) {
      if (strong && this.getProvisioningEntityWrapper().getGcGrouperSyncMember().matchesAttribute(logAllObjectsVerboseEntityAttributes, logAllObjectsVerboseForTheseEntityNames)) {
        this.assignLoggableCache(true);
        return true;
      }
    }
    return false;
  }


  /**
   * if this subject is marked as unresolvable in the members table
   */
  private boolean unresolvable;
  

  /**
   * if this subject is marked as unresolvable in the members table
   * @return
   */
  public boolean isUnresolvable() {
    return unresolvable;
  }

  /**
   * if this subject is marked as unresolvable in the members table
   * @param unresolvable
   */
  public void setUnresolvable(boolean unresolvable) {
    this.unresolvable = unresolvable;
  }

  /**
   * if the memberships for this entity was attempted to be selected from target
   */
  private boolean selectAllMembershipResultProcessed;
  
  /**
   * in incremental, if we're doing entity attributes and any membership events for this entity 
   * are recalc, then recalc all events for this entity in this incremental run and select
   * those memberships from the target. Since this is entity attributes, we need to select those via
   * the entity 
   */
  private boolean selectSomeMemberships;
  
  /**
   * in incremental, if we're doing entity attributes and any membership events for this entity 
   * are recalc, then recalc all events for this entity in this incremental run and select
   * those memberships from the target. Since this is entity attributes, we need to select those via
   * the entity 
   */
  public boolean isSelectSomeMemberships() {
    return selectSomeMemberships;
  }

  /**
   * in incremental, if we're doing entity attributes and any membership events for this entity 
   * are recalc, then recalc all events for this entity in this incremental run and select
   * those memberships from the target. Since this is entity attributes, we need to select those via
   * the entity 
   */
  public void setSelectSomeMemberships(boolean selectSomeMemberships) {
    this.selectSomeMemberships = selectSomeMemberships;
  }

  /**
   * set it to true if we want to select all memberships from the target for this group.
   * This happens when there are too many events for this group or if it's a new group or a deleted group
   * or manually kicked off from the UI
   */
  private boolean selectAllMemberships;
  
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
  
  
  private String memberId;
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
  
  private static Set<String> toStringFieldNamesToIgnore = GrouperClientUtils.toSet("provisioningEntityWrapper", "loggableStrong", "loggableWeak");
  
  /**
   * 
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this, toStringFieldNamesToIgnore, "id='" + this.getProvisioningEntityWrapper().getMemberId() + "'");
  }

  private ProvisioningEntityWrapper provisioningEntityWrapper = null;

  /**
   * if the memberships for this entity was attempted to be selected from target
   */
  private boolean selectAllMembershipsResultProcessed;

  private boolean selectSomeMembershipsResultProcessed;
  
  
  
  
  public ProvisioningEntityWrapper getProvisioningEntityWrapper() {
    return provisioningEntityWrapper;
  }


  
  public void setProvisioningEntityWrapper(
      ProvisioningEntityWrapper provisioningEntityWrapper) {
    this.provisioningEntityWrapper = provisioningEntityWrapper;
  }


  /**
   * if recalcing the entity memberships 
   * @return
   */
  public boolean isRecalcEntityMemberships() {
    return recalcEntityMemberships;
  }


  /**
   * if recalcing the entity memberships 
   * @param recalcEntityMemberships1
   */
  public void setRecalcEntityMemberships(boolean recalcEntityMemberships1) {
    this.recalcEntityMemberships = recalcEntityMemberships1;
  }

  
  public boolean isSelectAllMembershipResultProcessed() {
    return selectAllMembershipResultProcessed;
  }

  
  public void setSelectAllMembershipResultProcessed(
      boolean selectAllMembershipResultProcessed) {
    this.selectAllMembershipResultProcessed = selectAllMembershipResultProcessed;
  }

  /**
   * if the memberships for this entity was attempted to be selected from target
   */
  public boolean isSelectAllMembershipsResultProcessed() {
    return selectAllMembershipsResultProcessed;
  }

  public boolean isSelectSomeMembershipsResultProcessed() {
    return selectSomeMembershipsResultProcessed;
  }

  /**
   * if the memberships for this entity was attempted to be selected from target
   */
  public void setSelectAllMembershipsResultProcessed(boolean selectMembershipResultProcessed) {
    this.selectAllMembershipsResultProcessed = selectMembershipResultProcessed;
  }

  public void setSelectSomeMembershipsResultProcessed(
      boolean selectSomeMembershipsResultProcessed) {
    this.selectSomeMembershipsResultProcessed = selectSomeMembershipsResultProcessed;
  }
  
  private boolean entityRemovedDueToAttribute;
  
  public boolean isEntityRemovedDueToAttribute() {
    return entityRemovedDueToAttribute;
  }
  
  public void setEntityRemovedDueToAttribute(boolean entityRemovedDueToAttribute) {
    this.entityRemovedDueToAttribute = entityRemovedDueToAttribute;
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
  
}
