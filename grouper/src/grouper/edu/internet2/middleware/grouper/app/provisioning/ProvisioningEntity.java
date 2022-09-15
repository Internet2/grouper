package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * entity is a member of a group which is typically a user/account or person
 * @author mchyzer
 *
 */
public class ProvisioningEntity extends ProvisioningUpdatable {

  /**
   * id index in target (optional)
   * @return id index
   */
  public Long getIdIndex() {
    return this.retrieveAttributeValueLong("idIndex");
  }

  /**
   * id index in target (optional)
   * @param idIndex1
   */
  public void setIdIndex(Long idIndex1) {
    this.assignAttributeValue("idIndex", idIndex1);
  }
  /**
   * 
   * @return subjectIdentifier0
   */
  public String getSubjectIdentifier0() {
    return this.retrieveAttributeValueString("subjectIdentifier0");
  }
  
  /**
   * 
   * @param subjectIdentifier0
   */
  public void setSubjectIdentifier0(String subjectIdentifier0) {
    this.assignAttributeValue("subjectIdentifier0", subjectIdentifier0);
  }
  
  /**
   * 
   * @return subjectIdentifier1
   */
  public String getSubjectIdentifier1() {
    return this.retrieveAttributeValueString("subjectIdentifier1");
  }
  
  /**
   * 
   * @param subjectIdentifier1
   */
  public void setSubjectIdentifier1(String subjectIdentifier1) {
    this.assignAttributeValue("subjectIdentifier1", subjectIdentifier1);
  }
  
  /**
   * 
   * @return subjectIdentifier2
   */
  public String getSubjectIdentifier2() {
    return this.retrieveAttributeValueString("subjectIdentifier2");
  }
  
  /**
   * 
   * @param subjectIdentifier2
   */
  public void setSubjectIdentifier2(String subjectIdentifier2) {
    this.assignAttributeValue("subjectIdentifier2", subjectIdentifier2);
  }
  
  
  /**
   * 
   * @return description
   */
  public String getDescription() {
    return this.retrieveAttributeValueString("description");
  }

  /**
   * 
   * @param description
   */
  public void setDescription(String description) {
    this.assignAttributeValue("description", description);
  }

  /**
   * 
   * @return subject source id
   */
  public String getSubjectSourceId() {
    return this.retrieveAttributeValueString("subjectSourceId");
  }

  /**
   * 
   * @param subjectSourceId
   */
  public void setSubjectSourceId(String subjectSourceId) {
    this.assignAttributeValue("subjectSourceId", subjectSourceId);
  }

  /**
   * 
   * @return
   */
  public String getSubjectId() {
    return this.retrieveAttributeValueString("subjectId");
  }

  /**
   * 
   * @param subjectId
   */
  public void setSubjectId(String subjectId) {
    this.assignAttributeValue("subjectId", subjectId);
  }

  private ProvisioningEntityWrapper provisioningEntityWrapper;
  
  /**
   * id uniquely identifies this record, might be a uuid, or subject id
   * @return id
   */
  public String getId() {
    return this.retrieveAttributeValueString("id");
  }

  /**
   * id uniquely identifies this record, might be a uuid, or subject id
   * @param id1
   */
  public void setId(String id1) {
    this.assignAttributeValue("id", id1);
  }

  /**
   * login id could be a subject identifier or subject id (optional)
   * @return login id
   */
  public String getLoginId() {
    return this.retrieveAttributeValueString("loginId");
  }

  /**
   * login id could be a subject identifier or subject id (optional)
   * @param login1
   */
  public void setLoginId(String login1) {
    this.assignAttributeValue("loginId", login1);
  }

  /**
   * name field in the entity (optional)
   * @return name
   */
  public String getName() {
    return this.retrieveAttributeValueString("name");
  }

  /**
   * name field in the entity (optional)
   * @param name1
   */
  public void setName(String name1) {
    this.assignAttributeValue("name", name1);
  }

  /**
   * email of entity (optional)
   * @return email
   */
  public String getEmail() {
    return this.retrieveAttributeValueString("email");
  }

  /**
   * email of entity (optional)
   * @param email1
   */
  public void setEmail(String email1) {
    this.assignAttributeValue("email", email1);
  }

  public ProvisioningEntityWrapper getProvisioningEntityWrapper() {
    return provisioningEntityWrapper;
  }


  
  public void setProvisioningEntityWrapper(ProvisioningEntityWrapper provisioningEntityWrapper) {
    this.provisioningEntityWrapper = provisioningEntityWrapper;
  }
  
