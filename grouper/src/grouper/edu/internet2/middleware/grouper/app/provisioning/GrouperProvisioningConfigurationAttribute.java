package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningConfigurationAttribute {

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

  /**
   * After calculating the Grouper value store that in a sync field
   */
  private String translateGrouperToMemberSyncField;
  
  /**
   * After calculating the Grouper value store that in a sync field
   * @return
   */
  public String getTranslateGrouperToMemberSyncField() {
    return translateGrouperToMemberSyncField;
  }

  /**
   * After calculating the Grouper value store that in a sync field
   * @param translateGrouperToMemberSyncField
   */
  public void setTranslateGrouperToMemberSyncField(
      String translateGrouperToMemberSyncField) {
    this.translateGrouperToMemberSyncField = translateGrouperToMemberSyncField;
  }

  /**
   * After calculating the Grouper value store that in a sync field
   */
  private String translateGrouperToGroupSyncField;
  
  
  
  /**
   * After calculating the Grouper value store that in a sync field
   * @return
   */
  public String getTranslateGrouperToGroupSyncField() {
    return translateGrouperToGroupSyncField;
  }

  /**
   * After calculating the Grouper value store that in a sync field
   * @param translateGrouperToGroupSyncField
   */
  public void setTranslateGrouperToGroupSyncField(String translateGrouperToGroupSyncField) {
    this.translateGrouperToGroupSyncField = translateGrouperToGroupSyncField;
  }

  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    Set<String> fieldNames = GrouperUtil.fieldNames(GrouperProvisioningConfigurationAttribute.class, null, false);
        
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
   * 
   */
  private String translateFromMemberSyncField;

  /**
   * take this value from target and copy into the sync field
   */
  private String translateToGroupSyncField;

  
  
  /**
   * take this value from target and copy into the sync field
   * @return
   */
  public String getTranslateToGroupSyncField() {
    return translateToGroupSyncField;
  }

  /**
   * take this value from target and copy into the sync field
   * @param translateToGroupSyncField
   */
  public void setTranslateToGroupSyncField(String translateToGroupSyncField) {
    this.translateToGroupSyncField = translateToGroupSyncField;
  }

  /**
   * take this value from target and copy into the sync field
   */
  private String translateToMemberSyncField;

  
  /**
   * take this value from target and copy into the sync field
   * @return
   */
  public String getTranslateToMemberSyncField() {
    return translateToMemberSyncField;
  }

  /**
   * take this value from target and copy into the sync field
   * @param translateToEntitySyncField
   */
  public void setTranslateToMemberSyncField(String translateToEntitySyncField) {
    this.translateToMemberSyncField = translateToEntitySyncField;
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
   * 
   */
  private String translateFromGroupSyncField;
  
  
  public String getTranslateFromMemberSyncField() {
    return translateFromMemberSyncField;
  }

  
  public void setTranslateFromMemberSyncField(String translateFromMemberSyncField) {
    this.translateFromMemberSyncField = translateFromMemberSyncField;
  }

  
  public String getTranslateFromGroupSyncField() {
    return translateFromGroupSyncField;
  }

  
  public void setTranslateFromGroupSyncField(String translateFromGroupSyncField) {
    this.translateFromGroupSyncField = translateFromGroupSyncField;
  }

  /**
   * attribute or field name
   */
  private String name;
  
  /**
   * true for attribute, false for field
   */
  private boolean attribute;
  
  /**
   * value type
   */
  private GrouperProvisioningConfigurationAttributeValueType valueType;
  
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
  private boolean insert;
  
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
  private boolean select;
  
  /**
   * update this attribute in normal updates
   */
  private boolean update;
  
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
   * NOTE: CURRENTLY NOT USED
   * expression when translating a group or entity membership attribute
   */
  private String translateExpressionFromMembership;
  
  /**
   * if this attribute is used as the matching id
   */
  private boolean matchingId;

  /**
   * if this attribute is the membership attribute
   */
  private boolean membershipAttribute;

  /**
   * if this is the attribute used to search for objects in the target
   */
  private boolean searchAttribute;
  
  /**
   * if this is the attribute used to search for objects in the target
   * @return
   */
  public boolean isSearchAttribute() {
    return searchAttribute;
  }

  /**
   * if this is the attribute used to search for objects in the target
   * @param searchAttribute
   */
  public void setSearchAttribute(boolean searchAttribute) {
    this.searchAttribute = searchAttribute;
  }

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
   * true for attribute, false for field
   * @return
   */
  public boolean isAttribute() {
    return attribute;
  }

  /**
   * true for attribute, false for field
   * @param attribute
   */
  public void setAttribute(boolean attribute) {
    this.attribute = attribute;
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
   * if this attribute is used as the matching id
   * @return
   */
  public boolean isMatchingId() {
    return matchingId;
  }

  /**
   * if this attribute is used as the matching id
   * @param matchingId
   */
  public void setMatchingId(boolean matchingId) {
    this.matchingId = matchingId;
  }

  /**
   * if this attribute is the membership attribute
   * @return
   */
  public boolean isMembershipAttribute() {
    return membershipAttribute;
  }

  /**
   * if this attribute is the membership attribute
   * @param membershipAttribute
   */
  public void setMembershipAttribute(boolean membershipAttribute) {
    this.membershipAttribute = membershipAttribute;
  }

  
  /**
   * NOTE: CURRENTLY NOT USED
   * @return expression when translating a group or entity membership attribute
   */
  public String getTranslateExpressionFromMembership() {
    return translateExpressionFromMembership;
  }

  /**
   * expression when translating a group or entity membership attribute
   * @param translateExpressionFromMembership
   */
  public void setTranslateExpressionFromMembership(
      String translateExpressionFromMembership) {
    this.translateExpressionFromMembership = translateExpressionFromMembership;
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
}
