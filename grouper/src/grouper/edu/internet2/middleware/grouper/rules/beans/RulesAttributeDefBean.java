/**
 * 
 */
package edu.internet2.middleware.grouper.rules.beans;

import edu.internet2.middleware.grouper.attr.AttributeDef;


/**
 * @author mchyzer
 *
 */
public class RulesAttributeDefBean extends RulesBean {

  /**
   * 
   */
  public RulesAttributeDefBean() {
    
  }
  
  /**
   * 
   * @param theAttributeDef
   */
  public RulesAttributeDefBean(AttributeDef theAttributeDef) {
    super();
    this.attributeDef = theAttributeDef;
  }


  /** attributeDef */
  private AttributeDef attributeDef;

  /**
   * attributeDef
   * @return attributeDef
   */
  @Override
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

  /**
   * attributeDef
   * @param attributeDef1
   */
  public void setAttributeDef(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }

  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (this.attributeDef != null) {
      result.append("group: ").append(this.attributeDef.getName()).append(", ");
    }
    return result.toString();
  }
  
  
}