  /**
   * 
   * @param name
   * @param value
   */
  public String retrieveAttributeValueString(GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    
    return GrouperUtil.stringValue(this.retrieveAttributeValue(grouperProvisioningConfigurationAttribute));
    
  }

  
  /**
   * base on attribute get the value
   * @param grouperProvisioningConfigurationAttribute
   * @return the value
   */
  public Object retrieveAttributeValue(
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    if (grouperProvisioningConfigurationAttribute == null) {
      throw new NullPointerException("attribute is null: " + this);
    }
    return this.retrieveAttributeValueString(grouperProvisioningConfigurationAttribute.getName());
  }

  public String toString() {
    StringBuilder result = new StringBuilder("Entity(");
    boolean firstField = true;
    firstField = this.toStringProvisioningUpdatable(result, firstField);
    
    if (this.provisioningEntityWrapper != null) {
      if (this.provisioningEntityWrapper.isRecalcObject()) {
        firstField = toStringAppendField(result, firstField, "recalcObject", this.provisioningEntityWrapper.isRecalcObject());
      }
      if (this.provisioningEntityWrapper.isRecalcEntityMemberships()) {
        firstField = toStringAppendField(result, firstField, "recalcMships", this.provisioningEntityWrapper.isRecalcEntityMemberships());
      }
      if (this.provisioningEntityWrapper.isCreate()) {
        firstField = toStringAppendField(result, firstField, "create", this.provisioningEntityWrapper.isCreate());
      }
      if (this.provisioningEntityWrapper.isDelete()) {
        firstField = toStringAppendField(result, firstField, "delete", this.provisioningEntityWrapper.isDelete());
      }
      if (this.provisioningEntityWrapper.isIncrementalSyncMemberships()) {
        firstField = toStringAppendField(result, firstField, "incrementalSyncMemberships", this.provisioningEntityWrapper.isIncrementalSyncMemberships());
      }
      if (this.provisioningEntityWrapper.getErrorCode() != null) {
        firstField = toStringAppendField(result, firstField, "errorCode", this.provisioningEntityWrapper.getErrorCode().name());
      }
    }
    
    return result.append(")").toString();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public ProvisioningEntity clone() {

    ProvisioningEntity provisioningEntity = new ProvisioningEntity();

    this.cloneUpdatable(provisioningEntity);
    provisioningEntity.provisioningEntityWrapper = this.provisioningEntityWrapper;

    return provisioningEntity;
  }

  @Override
  public boolean canInsertAttribute(String name) {

    GrouperProvisioner grouperProvisioner = this.getProvisioningEntityWrapper().getGrouperProvisioner();
    GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (retrieveGrouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        && StringUtils.equals(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), name) ) {
      return retrieveGrouperProvisioningBehavior.isInsertMemberships();
    }

    return retrieveGrouperProvisioningBehavior.canInsertEntityAttribute(name);
  }

  @Override
  public boolean canUpdateAttribute(String name) {
    GrouperProvisioner grouperProvisioner = this.getProvisioningEntityWrapper().getGrouperProvisioner();
    GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (retrieveGrouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        && StringUtils.equals(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), name) ) {
      return retrieveGrouperProvisioningBehavior.isUpdateMemberships();
    }
    return this.getProvisioningEntityWrapper().getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canUpdateEntityAttribute(name);
  }

  @Override
  public boolean canDeleteAttribute(String name) {

    GrouperProvisioner grouperProvisioner = this.getProvisioningEntityWrapper().getGrouperProvisioner();
    GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (retrieveGrouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        && StringUtils.equals(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), name) ) {
      return retrieveGrouperProvisioningBehavior.isDeleteMemberships();
    }
    // theres no delete
    return this.canUpdateAttribute(name);
  }
  @Override
  public boolean canDeleteAttributeValue(String name, Object deleteValue) {

    GrouperProvisioner grouperProvisioner = this.getProvisioningEntityWrapper().getGrouperProvisioner();
    GrouperProvisioningBehavior retrieveGrouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (retrieveGrouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        && StringUtils.equals(grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), name) ) {
      
      if (retrieveGrouperProvisioningBehavior.isDeleteMembershipsIfNotExistInGrouper()) {
        return true;
      }
      
      ProvisioningAttribute provisioningAttribute = this.getAttributes().get(name);
      if (provisioningAttribute == null) {
        return false;
      }

      ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper().get(deleteValue);
      if (provisioningMembershipWrapper == null) {
        return false;
      }
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      if (gcGrouperSyncMembership == null) {
        return false;
      }
      if (retrieveGrouperProvisioningBehavior.isDeleteMembershipsIfGrouperDeleted()) {
        return true;
      }
      if (gcGrouperSyncMembership.isInTargetInsertOrExists() && retrieveGrouperProvisioningBehavior.isDeleteMembershipsIfGrouperCreated()) {
        return true;
      }
      return false;
    }
    // regular delete was already checked
    return true;
  }

  @Override
  public String objectTypeName() {
    return "entity";
  }

}
