/**
 * @author mchyzer
 * $Id: AttributeAssignResult.java,v 1.1 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.value;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * simple wrapper bean around result of attribute value assignment
 */
public class AttributeValueResult {

  /** attributeAssignResult */
  private AttributeAssignResult attributeAssignResult;

  /**
   * @param attributeAssignResult
   * @param attributeAssignValueResult
   */
  public AttributeValueResult(AttributeAssignResult attributeAssignResult,
      AttributeAssignValueResult attributeAssignValueResult) {
    super();
    this.attributeAssignResult = attributeAssignResult;
    this.attributeAssignValueResult = attributeAssignValueResult;
  }

  /**
   * @param attributeAssignResult
   * @param attributeAssignValuesResult
   */
  public AttributeValueResult(AttributeAssignResult attributeAssignResult,
      AttributeAssignValuesResult attributeAssignValuesResult) {
    super();
    this.attributeAssignResult = attributeAssignResult;
    this.attributeAssignValuesResult = attributeAssignValuesResult;
  }

  /**
   * 
   */
  public AttributeValueResult() {
    super();
  }

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

  /** attribute assign value result */
  private AttributeAssignValueResult attributeAssignValueResult;

  /** attribute assign values result */
  private AttributeAssignValuesResult attributeAssignValuesResult;

  /**
   * 
   * @return attribute assign value result
   */
  public AttributeAssignValueResult getAttributeAssignValueResult() {
    
    //convert from one to the other
    if (this.attributeAssignValueResult != null) {
      return this.attributeAssignValueResult;
    }
    
    if (this.attributeAssignValuesResult != null) {
      return GrouperUtil.collectionPopOne(this.attributeAssignValuesResult.getAttributeAssignValueResults(), true);
    }
    
    return null;
  }

  /**
   * attribute assign value result
   * @param attributeAssignValueResult1
   */
  public void setAttributeAssignValueResult(
      AttributeAssignValueResult attributeAssignValueResult1) {
    this.attributeAssignValueResult = attributeAssignValueResult1;
  }

  /**
   * attribute assign values result
   * @return attribute assign values result
   */
  public AttributeAssignValuesResult getAttributeAssignValuesResult() {
    //convert from one to the other
    if (this.attributeAssignValuesResult != null) {
      return this.attributeAssignValuesResult;
    }
    if (this.attributeAssignValueResult != null) {
      AttributeAssignValuesResult theAttributeAssignValuesResult = new AttributeAssignValuesResult(
          this.attributeAssignValueResult.isChanged(), GrouperUtil.toSet(this.attributeAssignValueResult));
      return theAttributeAssignValuesResult;
    }
    return null;
  }

  /**
   * attribute assign value result
   * @param attributeAssignValuesResult1
   */
  public void setAttributeAssignValuesResult(
      AttributeAssignValuesResult attributeAssignValuesResult1) {
    this.attributeAssignValuesResult = attributeAssignValuesResult1;
  }

  /**
   * if there was a change
   * @return if change
   */
  public boolean isChanged() {
    boolean changed = this.attributeAssignResult != null && this.attributeAssignResult.isChanged();
    changed = changed || (this.attributeAssignValueResult != null && this.attributeAssignValueResult.isChanged());
    changed = changed || (this.attributeAssignValuesResult != null && this.attributeAssignValuesResult.isChanged());
    return changed;
  }
  
  
}
