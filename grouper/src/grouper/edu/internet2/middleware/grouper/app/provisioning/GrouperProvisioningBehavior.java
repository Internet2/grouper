package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

/**
 * how this provisioner interacts with the target.
 * some of these things default to the common configuration
 * @author mchyzer-local
 *
 */
public class GrouperProvisioningBehavior {

  /**
   * 
   */
  private GrouperProvisioningType grouperProvisioningType;
  
  
  public GrouperProvisioningType getGrouperProvisioningType() {
    return grouperProvisioningType;
  }

  
  public void setGrouperProvisioningType(GrouperProvisioningType grouperProvisioningType) {
    this.grouperProvisioningType = grouperProvisioningType;
  }


  private GrouperProvisioner grouperProvisioner;
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public GrouperProvisioningBehavior(GrouperProvisioner grouperProvisioner) {
    super();
    this.grouperProvisioner = grouperProvisioner;
  }

  public GrouperProvisioningBehavior() {
    super();
    // TODO Auto-generated constructor stub
  }

  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  public GrouperProvisioningBehaviorMembershipType getGrouperProvisioningBehaviorMembershipType() {
    return grouperProvisioningBehaviorMembershipType;
  }
  
  public void setGrouperProvisioningBehaviorMembershipType(
      GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType) {
    this.grouperProvisioningBehaviorMembershipType = grouperProvisioningBehaviorMembershipType;
  }

  private GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType;

  private Boolean entitiesRetrieve;

  
  public Boolean getEntitiesRetrieve() {
    return entitiesRetrieve;
  }


  
  public void setEntitiesRetrieve(Boolean entitiesRetrieve) {
    this.entitiesRetrieve = entitiesRetrieve;
  }

  private Boolean membershipsRetrieve;

  
  
  public Boolean getMembershipsRetrieve() {
    return membershipsRetrieve;
  }


  
  public void setMembershipsRetrieve(Boolean membershipsRetrieve) {
    this.membershipsRetrieve = membershipsRetrieve;
  }


  private Boolean groupsRetrieve;

  
  public Boolean getGroupsRetrieve() {
    return groupsRetrieve;
  }


  
  public void setGroupsRetrieve(Boolean groupsRetrieve) {
    this.groupsRetrieve = groupsRetrieve;
  }


  private Boolean groupsRetrieveAll;

  private Set<String> groupsRetrieveAttributes;

  private Set<String> groupsRetrieveFields;

  private Boolean groupsUpdate;

  private Set<String> groupsUpdateAttributes;

  private Set<String> groupsUpdateFields;
  
  private Boolean groupsInsert;

  private Set<String> groupsInsertAttributes;

  private Set<String> groupsInsertFields;
  
  private Boolean groupsDeleteIfNotInGrouper;
  
  private Boolean groupsDeleteIfDeletedFromGrouper;

  private Boolean entitiesRetrieveAll;

  private Set<String> entitiesRetrieveAttributes;

  private Set<String> entitiesRetrieveFields;

  private Boolean entitiesUpdate;

  private Set<String> entitiesUpdateAttributes;

  private Set<String> entitiesUpdateFields;
  
  private Boolean entitiesInsert;

  private Set<String> entitiesInsertAttributes;

  private Set<String> entitiesInsertFields;
  
  private Boolean entitiesDeleteIfNotInGrouper;
  
  private Boolean entitiesDeleteIfDeletedFromGrouper;

  private Boolean membershipsRetrieveAll;

  private Set<String> membershipsRetrieveAttributes;

  private Set<String> membershipsRetrieveFields;

  private Boolean membershipsUpdate;

  private Set<String> membershipsUpdateAttributes;

  private Set<String> membershipsUpdateFields;
  
  private Boolean membershipsInsert;

  private Set<String> membershipsInsertAttributes;

  private Set<String> membershipsInsertFields;
  
  private Boolean membershipsDeleteIfNotInGrouper;
  
