/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;


/**
 * one result of attribute assign query
 */
public class AttributeAssignFinderResult {

  /**
   * attribute definition
   */
  private AttributeDef attributeDef;
  
  /**
   * attribute definition name
   */
  private AttributeDefName attributeDefName;
  
  /**
   * 
   * @return attr def
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

  /**
   * 
   * @param attributeDef1
   */
  public void setAttributeDef(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }

  /**
   * 
   * @return attr def name
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }

  /**
   * attr def name
   * @param attributeDefName1
   */
  public void setAttributeDefName(AttributeDefName attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }


  /**
   * attribute assignment on an assignment
   */
  private AttributeAssign attributeAssignOnAssign;

  /**
   * attribute assignment on an assignment
   * @return the assign
   */
  public AttributeAssign getAttributeAssignOnAssign() {
    return this.attributeAssignOnAssign;
  }

  /**
   * attribute assignment on an assignment
   * @param attributeAssignOnAssign1
   */
  public void setAttributeAssignOnAssign(AttributeAssign attributeAssignOnAssign1) {
    this.attributeAssignOnAssign = attributeAssignOnAssign1;
  }

  /**
   * 
   */
  public AttributeAssignFinderResult() {
  }

  
  /**
   * owner attribute assign
   */
  private AttributeAssign ownerAttributeAssign;
  
  
  
  
  public AttributeAssign getOwnerAttributeAssign() {
    return ownerAttributeAssign;
  }

  
  public void setOwnerAttributeAssign(AttributeAssign ownerAttributeAssign) {
    this.ownerAttributeAssign = ownerAttributeAssign;
  }


  /**
   * group
   */
  private Group ownerGroup;
  
  /**
   * group
   * @return the group
   */
  public Group getOwnerGroup() {
    return this.ownerGroup;
  }
  
  /**
   * group
   * @param ownerGroup1 the group to set
   */
  public void setOwnerGroup(Group ownerGroup1) {
    this.ownerGroup = ownerGroup1;
  }
  
  /**
   * attribute assign
   */
  private AttributeAssign attributeAssign;

  
  /**
   * attribute assign
   * @return the attributeAssign
   */
  public AttributeAssign getAttributeAssign() {
    return this.attributeAssign;
  }

  
  /**
   * attribute assign
   * @param attributeAssign1 the attributeAssign to set
   */
  public void setAttributeAssign(AttributeAssign attributeAssign1) {
    this.attributeAssign = attributeAssign1;
  }
  
  /**
   * if getting values, this is the value
   */
  private Set<AttributeAssignValue> attributeAssignValues;

  /**
   * if getting values, this is the value
   * @return the value
   */
  public Set<AttributeAssignValue> getAttributeAssignValues() {
    return this.attributeAssignValues;
  }

  /**
   * if getting values, this is the value
   * @param attributeAssignValue1
   */
  public void setAttributeAssignValues(Set<AttributeAssignValue> attributeAssignValue1) {
    this.attributeAssignValues = attributeAssignValue1;
  }
  
}
