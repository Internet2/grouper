package edu.internet2.middleware.grouper.app.provisioning;

public class GrouperProvisioningConfigurationAttribute {

  public GrouperProvisioningConfigurationAttribute() {
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
   * if this attribute is used as the target id
   */
  private boolean targetId;

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
   * if this attribute is used as the target id
   * @return
   */
  public boolean isTargetId() {
    return targetId;
  }

  /**
   * if this attribute is used as the target id
   * @param targetId
   */
  public void setTargetId(boolean targetId) {
    this.targetId = targetId;
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

  
  
}
