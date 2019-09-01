/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;


/**
 * one result of attribute assign query
 */
public class AttributeAssignFinderResult {

  /**
   * 
   */
  public AttributeAssignFinderResult() {
  }

  /**
   * group
   */
  private Group group;
  
  /**
   * group
   * @return the group
   */
  public Group getGroup() {
    return this.group;
  }
  
  /**
   * group
   * @param group1 the group to set
   */
  public void setGroup(Group group1) {
    this.group = group1;
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
