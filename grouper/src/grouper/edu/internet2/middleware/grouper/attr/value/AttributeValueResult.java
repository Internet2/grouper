/**
 * @author mchyzer
 * $Id: AttributeAssignResult.java,v 1.1 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.value;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;


/**
 * simple wrapper bean around result of attribute assignment
 */
public class AttributeValueResult {

  /** attributeAssignResult */
  private AttributeAssignResult attributeAssignResult;

  /**
   * 
   * @return attributeAssignResult
   */
  public AttributeAssignResult getAttributeAssignResult() {
    return this.attributeAssignResult;
  }

  /**
   * 
   * @param attributeAssignResult1
   */
  public void setAttributeAssignResult(AttributeAssignResult attributeAssignResult1) {
    this.attributeAssignResult = attributeAssignResult1;
  }

  /**
   * 
   */
  private Set<AttributeAssignValue> attributeAssignValues = null;

  /**
   * 
   * @return values
   */
  public Set<AttributeAssignValue> getAttributeAssignValues() {
    return this.attributeAssignValues;
  }

  /**
   * 
   * @param attributeAssignValues1
   */
  public void setAttributeAssignValues(Set<AttributeAssignValue> attributeAssignValues1) {
    this.attributeAssignValues = attributeAssignValues1;
  }
  
  
  
}
