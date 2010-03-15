/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;


/**
 * This object is able to have attributes assigned
 */
public interface AttributeAssignable {

  /**
   * get the logic delegate
   * @return the delegate
   */
  public AttributeAssignBaseDelegate getAttributeDelegate();
  
}
