/**
 * @author mchyzer
 * $Id: AttributeAssignResult.java,v 1.1 2009-10-12 09:46:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;


/**
 * simple wrapper bean around result of attribute assignment
 */
public class AttributeAssignResult {

  /** if this attribute assignment took place or already existed */
  private boolean changed = false;
  
  /**
   * the attribute assignment
   */
  private AttributeAssign attributeAssign = null;

  
  /**
   * @param newlyAssigned1 if this attribute assignment took place or already existed
   * @param attributeAssign1 the attribute assignment
   */
  public AttributeAssignResult(boolean newlyAssigned1, AttributeAssign attributeAssign1) {
    super();
    this.changed = newlyAssigned1;
    this.attributeAssign = attributeAssign1;
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
   * the attribute assignment
   * @return the attributeAssign
   */
  public AttributeAssign getAttributeAssign() {
    return this.attributeAssign;
  }

  
  /**
   * the attribute assignment
   * @param attributeAssign the attributeAssign to set
   */
  public void setAttributeAssign(AttributeAssign attributeAssign) {
    this.attributeAssign = attributeAssign;
  }
}
