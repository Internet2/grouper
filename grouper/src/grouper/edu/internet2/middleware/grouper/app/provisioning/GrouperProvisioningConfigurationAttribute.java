package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.Collection;
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
   * insert this attribute
   */
  private boolean insert;
  
  /**
   * delete this attribute
   */
  private boolean delete;
  
  /**
   * delete this attribute
   * @return
   */
  public boolean isDelete() {
    return delete;
  }

  /**
   * delete this attribute
   * @param delete
   */
  public void setDelete(boolean delete) {
    this.delete = delete;
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
