package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisionerDaoCapabilities {

  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    Set<String> fieldNames = GrouperUtil.fieldNames(GrouperProvisionerDaoCapabilities.class, null, false);
        
    fieldNames = new TreeSet<String>(fieldNames);
    boolean firstField = true;
    for (String fieldName : fieldNames) {
      
      Object value = GrouperUtil.propertyValue(this, fieldName);
      if (!GrouperUtil.isBlank(value)) {
        
        if ((value instanceof Collection) && ((Collection)value).size() == 0) {
          continue;
        }
        if ((value instanceof Map) && ((Map)value).size() == 0) {
          continue;
        }
        if ((value.getClass().isArray()) && Array.getLength(value) == 0) {
          continue;
        }
        
        if (!firstField) {
          result.append(", ");
        }
        firstField = false;
        result.append(fieldName).append(" = '").append(GrouperUtil.toStringForLog(value, false)).append("'");
      }
    }
    
    return result.toString();
  }

  private Boolean canDeleteEntities;
  
  private Boolean canDeleteEntity;
  
  private Boolean canDeleteGroup;
  
  private Boolean canDeleteGroups;
  
  private Boolean canDeleteMembership;
  
  private Boolean canDeleteMemberships;
  
  private Boolean canInsertEntities;
  private Boolean canInsertEntity;
  private Boolean canInsertGroup;
  private Boolean canInsertGroups;
  private Boolean canInsertMembership;
  private Boolean canInsertMemberships;
  private Boolean canRetrieveAllData;
  private Boolean canRetrieveAllEntities;
  private Boolean canRetrieveAllGroups;
  private Boolean canRetrieveAllMemberships;
  private Boolean canRetrieveEntities;
  private Boolean canRetrieveEntity;
  private Boolean canRetrieveGroup;
  private Boolean canRetrieveGroupWithOrWithoutMembershipAttribute;
  private Boolean canRetrieveEntityWithOrWithoutMembershipAttribute;
  private Boolean canRetrieveGroups;
  private Boolean canRetrieveIncrementalData;
  private Boolean canRetrieveMembership;
  private Boolean canRetrieveMemberships;
  private Boolean canRetrieveMembershipsBulk;
  private Boolean canRetrieveMembershipsByEntities;
  private Boolean canRetrieveMembershipsByEntity;
  private Boolean canRetrieveMembershipsByGroup;
  private Boolean canRetrieveMembershipsByGroups;
  private Boolean canRetrieveMembershipsByTargetGroupEntityMembership;
  private Boolean canSendChangesToTarget;
  private Boolean canSendEntityChangesToTarget;
  private Boolean canSendGroupChangesToTarget;
  private Boolean canSendMembershipChangesToTarget;
  private Boolean canUpdateEntities;
  private Boolean canUpdateEntity;
  private Boolean canUpdateGroup;
  private Boolean canUpdateGroups;
  private Boolean canUpdateEntityMembershipAttribute;
  private Boolean canUpdateGroupMembershipAttribute;
  private Boolean canUpdateMembership;
  private Boolean canUpdateMemberships;
  private Boolean canReplaceGroupMemberships;
  
  public Boolean getCanReplaceGroupMemberships() {
    return canReplaceGroupMemberships;
  }
  
  public void setCanReplaceGroupMemberships(Boolean canReplaceGroupMemberships) {
    this.canReplaceGroupMemberships = canReplaceGroupMemberships;
  }

  public Boolean getCanDeleteEntities() {
    return canDeleteEntities;
  }
  
  public void setCanDeleteEntities(Boolean canDeleteEntities) {
    this.canDeleteEntities = canDeleteEntities;
  }
  
  public Boolean getCanDeleteEntity() {
    return canDeleteEntity;
  }
  
  public void setCanDeleteEntity(Boolean canDeleteEntity) {
    this.canDeleteEntity = canDeleteEntity;
  }
  
  public Boolean getCanDeleteGroup() {
    return canDeleteGroup;
  }
  
  public void setCanDeleteGroup(Boolean canDeleteGroup) {
    this.canDeleteGroup = canDeleteGroup;
  }
  
  public Boolean getCanDeleteGroups() {
    return canDeleteGroups;
  }
  
  public void setCanDeleteGroups(Boolean canDeleteGroups) {
    this.canDeleteGroups = canDeleteGroups;
  }
  
  public Boolean getCanDeleteMembership() {
    return canDeleteMembership;
  }
  
  public void setCanDeleteMembership(Boolean canDeleteMembership) {
    this.canDeleteMembership = canDeleteMembership;
  }
  
  public Boolean getCanDeleteMemberships() {
    return canDeleteMemberships;
  }
  
  public void setCanDeleteMemberships(Boolean canDeleteMemberships) {
    this.canDeleteMemberships = canDeleteMemberships;
  }
  
  public Boolean getCanInsertEntities() {
    return canInsertEntities;
  }
  
  public void setCanInsertEntities(Boolean canInsertEntities) {
    this.canInsertEntities = canInsertEntities;
  }
  
  public Boolean getCanInsertEntity() {
    return canInsertEntity;
  }
  
  public void setCanInsertEntity(Boolean canInsertEntity) {
    this.canInsertEntity = canInsertEntity;
  }
  
  public Boolean getCanInsertGroup() {
    return canInsertGroup;
  }
  
  public void setCanInsertGroup(Boolean canInsertGroup) {
    this.canInsertGroup = canInsertGroup;
  }
  
  public Boolean getCanInsertGroups() {
    return canInsertGroups;
  }
  
  public void setCanInsertGroups(Boolean canInsertGroups) {
    this.canInsertGroups = canInsertGroups;
  }
  
  public Boolean getCanInsertMembership() {
    return canInsertMembership;
  }
  
  public void setCanInsertMembership(Boolean canInsertMembership) {
    this.canInsertMembership = canInsertMembership;
  }
  
  public Boolean getCanInsertMemberships() {
    return canInsertMemberships;
  }
  
  public void setCanInsertMemberships(Boolean canInsertMemberships) {
    this.canInsertMemberships = canInsertMemberships;
  }
  
  public Boolean getCanRetrieveAllData() {
    return canRetrieveAllData;
  }
  
  public void setCanRetrieveAllData(Boolean canRetrieveAllData) {
    this.canRetrieveAllData = canRetrieveAllData;
  }
  
  public Boolean getCanRetrieveAllEntities() {
    return canRetrieveAllEntities;
  }
  
  public void setCanRetrieveAllEntities(Boolean canRetrieveAllEntities) {
    this.canRetrieveAllEntities = canRetrieveAllEntities;
  }
  
  public Boolean getCanRetrieveAllGroups() {
    return canRetrieveAllGroups;
  }
  
  public void setCanRetrieveAllGroups(Boolean canRetrieveAllGroups) {
    this.canRetrieveAllGroups = canRetrieveAllGroups;
  }
  
  public Boolean getCanRetrieveAllMemberships() {
    return canRetrieveAllMemberships;
  }
  
  public void setCanRetrieveAllMemberships(Boolean canRetrieveAllMemberships) {
    this.canRetrieveAllMemberships = canRetrieveAllMemberships;
  }
  
  public Boolean getCanRetrieveEntities() {
    return canRetrieveEntities;
  }
  
  public void setCanRetrieveEntities(Boolean canRetrieveEntities) {
    this.canRetrieveEntities = canRetrieveEntities;
  }
  
  public Boolean getCanRetrieveEntity() {
    return canRetrieveEntity;
  }
  
  public void setCanRetrieveEntity(Boolean canRetrieveEntity) {
    this.canRetrieveEntity = canRetrieveEntity;
  }
  
  public Boolean getCanRetrieveGroup() {
    return canRetrieveGroup;
  }
  
  public void setCanRetrieveGroup(Boolean canRetrieveGroup) {
    this.canRetrieveGroup = canRetrieveGroup;
  }
  
  public Boolean getCanRetrieveGroupWithOrWithoutMembershipAttribute() {
    return canRetrieveGroupWithOrWithoutMembershipAttribute;
  }
  
  public void setCanRetrieveGroupWithOrWithoutMembershipAttribute(
      Boolean canRetrieveGroupWithOrWithoutMembershipAttribute) {
    this.canRetrieveGroupWithOrWithoutMembershipAttribute = canRetrieveGroupWithOrWithoutMembershipAttribute;
  }
  
  public Boolean getCanRetrieveEntityWithOrWithoutMembershipAttribute() {
    return canRetrieveEntityWithOrWithoutMembershipAttribute;
  }
  
  public void setCanRetrieveEntityWithOrWithoutMembershipAttribute(
      Boolean canRetrieveEntityWithOrWithoutMembershipAttribute) {
    this.canRetrieveEntityWithOrWithoutMembershipAttribute = canRetrieveEntityWithOrWithoutMembershipAttribute;
  }
  
  public Boolean getCanRetrieveGroups() {
    return canRetrieveGroups;
  }
  
  public void setCanRetrieveGroups(Boolean canRetrieveGroups) {
    this.canRetrieveGroups = canRetrieveGroups;
  }
  
  public Boolean getCanRetrieveIncrementalData() {
    return canRetrieveIncrementalData;
  }
  
  public void setCanRetrieveIncrementalData(Boolean canRetrieveIncrementalData) {
    this.canRetrieveIncrementalData = canRetrieveIncrementalData;
  }
  
  public Boolean getCanRetrieveMembership() {
    return canRetrieveMembership;
  }
  
  public void setCanRetrieveMembership(Boolean canRetrieveMembership) {
    this.canRetrieveMembership = canRetrieveMembership;
  }
  
  public Boolean getCanRetrieveMemberships() {
    return canRetrieveMemberships;
  }
  
  public void setCanRetrieveMemberships(Boolean canRetrieveMemberships) {
    this.canRetrieveMemberships = canRetrieveMemberships;
  }
  
  public Boolean getCanRetrieveMembershipsBulk() {
    return canRetrieveMembershipsBulk;
  }
  
  public void setCanRetrieveMembershipsBulk(Boolean canRetrieveMembershipsBulk) {
    this.canRetrieveMembershipsBulk = canRetrieveMembershipsBulk;
  }
  
  public Boolean getCanRetrieveMembershipsByEntities() {
    return canRetrieveMembershipsByEntities;
  }
  
  public void setCanRetrieveMembershipsByEntities(
      Boolean canRetrieveMembershipsByEntities) {
    this.canRetrieveMembershipsByEntities = canRetrieveMembershipsByEntities;
  }
  
  public Boolean getCanRetrieveMembershipsByEntity() {
    return canRetrieveMembershipsByEntity;
  }
  
  public void setCanRetrieveMembershipsByEntity(Boolean canRetrieveMembershipsByEntity) {
    this.canRetrieveMembershipsByEntity = canRetrieveMembershipsByEntity;
  }
  
  public Boolean getCanRetrieveMembershipsByGroup() {
    return canRetrieveMembershipsByGroup;
  }
  
  public void setCanRetrieveMembershipsByGroup(Boolean canRetrieveMembershipsByGroup) {
    this.canRetrieveMembershipsByGroup = canRetrieveMembershipsByGroup;
  }
  
  public Boolean getCanRetrieveMembershipsByGroups() {
    return canRetrieveMembershipsByGroups;
  }
  
  public void setCanRetrieveMembershipsByGroups(Boolean canRetrieveMembershipsByGroups) {
    this.canRetrieveMembershipsByGroups = canRetrieveMembershipsByGroups;
  }
  
  public Boolean getCanRetrieveMembershipsByTargetGroupEntityMembership() {
    return canRetrieveMembershipsByTargetGroupEntityMembership;
  }
  
  public void setCanRetrieveMembershipsByTargetGroupEntityMembership(
      Boolean canRetrieveMembershipsByTargetGroupEntityMembership) {
    this.canRetrieveMembershipsByTargetGroupEntityMembership = canRetrieveMembershipsByTargetGroupEntityMembership;
  }
  
  public Boolean getCanSendChangesToTarget() {
    return canSendChangesToTarget;
  }
  
  public void setCanSendChangesToTarget(Boolean canSendChangesToTarget) {
    this.canSendChangesToTarget = canSendChangesToTarget;
  }
  
  public Boolean getCanSendEntityChangesToTarget() {
    return canSendEntityChangesToTarget;
  }
  
  public void setCanSendEntityChangesToTarget(Boolean canSendEntityChangesToTarget) {
    this.canSendEntityChangesToTarget = canSendEntityChangesToTarget;
  }
  
  public Boolean getCanSendGroupChangesToTarget() {
    return canSendGroupChangesToTarget;
  }
  
  public void setCanSendGroupChangesToTarget(Boolean canSendGroupChangesToTarget) {
    this.canSendGroupChangesToTarget = canSendGroupChangesToTarget;
  }
  
  public Boolean getCanSendMembershipChangesToTarget() {
    return canSendMembershipChangesToTarget;
  }
  
  public void setCanSendMembershipChangesToTarget(
      Boolean canSendMembershipChangesToTarget) {
    this.canSendMembershipChangesToTarget = canSendMembershipChangesToTarget;
  }
  
  public Boolean getCanUpdateEntities() {
    return canUpdateEntities;
  }
  
  public void setCanUpdateEntities(Boolean canUpdateEntities) {
    this.canUpdateEntities = canUpdateEntities;
  }
  
  public Boolean getCanUpdateEntity() {
    return canUpdateEntity;
  }
  
  public void setCanUpdateEntity(Boolean canUpdateEntity) {
    this.canUpdateEntity = canUpdateEntity;
  }
  
  public Boolean getCanUpdateGroup() {
    return canUpdateGroup;
  }
  
  public void setCanUpdateGroup(Boolean canUpdateGroup) {
    this.canUpdateGroup = canUpdateGroup;
  }
  
  public Boolean getCanUpdateGroups() {
    return canUpdateGroups;
  }
  
  public void setCanUpdateGroups(Boolean canUpdateGroups) {
    this.canUpdateGroups = canUpdateGroups;
  }
  
  public Boolean getCanUpdateEntityMembershipAttribute() {
    return canUpdateEntityMembershipAttribute;
  }
  
  public void setCanUpdateEntityMembershipAttribute(
      Boolean canUpdateEntityMembershipAttribute) {
    this.canUpdateEntityMembershipAttribute = canUpdateEntityMembershipAttribute;
  }
  
  public Boolean getCanUpdateGroupMembershipAttribute() {
    return canUpdateGroupMembershipAttribute;
  }
  
  public void setCanUpdateGroupMembershipAttribute(
      Boolean canUpdateGroupMembershipAttribute) {
    this.canUpdateGroupMembershipAttribute = canUpdateGroupMembershipAttribute;
  }
  
  public Boolean getCanUpdateMembership() {
    return canUpdateMembership;
  }
  
  public void setCanUpdateMembership(Boolean canUpdateMembership) {
    this.canUpdateMembership = canUpdateMembership;
  }
  
  public Boolean getCanUpdateMemberships() {
    return canUpdateMemberships;
  }
  
  public void setCanUpdateMemberships(Boolean canUpdateMemberships) {
    this.canUpdateMemberships = canUpdateMemberships;
  }
  
  
}