  private Boolean membershipsDeleteIfDeletedFromGrouper;

  
  public Boolean getGroupsRetrieveAll() {
    return groupsRetrieveAll;
  }

  
  public void setGroupsRetrieveAll(Boolean groupsRetrieveAll) {
    this.groupsRetrieveAll = groupsRetrieveAll;
  }

  
  public Set<String> getGroupsRetrieveAttributes() {
    return groupsRetrieveAttributes;
  }

  
  public void setGroupsRetrieveAttributes(Set<String> groupsRetrieveAttributes) {
    this.groupsRetrieveAttributes = groupsRetrieveAttributes;
  }

  
  public Set<String> getGroupsRetrieveFields() {
    return groupsRetrieveFields;
  }

  
  public void setGroupsRetrieveFields(Set<String> groupsRetrieveFields) {
    this.groupsRetrieveFields = groupsRetrieveFields;
  }

  
  public Boolean getGroupsUpdate() {
    return groupsUpdate;
  }

  
  public void setGroupsUpdate(Boolean groupsUpdate) {
    this.groupsUpdate = groupsUpdate;
  }

  
  public Set<String> getGroupsUpdateAttributes() {
    return groupsUpdateAttributes;
  }

  
  public void setGroupsUpdateAttributes(Set<String> groupsUpdateAttributes) {
    this.groupsUpdateAttributes = groupsUpdateAttributes;
  }

  
  public Set<String> getGroupsUpdateFields() {
    return groupsUpdateFields;
  }

  
  public void setGroupsUpdateFields(Set<String> groupsUpdateFields) {
    this.groupsUpdateFields = groupsUpdateFields;
  }

  
  public Boolean getGroupsInsert() {
    return groupsInsert;
  }

  
  public void setGroupsInsert(Boolean groupsInsert) {
    this.groupsInsert = groupsInsert;
  }

  
  public Set<String> getGroupsInsertAttributes() {
    return groupsInsertAttributes;
  }

  
  public void setGroupsInsertAttributes(Set<String> groupsInsertAttributes) {
    this.groupsInsertAttributes = groupsInsertAttributes;
  }

  
  public Set<String> getGroupsInsertFields() {
    return groupsInsertFields;
  }

  
  public void setGroupsInsertFields(Set<String> groupsInsertFields) {
    this.groupsInsertFields = groupsInsertFields;
  }

  
  public Boolean getGroupsDeleteIfNotInGrouper() {
    return groupsDeleteIfNotInGrouper;
  }

  
  public void setGroupsDeleteIfNotInGrouper(Boolean groupsDeleteIfNotInGrouper) {
    this.groupsDeleteIfNotInGrouper = groupsDeleteIfNotInGrouper;
  }

  
  public Boolean getGroupsDeleteIfDeletedFromGrouper() {
    return groupsDeleteIfDeletedFromGrouper;
  }

  
  public void setGroupsDeleteIfDeletedFromGrouper(
      Boolean groupsDeleteIfDeletedFromGrouper) {
    this.groupsDeleteIfDeletedFromGrouper = groupsDeleteIfDeletedFromGrouper;
  }

  
  public Boolean getEntitiesRetrieveAll() {
    return entitiesRetrieveAll;
  }

  
  public void setEntitiesRetrieveAll(Boolean entitiesRetrieveAll) {
    this.entitiesRetrieveAll = entitiesRetrieveAll;
  }

  
  public Set<String> getEntitiesRetrieveAttributes() {
    return entitiesRetrieveAttributes;
  }

  
  public void setEntitiesRetrieveAttributes(Set<String> entitiesRetrieveAttributes) {
    this.entitiesRetrieveAttributes = entitiesRetrieveAttributes;
  }

  
  public Set<String> getEntitiesRetrieveFields() {
    return entitiesRetrieveFields;
  }

  
  public void setEntitiesRetrieveFields(Set<String> entitiesRetrieveFields) {
    this.entitiesRetrieveFields = entitiesRetrieveFields;
  }

  
  public Boolean getEntitiesUpdate() {
    return entitiesUpdate;
  }

  
  public void setEntitiesUpdate(Boolean entitiesUpdate) {
    this.entitiesUpdate = entitiesUpdate;
  }

  
  public Set<String> getEntitiesUpdateAttributes() {
    return entitiesUpdateAttributes;
  }

  
  public void setEntitiesUpdateAttributes(Set<String> entitiesUpdateAttributes) {
    this.entitiesUpdateAttributes = entitiesUpdateAttributes;
  }

  
  public Set<String> getEntitiesUpdateFields() {
    return entitiesUpdateFields;
  }

  
  public void setEntitiesUpdateFields(Set<String> entitiesUpdateFields) {
    this.entitiesUpdateFields = entitiesUpdateFields;
  }

  
  public Boolean getEntitiesInsert() {
    return entitiesInsert;
  }

  
  public void setEntitiesInsert(Boolean entitiesInsert) {
    this.entitiesInsert = entitiesInsert;
  }

  
  public Set<String> getEntitiesInsertAttributes() {
    return entitiesInsertAttributes;
  }

  
  public void setEntitiesInsertAttributes(Set<String> entitiesInsertAttributes) {
    this.entitiesInsertAttributes = entitiesInsertAttributes;
  }

  
  public Set<String> getEntitiesInsertFields() {
    return entitiesInsertFields;
  }

  
  public void setEntitiesInsertFields(Set<String> entitiesInsertFields) {
    this.entitiesInsertFields = entitiesInsertFields;
  }

  
  public Boolean getEntitiesDeleteIfNotInGrouper() {
    return entitiesDeleteIfNotInGrouper;
  }

  
  public void setEntitiesDeleteIfNotInGrouper(Boolean entitiesDeleteIfNotInGrouper) {
    this.entitiesDeleteIfNotInGrouper = entitiesDeleteIfNotInGrouper;
  }

  
  public Boolean getEntitiesDeleteIfDeletedFromGrouper() {
    return entitiesDeleteIfDeletedFromGrouper;
  }

  
  public void setEntitiesDeleteIfDeletedFromGrouper(
      Boolean entitiesDeleteIfDeletedFromGrouper) {
    this.entitiesDeleteIfDeletedFromGrouper = entitiesDeleteIfDeletedFromGrouper;
  }

  
  public Boolean getMembershipsRetrieveAll() {
    return membershipsRetrieveAll;
  }

  
  public void setMembershipsRetrieveAll(Boolean membershipsRetrieveAll) {
    this.membershipsRetrieveAll = membershipsRetrieveAll;
  }

  
  public Set<String> getMembershipsRetrieveAttributes() {
    return membershipsRetrieveAttributes;
  }

  
  public void setMembershipsRetrieveAttributes(Set<String> membershipsRetrieveAttributes) {
    this.membershipsRetrieveAttributes = membershipsRetrieveAttributes;
  }

  
  public Set<String> getMembershipsRetrieveFields() {
    return membershipsRetrieveFields;
  }

  
  public void setMembershipsRetrieveFields(Set<String> membershipsRetrieveFields) {
    this.membershipsRetrieveFields = membershipsRetrieveFields;
  }

  
  public Boolean getMembershipsUpdate() {
    return membershipsUpdate;
  }

  
  public void setMembershipsUpdate(Boolean membershipsUpdate) {
    this.membershipsUpdate = membershipsUpdate;
  }

  
  public Set<String> getMembershipsUpdateAttributes() {
    return membershipsUpdateAttributes;
  }

  
  public void setMembershipsUpdateAttributes(Set<String> membershipsUpdateAttributes) {
    this.membershipsUpdateAttributes = membershipsUpdateAttributes;
  }

  
  public Set<String> getMembershipsUpdateFields() {
    return membershipsUpdateFields;
  }

  
  public void setMembershipsUpdateFields(Set<String> membershipsUpdateFields) {
    this.membershipsUpdateFields = membershipsUpdateFields;
  }

  
  public Boolean getMembershipsInsert() {
    return membershipsInsert;
  }

  
  public void setMembershipsInsert(Boolean membershipsInsert) {
    this.membershipsInsert = membershipsInsert;
  }

  
  public Set<String> getMembershipsInsertAttributes() {
    return membershipsInsertAttributes;
  }

  
  public void setMembershipsInsertAttributes(Set<String> membershipsInsertAttributes) {
    this.membershipsInsertAttributes = membershipsInsertAttributes;
  }

  
  public Set<String> getMembershipsInsertFields() {
    return membershipsInsertFields;
  }

  
  public void setMembershipsInsertFields(Set<String> membershipsInsertFields) {
    this.membershipsInsertFields = membershipsInsertFields;
  }

  
  public Boolean getMembershipsDeleteIfNotInGrouper() {
    return membershipsDeleteIfNotInGrouper;
  }

  
  public void setMembershipsDeleteIfNotInGrouper(Boolean membershipsDeleteIfNotInGrouper) {
    this.membershipsDeleteIfNotInGrouper = membershipsDeleteIfNotInGrouper;
  }

  
  public Boolean getMembershipsDeleteIfDeletedFromGrouper() {
    return membershipsDeleteIfDeletedFromGrouper;
  }

  
  public void setMembershipsDeleteIfDeletedFromGrouper(
      Boolean membershipsDeleteIfDeletedFromGrouper) {
    this.membershipsDeleteIfDeletedFromGrouper = membershipsDeleteIfDeletedFromGrouper;
  }

  
}
