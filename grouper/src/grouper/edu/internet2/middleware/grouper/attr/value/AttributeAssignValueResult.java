/**
 * @author mchyzer
 * $Id: AttributeAssignResult.java,v 1.1 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.value;

import java.util.Set;


/**
 * simple wrapper bean around result of attribute assignment
 */
public class AttributeAssignValueResult {

  /** if this attribute assignment/deletion took place or already existed */
  private boolean changed = false;
  
  /** if this attribute deletion took place or already existed */
  private boolean deleted = false;
  
  /**
   * the attribute assignment
   */
  private AttributeAssignValue attributeAssignValue = null;

  /**
   * the attribute assignment
   */
  private Set<AttributeAssignValue> attributeAssignValues = null;

  
  /**
   * @param newlyAssigned1 if this attribute assignment took place or already existed
   * @param deleted1 
   * @param attributeAssignValue1 the attribute assignment
   */
  public AttributeAssignValueResult(boolean newlyAssigned1, boolean deleted1, AttributeAssignValue attributeAssignValue1) {
    super();
    this.changed = newlyAssigned1;
    this.deleted = deleted1;
    this.attributeAssignValue = attributeAssignValue1;
  }


  /**
   * deleted
   * @return deleted
   */
  public boolean isDeleted() {
    return this.deleted;
  }


  /**
   * if this attribute assignment/deletion took place or already existed
   * @return the newlyAssigned
   */
  public boolean isChanged() {
    return this.changed;
  }

  
  /**
   * if this attribute assignment/deletion took place or already existed
   * @param newlyAssigned the newlyAssigned to set
   */
  public void setChanged(boolean newlyAssigned) {
    this.changed = newlyAssigned;
  }

  
  /**
   * the attribute assignment
   * @return the attributeAssign
   */
  public AttributeAssignValue getAttributeAssignValue() {
    return this.attributeAssignValue;
  }

  
  /**
   * the attribute assignment
   * @param attributeAssignValue the attributeAssign to set
   */
  public void setAttributeAssign(AttributeAssignValue attributeAssignValue) {
    this.attributeAssignValue = attributeAssignValue;
  }


  /**
   * 
   * @return the set of values
   */
  public Set<AttributeAssignValue> getAttributeAssignValues() {
    return this.attributeAssignValues;
  }


  /**
   * set of values
   * @param attributeAssignValues1
   */
  public void setAttributeAssignValues(Set<AttributeAssignValue> attributeAssignValues1) {
    this.attributeAssignValues = attributeAssignValues1;
  }
}
