package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;

/**
 * for displaying an attribute assignment on the screen
 * @author mchyzer
 *
 */
public class GuiAttributeAssign implements Serializable {
  
  /** attribute assignment */
  private AttributeAssign attributeAssign;

  /**
   * attribute assignment
   * @return attribute assignment
   */
  public AttributeAssign getAttributeAssign() {
    return this.attributeAssign;
  }

  /**
   * attribute assignment
   * @param attributeAssign1
   */
  public void setAttributeAssign(AttributeAssign attributeAssign1) {
    this.attributeAssign = attributeAssign1;
  }
  
  
  
}
