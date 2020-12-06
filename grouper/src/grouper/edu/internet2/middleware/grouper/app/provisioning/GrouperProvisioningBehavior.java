package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * how this provisioner interacts with the target.
 * some of these things default to the common configuration
 * @author mchyzer-local
 *
 */
public class GrouperProvisioningBehavior {

  /**
   * If the subject API is needed to resolve attribute on subject  required, drives requirements of other configurations. defaults to false.
   */
  private Boolean hasSubjectLink = null;
  
  /**
   * If groups need to be resolved in the target before provisioning
   */
  private Boolean hasTargetGroupLink = null;
  
  /**
   * If users need to be resolved in the target before provisioning
   */
  private Boolean hasTargetEntityLink = null;

  
  public boolean canGroupInsertField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isInsert();
  }
  public boolean canGroupInsertAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isInsert();
  }
  public boolean canGroupUpdateField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isUpdate();
  }
  public boolean canGroupUpdateAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isUpdate();
  }
  public boolean canGroupDeleteField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isDelete();
  }
  public boolean canGroupDeleteAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isDelete();
  }


  
  public boolean canEntityInsertField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isInsert();
  }
  public boolean canEntityInsertAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isInsert();
  }
  public boolean canEntityUpdateField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isUpdate();
  }
  public boolean canEntityUpdateAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isUpdate();
  }
  public boolean canEntityDeleteField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isDelete();
  }
  public boolean canEntityDeleteAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isDelete();
  }

  
  public boolean canMembershipInsertField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isInsert();
  }
  public boolean canMembershipInsertAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isInsert();
  }
  public boolean canMembershipUpdateField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isUpdate();
  }
  public boolean canMembershipUpdateAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isUpdate();
  }
  public boolean canMembershipDeleteField(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isDelete();
  }
  public boolean canMembershipDeleteAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute == null || grouperProvisioningConfigurationAttribute.isDelete();
  }

  
  public Boolean getHasTargetEntityLink() {
    if (hasTargetEntityLink != null) {
      return hasTargetEntityLink;
    }
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isHasTargetEntityLink();
  }



  
  public void setHasTargetEntityLink(Boolean hasTargetEntityLink) {
    this.hasTargetEntityLink = hasTargetEntityLink;
  }



  public Boolean getHasSubjectLink() {
    if (hasSubjectLink != null) {
      return hasSubjectLink;
    }
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isHasSubjectLink();
  }


  
  public void setHasSubjectLink(Boolean hasSubjectLink) {
    this.hasSubjectLink = hasSubjectLink;
  }


  
  public Boolean getHasTargetGroupLink() {
    if (hasTargetGroupLink != null) {
      return hasTargetGroupLink;
    }
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isHasTargetGroupLink();
  }


  
  public void setHasTargetGroupLink(Boolean hasTargetGroupLink) {
    this.hasTargetGroupLink = hasTargetGroupLink;
  }

  /**
   * 
   */
  private Boolean groupsRetrieveMissingIncremental;

  
  /**
   * 
   * @return
   */
  public Boolean getGroupsRetrieveMissingIncremental() {
    if (groupsRetrieveMissingIncremental != null) {
      return groupsRetrieveMissingIncremental;
    }
    if (!this.getGrouperProvisioningType().isIncrementalSync()) {
      return false;
    }
    if (this.getGroupsRetrieve()) {
      return true;
    }
    return null;
  }


  /**
   * 
   * @param retrieveMissingGroupsIncremental
   */
  public void setGroupsRetrieveMissingIncremental(
      Boolean retrieveMissingGroupsIncremental) {
    this.groupsRetrieveMissingIncremental = retrieveMissingGroupsIncremental;
  }


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
    if (this.entitiesRetrieve != null) {
      return this.entitiesRetrieve;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      return true;
    }
    if (GrouperUtil.booleanValue(this.getHasTargetEntityLink(), false)) {
      return true;
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveEntities(), false)
        || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveEntity(), false)) {
      return true;
    }
    return null;
  }


  
  public void setEntitiesRetrieve(Boolean entitiesRetrieve) {
    this.entitiesRetrieve = entitiesRetrieve;
  }

  private Boolean membershipsRetrieve;

  
  
  public Boolean getMembershipsRetrieve() {
    if (this.membershipsRetrieve != null) {
      return membershipsRetrieve;
    }

    if (GrouperUtil.booleanValue(this.getMembershipsRetrieveAll(), false)) {
      return true;
    }

    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      return true;
    }
    return null;
  }


  
  public void setMembershipsRetrieve(Boolean membershipsRetrieve) {
    this.membershipsRetrieve = membershipsRetrieve;
  }


  private Boolean groupsRetrieve;

  public Boolean getGroupsRetrieve() {
    
    if (this.groupsRetrieve != null) {
      return groupsRetrieve;
    }

    if (GrouperUtil.booleanValue(this.getGroupsRetrieveAll(), false)) {
      return true;
    }

    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      return true;
    }
    if (GrouperUtil.booleanValue(this.getHasTargetGroupLink(), false)) {
      return true;
    }
    if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)
        || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)) {
      return true;
    }
    return null;
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
    if (this.groupsRetrieveAll != null) {
      return groupsRetrieveAll;
    }
    if (GrouperUtil.booleanValue(this.getHasTargetGroupLink(), false)) {
      return true;
    }
    
    // by default, if we're inserting/updating/deleting groups, then retrieve all groups?
    if (this.getGroupsInsert() == Boolean.TRUE || this.getGroupsUpdate() == Boolean.TRUE ||
        this.getGroupsDeleteIfDeletedFromGrouper() == Boolean.TRUE || this.getGroupsDeleteIfNotInGrouper() == Boolean.TRUE ||
            this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      return true;
    }
    return null;
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
    if (this.groupsUpdate != null) {
      return groupsUpdate;
    }
    
    if ( this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        || GrouperUtil.length(this.getGroupsUpdateAttributes()) > 0 || GrouperUtil.length(this.getGroupsUpdateFields()) > 0) {
      return true;
    }
    return null;
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
    if (this.groupsInsert != null) {
      return groupsInsert;
    }
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCreateMissingGroups()) {
      return true;
    }
    return null;
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
    if (this.groupsDeleteIfNotInGrouper != null) {
      return groupsDeleteIfNotInGrouper;
    }
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteInTargetIfInTargetAndNotGrouper()) {
      return true;
    }
    return null;    
  }

  
  public void setGroupsDeleteIfNotInGrouper(Boolean groupsDeleteIfNotInGrouper) {
    this.groupsDeleteIfNotInGrouper = groupsDeleteIfNotInGrouper;
  }

  
  public Boolean getGroupsDeleteIfDeletedFromGrouper() {
    if (this.groupsDeleteIfDeletedFromGrouper != null) {
      return groupsDeleteIfDeletedFromGrouper;
    }
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteInTargetIfDeletedInGrouper()) {
      return true;
    }
    return null;
  }

  
  public void setGroupsDeleteIfDeletedFromGrouper(
      Boolean groupsDeleteIfDeletedFromGrouper) {
    this.groupsDeleteIfDeletedFromGrouper = groupsDeleteIfDeletedFromGrouper;
  }

  
  public Boolean getEntitiesRetrieveAll() {
    if (this.entitiesRetrieveAll != null) {
      return entitiesRetrieveAll;
    }

    if (GrouperUtil.booleanValue(this.getHasTargetEntityLink(), false)) {
      return true;
    }

    // by default, if we're inserting/updating/deleting entities or there's an entity link, then retrieve all entities?
    if (this.getEntitiesInsert() == Boolean.TRUE || this.getEntitiesUpdate() == Boolean.TRUE ||
        this.getEntitiesDeleteIfDeletedFromGrouper() == Boolean.TRUE || this.getEntitiesDeleteIfNotInGrouper() == Boolean.TRUE ||
        this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      return true;
    }
    return null;
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
    if (this.entitiesUpdate != null) {
      return this.entitiesUpdate;
    }
    if ( this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        || GrouperUtil.length(this.getEntitiesUpdateAttributes()) > 0 || GrouperUtil.length(this.getEntitiesUpdateFields()) > 0) {
      return true;
    }
    return null;
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
    if (this.entitiesInsert != null) {
      return this.entitiesInsert;
    }
    return this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCreateMissingUsers();
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
    if (this.membershipsRetrieveAll != null) {
      return this.membershipsRetrieveAll;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      return true;
    }
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
    if (membershipsUpdate != null) {
      return membershipsUpdate;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanUpdateMembership(), false)
          || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanUpdateMemberships(), false)) {
        return true;
      }
    }
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
    if (membershipsInsert != null) {
      return membershipsInsert;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      return true;
    }
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
    if (membershipsDeleteIfNotInGrouper != null) {
      return membershipsDeleteIfNotInGrouper;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      return true;
    }
    return membershipsDeleteIfNotInGrouper;
  }

  
  public void setMembershipsDeleteIfNotInGrouper(Boolean membershipsDeleteIfNotInGrouper) {
    this.membershipsDeleteIfNotInGrouper = membershipsDeleteIfNotInGrouper;
  }

  
  public Boolean getMembershipsDeleteIfDeletedFromGrouper() {
    if (membershipsDeleteIfDeletedFromGrouper != null) {
      return membershipsDeleteIfDeletedFromGrouper;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      return true;
    }
    return membershipsDeleteIfDeletedFromGrouper;
  }

  
  public void setMembershipsDeleteIfDeletedFromGrouper(
      Boolean membershipsDeleteIfDeletedFromGrouper) {
    this.membershipsDeleteIfDeletedFromGrouper = membershipsDeleteIfDeletedFromGrouper;
  }

  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    Set<String> fieldNames = GrouperUtil.fieldNames(GrouperProvisioningBehavior.class, null, false);
        
    fieldNames = new TreeSet<String>(fieldNames);
    boolean firstField = true;
    for (String fieldName : fieldNames) {
      if ("grouperProvisioner".equals(fieldName)) {
        continue;
      }
      // call getter
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
  
  public boolean canObjectUpdateAttribute(
      ProvisioningUpdatable grouperProvisioningUpdatable, String attributeName) {

    if (grouperProvisioningUpdatable instanceof ProvisioningGroup) {
      return this.canGroupUpdateAttribute(attributeName);
    }
    if (grouperProvisioningUpdatable instanceof ProvisioningEntity) {
      return this.canEntityUpdateAttribute(attributeName);
    }
    if (grouperProvisioningUpdatable instanceof ProvisioningMembership) {
      return this.canMembershipUpdateAttribute(attributeName);
    }
    throw new RuntimeException("Not expecting object type: " + (grouperProvisioningUpdatable == null ? "null" : grouperProvisioningUpdatable.getClass().getName()));
  }
  
  public boolean canObjectUpdateField(ProvisioningUpdatable grouperTargetUpdatable,
      String fieldName) {
    if (grouperTargetUpdatable instanceof ProvisioningGroup) {
      return this.canGroupUpdateField(fieldName);
    }
    if (grouperTargetUpdatable instanceof ProvisioningEntity) {
      return this.canEntityUpdateField(fieldName);
    }
    if (grouperTargetUpdatable instanceof ProvisioningMembership) {
      return this.canMembershipUpdateField(fieldName);
    }
    throw new RuntimeException("Not expecting object type: " + (grouperTargetUpdatable == null ? "null" : grouperTargetUpdatable.getClass().getName()));
  }

}
