package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class GrouperProvisioningConfigurationAttribute {

  private GrouperProvisioner grouperProvisioner;
  
  private GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache;

  private boolean grouperProvisioningConfigurationAttributeDbCacheRetrieved = false;
  
  /**
   * 
   * @return if this is a member cache attribute
   */
  public GrouperProvisioningConfigurationAttributeDbCache getSyncMemberCacheAttribute() {

    // this is only for entities
    if (this.grouperProvisioningConfigurationAttributeType !=  GrouperProvisioningConfigurationAttributeType.entity) {
      return null;
    }

    // cache this
    if (this.grouperProvisioningConfigurationAttributeDbCacheRetrieved) {
      return this.grouperProvisioningConfigurationAttributeDbCache;
    }

    this.grouperProvisioningConfigurationAttributeDbCacheRetrieved = true;
    
    for (GrouperProvisioningConfigurationAttributeDbCache theGrouperProvisioningConfigurationAttributeDbCache 
        : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
      
      if (theGrouperProvisioningConfigurationAttributeDbCache != null 
          && StringUtils.equals(this.name, theGrouperProvisioningConfigurationAttributeDbCache.getAttributeName())) {
        this.grouperProvisioningConfigurationAttributeDbCache = theGrouperProvisioningConfigurationAttributeDbCache;
        break;
      }
    }
    return this.grouperProvisioningConfigurationAttributeDbCache;
  }
  
  /**
   * 
   * @return if this is a group cache attribute
   */
  public GrouperProvisioningConfigurationAttributeDbCache getSyncGroupCacheAttribute() {
    
    // this is only for groups
    if (this.grouperProvisioningConfigurationAttributeType !=  GrouperProvisioningConfigurationAttributeType.group) {
      return null;
    }

    // cache this
    if (this.grouperProvisioningConfigurationAttributeDbCacheRetrieved) {
      return this.grouperProvisioningConfigurationAttributeDbCache;
    }

    this.grouperProvisioningConfigurationAttributeDbCacheRetrieved = true;
    
    for (GrouperProvisioningConfigurationAttributeDbCache theGrouperProvisioningConfigurationAttributeDbCache 
        : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
      
      if (theGrouperProvisioningConfigurationAttributeDbCache != null 
          && StringUtils.equals(this.name, theGrouperProvisioningConfigurationAttributeDbCache.getAttributeName())) {
        this.grouperProvisioningConfigurationAttributeDbCache = theGrouperProvisioningConfigurationAttributeDbCache;
        break;
      }
    }
    return this.grouperProvisioningConfigurationAttributeDbCache;
  }

  /**
   * 
   * @return if this is a translatable attribute from sync member
   */
  public boolean isSyncMemberAttribute() {
    return StringUtils.equals("memberId", this.translateFromGrouperProvisioningEntityField)
        || StringUtils.equals("subjectId", this.translateFromGrouperProvisioningEntityField)
        || StringUtils.equals("subjectIdentifier", this.translateFromGrouperProvisioningEntityField)
        || StringUtils.equals("entityAttributeValueCache0", this.translateFromGrouperProvisioningEntityField)
        || StringUtils.equals("entityAttributeValueCache1", this.translateFromGrouperProvisioningEntityField)
        || StringUtils.equals("entityAttributeValueCache2", this.translateFromGrouperProvisioningEntityField)
        || StringUtils.equals("entityAttributeValueCache3", this.translateFromGrouperProvisioningEntityField);
        
  }
  
  /**
   * 
   * @return if this is a translatable attribute from sync group
   */
  public boolean isSyncGroupAttribute() {
    return StringUtils.equals("groupId", this.translateFromGrouperProvisioningGroupField)
        || StringUtils.equals("groupIdIndex", this.translateFromGrouperProvisioningGroupField)
        || StringUtils.equals("groupExtension", this.translateFromGrouperProvisioningGroupField)
        || StringUtils.equals("groupName", this.translateFromGrouperProvisioningGroupField)
        || StringUtils.equals("groupAttributeValueCache0", this.translateFromGrouperProvisioningGroupField)
        || StringUtils.equals("groupAttributeValueCache1", this.translateFromGrouperProvisioningGroupField)
        || StringUtils.equals("groupAttributeValueCache2", this.translateFromGrouperProvisioningGroupField)
        || StringUtils.equals("groupAttributeValueCache3", this.translateFromGrouperProvisioningGroupField);
  }
    
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * for validation messages
   */
  private int configIndex = -1;
  
  /**
   * for validation messages
   * @return config index
   */
  public int getConfigIndex() {
    return this.configIndex;
  }

  /**
   * for validation messages
   * @param configIndex1
   */
  public void setConfigIndex(int configIndex1) {
    this.configIndex = configIndex1;
  }

  /**
   * get config key for validation.  pass in select, receive: targetEntityAttribute.1.select
   * @param farRightSuffix
   * @return
   */
  public String configKey(String farRightSuffix) {
    return this.getGrouperProvisioningConfigurationAttributeType().getConfigPrefix() + "." + this.configIndex + "." + farRightSuffix;
  }
  
  /**
   * groups, entities, or memberships
   */
  private GrouperProvisioningConfigurationAttributeType grouperProvisioningConfigurationAttributeType;
  
  /**
   * groups, entities, or memberships
   * @return
   */
  public GrouperProvisioningConfigurationAttributeType getGrouperProvisioningConfigurationAttributeType() {
    return grouperProvisioningConfigurationAttributeType;
  }

  /**
   * groups, entities, or memberships
   * @param grouperProvisioningConfigurationAttributeType
   */
  public void setGrouperProvisioningConfigurationAttributeType(
      GrouperProvisioningConfigurationAttributeType grouperProvisioningConfigurationAttributeType) {
    this.grouperProvisioningConfigurationAttributeType = grouperProvisioningConfigurationAttributeType;
  }

  public GrouperProvisioningConfigurationAttribute() {
  }

  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    Set<String> fieldNames = GrouperUtil.fieldNames(GrouperProvisioningConfigurationAttribute.class, null, false);
        
    fieldNames.remove("grouperProvisioner");
    fieldNames.remove("grouperProvisioningConfigurationAttributeDbCache");
    fieldNames.remove("grouperProvisioningConfigurationAttributeDbCacheRetrieved");
    
    fieldNames = new TreeSet<String>(fieldNames);
    boolean firstField = true;
    for (String fieldName : fieldNames) {
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

  /**
   * grouper provisioning entity field
   */
  private String translateFromGrouperProvisioningEntityField;
  

  /**
   * grouper provisioning entity field
   * @return
   */
  public String getTranslateFromGrouperProvisioningEntityField() {
    return translateFromGrouperProvisioningEntityField;
  }

  /**
   * grouper provisioning entity field
   * @param translateFromGrouperProvisioningEntityField
   */
  public void setTranslateFromGrouperProvisioningEntityField(
      String translateFromGrouperProvisioningEntityField) {
    this.translateFromGrouperProvisioningEntityField = translateFromGrouperProvisioningEntityField;
  }

  /**
   * grouper provisioning group field create only
   */
  private String translateFromGrouperProvisioningGroupFieldCreateOnly;
  
  /**
   * grouper provisioning group field create only
   * @return value
   */
  public String getTranslateFromGrouperProvisioningGroupFieldCreateOnly() {
    return this.translateFromGrouperProvisioningGroupFieldCreateOnly;
  }

  /**
   * grouper provisioning group field create only
   * @param translateFromGrouperProvisioningGroupFieldCreateOnly1
   */
  public void setTranslateFromGrouperProvisioningGroupFieldCreateOnly(
      String translateFromGrouperProvisioningGroupFieldCreateOnly1) {
    this.translateFromGrouperProvisioningGroupFieldCreateOnly = translateFromGrouperProvisioningGroupFieldCreateOnly1;
  }

  /**
   * grouper provisioning entity field create only
   */
  private String translateFromGrouperProvisioningEntityFieldCreateOnly;
  
  /**
   * grouper provisioning entity field create only
   * @return grouper provisioning entity field create only
   */
  public String getTranslateFromGrouperProvisioningEntityFieldCreateOnly() {
    return this.translateFromGrouperProvisioningEntityFieldCreateOnly;
  }

  /**
   * grouper provisioning entity field create only
   * @param translateFromGrouperProvisioningEntityFieldCreateOnly1
   */
  public void setTranslateFromGrouperProvisioningEntityFieldCreateOnly(
      String translateFromGrouperProvisioningEntityFieldCreateOnly1) {
    this.translateFromGrouperProvisioningEntityFieldCreateOnly = translateFromGrouperProvisioningEntityFieldCreateOnly1;
  }

  /**
   * grouper provisioning group field
   */
  private String translateFromGrouperProvisioningGroupField;
  
  /**
   * grouper provisioning group field
   * @return
   */
  public String getTranslateFromGrouperProvisioningGroupField() {
    return translateFromGrouperProvisioningGroupField;
  }

  /**
   * grouper provisioning group field
   * @param translateFromGrouperProvisioningGroupField
   */
  public void setTranslateFromGrouperProvisioningGroupField(
      String translateFromGrouperProvisioningGroupField) {
    this.translateFromGrouperProvisioningGroupField = translateFromGrouperProvisioningGroupField;
  }

  /**
   * attribute or field name
   */
  private String name;
  
  /**
   * value type
   */
  private GrouperProvisioningConfigurationAttributeValueType valueType = GrouperProvisioningConfigurationAttributeValueType.STRING;
  
  /**
   * Validate value with jexl to see if valid for provisioning, the variable 'value' represents the current value.  return true if valid and false if invalid
   */
  private String validExpression;
  
  /**
   * Validate value with jexl to see if valid for provisioning, the variable 'value' represents the current value.  return true if valid and false if invalid
   * @return
   */
  public String getValidExpression() {
    return validExpression;
  }

  /**
   * Validate value with jexl to see if valid for provisioning, the variable 'value' represents the current value.  return true if valid and false if invalid
   * @param validExpression
   */
  public void setValidExpression(String validExpression) {
    this.validExpression = validExpression;
  }

  /**
   * max length of value to be valid for provisioning
   */
  private Integer maxlength;
  
  /**
   * max length of value to be valid for provisioning
   * @return
   */
  public Integer getMaxlength() {
    return maxlength;
  }

  /**
   * max length of value to be valid for provisioning
   * @param maxlength
   */
  public void setMaxlength(Integer maxlength) {
    this.maxlength = maxlength;
  }

  /**
   * if a value is require to provision this group/entity
   */
  private boolean required;
  
  /**
   * if a value is require to provision this group/entity
   * @return
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * if a value is require to provision this group/entity
   * @param required
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * insert this attribute
   */
  private boolean insert = true;
  
  /**
   * 
   */
  private Set<Object> ignoreIfMatchesValues = new HashSet<Object>();
  
  /**
   * 
   * @return
   */
  public Set<Object> getIgnoreIfMatchesValues() {
    return ignoreIfMatchesValues;
  }

  /**
   * 
   * @param ignoreIfMatchesValues
   */
  public void setIgnoreIfMatchesValue(Set<Object> ignoreIfMatchesValues) {
    this.ignoreIfMatchesValues = ignoreIfMatchesValues;
  }

  /**
   * select this attribute for normal selects
   */
  private boolean select = true;
  
  /**
   * update this attribute in normal updates
   */
  private boolean update = true;
  
  /**
   * if this is a multivalued attribute (Set)
   */
  private boolean multiValued;
  
  /**
   * expression when translating this field in normal translation
   */
  private String translateExpression;

  /**
   * expression when translating this field in create translation
   */
  private String translateExpressionCreateOnly;
  
  /**
   * static values in normal translation
   */
  private String translateFromStaticValues;

  /**
   * static values in create translation
   */
  private String translateFromStaticValuesCreateOnly;
  
  /**
   * attribute or field name
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * attribute or field name
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * value type
   * @return
   */
  public GrouperProvisioningConfigurationAttributeValueType getValueType() {
    return valueType;
  }

  /**
   * value type
   * @param valueType
   */
  public void setValueType(GrouperProvisioningConfigurationAttributeValueType valueType) {
    this.valueType = valueType;
  }

  /**
   * insert this attribute
   * @return
   */
  public boolean isInsert() {
    return insert;
  }

  /**
   * insert this attribute
   * @param insert
   */
  public void setInsert(boolean insert) {
    this.insert = insert;
  }

  /**
   * select this attribute for normal selects
   * @return
   */
  public boolean isSelect() {
    return select;
  }

  /**
   * select this attribute for normal selects
   * @param select
   */
  public void setSelect(boolean select) {
    this.select = select;
  }

  /**
   * update this attribute in normal updates
   * @return
   */
  public boolean isUpdate() {
    return update;
  }


  /**
   * update this attribute in normal updates
   * @return true if update this attribute
   */
  public boolean isUpdateConsiderMemberships() {
    if (this.update) {
      return true;
    }
    GrouperProvisioningBehavior grouperProvisioningBehavior = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior();
    
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    if (grouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        && this.grouperProvisioningConfigurationAttributeType == GrouperProvisioningConfigurationAttributeType.group
        && !StringUtils.isBlank(grouperProvisioningConfiguration.getGroupMembershipAttributeName())
        && StringUtils.equals(grouperProvisioningConfiguration.getGroupMembershipAttributeName(), this.name)
        && (grouperProvisioningBehavior.isInsertMemberships() 
            || grouperProvisioningBehavior.isDeleteMemberships() || grouperProvisioningBehavior.isUpdateMemberships())) {
      return true;
    }
    if (grouperProvisioningBehavior.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        && this.grouperProvisioningConfigurationAttributeType == GrouperProvisioningConfigurationAttributeType.entity
        && !StringUtils.isBlank(grouperProvisioningConfiguration.getEntityMembershipAttributeName())
        && StringUtils.equals(grouperProvisioningConfiguration.getEntityMembershipAttributeName(), this.name)
        && (grouperProvisioningBehavior.isInsertMemberships() 
            || grouperProvisioningBehavior.isDeleteMemberships() || grouperProvisioningBehavior.isUpdateMemberships())) {
      return true;
    }
    
    return false;
  }

  /**
   * update this attribute in normal updates
   * @param update
   */
  public void setUpdate(boolean update) {
    this.update = update;
  }

  /**
   * if this is a multivalued attribute (Set)
   * @return
   */
  public boolean isMultiValued() {
    return multiValued;
  }

  /**
   * if this is a multivalued attribute (Set)
   * @param multiValued
   */
  public void setMultiValued(boolean multiValued) {
    this.multiValued = multiValued;
  }

  /**
   * expression when translating this field in normal translation
   * @return
   */
  public String getTranslateExpression() {
    return translateExpression;
  }

  /**
   * expression when translating this field in normal translation
   * @param translateExpression
   */
  public void setTranslateExpression(String translateExpression) {
    this.translateExpression = translateExpression;
  }

  /**
   * expression when translating this field in create translation
   * @return
   */
  public String getTranslateExpressionCreateOnly() {
    return translateExpressionCreateOnly;
  }

  /**
   * expression when translating this field in create translation
   * @param translateExpressionCreateOnly
   */
  public void setTranslateExpressionCreateOnly(String translateExpressionCreateOnly) {
    this.translateExpressionCreateOnly = translateExpressionCreateOnly;
  }

  /**
   * default value if there is not a value
   */
  private String defaultValue;
  
  /**
   * default value if there is not a value
   * @return
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * default value if there is not a value
   * @param defaultValue1
   */
  public void setDefaultValue(String defaultValue1) {
    this.defaultValue = defaultValue1;
    
  }

  /**
   * static values in normal translation
   * @return
   */
  public String getTranslateFromStaticValues() {
    return translateFromStaticValues;
  }

  /**
   * static values in normal translation
   * @param translateFromStaticValues
   */
  public void setTranslateFromStaticValues(String translateFromStaticValues) {
    this.translateFromStaticValues = translateFromStaticValues;
  }
  
  /**
   * static values in create translation
   * @return
   */
  public String getTranslateFromStaticValuesCreateOnly() {
    return translateFromStaticValuesCreateOnly;
  }
  
  /**
   * static values in create translation
   * @param translateFromStaticValuesCreateOnly
   */
  public void setTranslateFromStaticValuesCreateOnly(
      String translateFromStaticValuesCreateOnly) {
    this.translateFromStaticValuesCreateOnly = translateFromStaticValuesCreateOnly;
  }
}
