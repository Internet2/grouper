/**
 * @author mchyzer
 * $Id: AttributeAssignResult.java,v 1.1 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.value;

import java.util.Set;


/**
 * simple wrapper bean around result of attribute assignment value
 */
public class AttributeAssignValuesResult {

  /**
   * 
   */
  public AttributeAssignValuesResult() {
    super();
  }


  /** if this attribute assignment took place or already existed */
  private boolean changed = false;
  
  /**
   * the attribute assignment
   */
  private Set<AttributeAssignValueResult> attributeAssignValueResults = null;

  
  /**
   * @param newlyAssigned1 if this attribute assignment took place or already existed
   * @param attributeAssignValueResults1 the attribute assignment
   */
  public AttributeAssignValuesResult(boolean newlyAssigned1, Set<AttributeAssignValueResult> attributeAssignValueResults1) {
    super();
    this.changed = newlyAssigned1;
    this.attributeAssignValueResults = attributeAssignValueResults1;
  }


  /**
   * if this attribute assignment took place or already existed
   * @return the newlyAssigned
   */
  public boolean isChanged() {
    return this.changed;
  }

  
  /**
   * if this attribute assignment took place or already existed
   * @param newlyAssigned the newlyAssigned to set
   */
  public void setChanged(boolean newlyAssigned) {
    this.changed = newlyAssigned;
  }


  /**
   * 
   * @return the set of values
   */
  public Set<AttributeAssignValueResult> getAttributeAssignValueResults() {
    return this.attributeAssignValueResults;
  }


  /**
   * set of values
   * @param attributeAssignValueResults1
   */
  public void setAttributeAssignValueResults(Set<AttributeAssignValueResult> attributeAssignValueResults1) {
    this.attributeAssignValueResults = attributeAssignValueResults1;
  }

  
}
