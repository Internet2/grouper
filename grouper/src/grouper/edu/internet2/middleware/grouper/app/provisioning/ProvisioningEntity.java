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
   * see if this object is empty e.g. after translating if empty then dont keep track of group
   * since the translation might have affected another object
   * @return
   */
  public boolean isEmpty() {
    if (StringUtils.isBlank(this.email)
        && StringUtils.isBlank(this.id)
        && StringUtils.isBlank(this.name)
        && StringUtils.isBlank(this.loginId)
        && StringUtils.isBlank(this.subjectId)
        && this.isEmptyUpdatable()) {
      return true;
    }
    return false;
  }

  /**
   * id uniquely identifies this record, might be a target uuid, or subject id
   */
  private String id;
  
  /**
   * login id could be a subject identifier or subject id (optional)
   */
  private String loginId;

  /**
   * name field in the entity (optional)
   */
  private String name;
  
  /**
   * email of entity (optional)
   */
  private String email;
  
  /**
   * subject id (optional)
   */
  private String subjectId;
  
  /**
   * 
   * @return
   */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * 
   * @param subjectId
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  private ProvisioningEntityWrapper provisioningEntityWrapper;
  
  /**
   * id uniquely identifies this record, might be a uuid, or subject id
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id uniquely identifies this record, might be a uuid, or subject id
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * login id could be a subject identifier or subject id (optional)
   * @return login id
   */
  public String getLoginId() {
    return this.loginId;
  }

  /**
   * login id could be a subject identifier or subject id (optional)
   * @param login1
   */
  public void setLoginId(String login1) {
    this.loginId = login1;
  }

  /**
   * name field in the entity (optional)
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name field in the entity (optional)
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * email of entity (optional)
   * @return email
   */
  public String getEmail() {
    return this.email;
  }

  /**
   * email of entity (optional)
   * @param email1
   */
  public void setEmail(String email1) {
    this.email = email1;
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
  public String retrieveFieldOrAttributeValueString(GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    
    return GrouperUtil.stringValue(this.retrieveFieldOrAttributeValue(grouperProvisioningConfigurationAttribute));
    
  }

  
  /**
   * base on attribute get the value
   * @param grouperProvisioningConfigurationAttribute
   * @return the value
   */
  public Object retrieveFieldOrAttributeValue(
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    if (grouperProvisioningConfigurationAttribute == null) {
      throw new NullPointerException("attribute is null: " + this);
    }
    if (grouperProvisioningConfigurationAttribute.isAttribute()) {
      return this.retrieveAttributeValueString(grouperProvisioningConfigurationAttribute.getName());
    } else {
      if ("email".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getEmail();
      }
      if ("id".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getId();
      }
      if ("loginId".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getLoginId();
      }
      if ("name".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getName();
      }
      if ("subjectId".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getSubjectId();
      }
      throw new RuntimeException("Invalid field name '" + grouperProvisioningConfigurationAttribute.getName() + "': " + this);
    }
  }

  public String toString() {
    StringBuilder result = new StringBuilder("Entity(");
    boolean firstField = true;
    firstField = toStringAppendField(result, firstField, "id", this.id);
    firstField = toStringAppendField(result, firstField, "email", this.email);
    firstField = toStringAppendField(result, firstField, "name", this.name);
    firstField = toStringAppendField(result, firstField, "loginId", this.loginId);
    firstField = toStringAppendField(result, firstField, "subjectId", this.subjectId);
    firstField = this.toStringProvisioningUpdatable(result, firstField);
    
    if (this.provisioningEntityWrapper != null) {
      if (this.provisioningEntityWrapper.isRecalc()) {
        firstField = toStringAppendField(result, firstField, "recalc", this.provisioningEntityWrapper.isRecalc());
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
    provisioningEntity.email = this.email;
    provisioningEntity.id = this.id;
    provisioningEntity.loginId = this.loginId;
    provisioningEntity.name = this.name;
    provisioningEntity.subjectId = this.subjectId;
    provisioningEntity.provisioningEntityWrapper = this.provisioningEntityWrapper;

    return provisioningEntity;
  }

  public void assignSearchFilter() {
    String userSearchFilter = this.getProvisioningEntityWrapper().getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchFilter();
    if (!StringUtils.isBlank(userSearchFilter)) {
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("targetEntity", this);
      String result = GrouperUtil.stringValue(this.getProvisioningEntityWrapper().getGrouperProvisioner().retrieveGrouperTranslator().runExpression(userSearchFilter, variableMap));
      this.setSearchFilter(result);
    }
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

}
