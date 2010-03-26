/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;


/**
 * This object is able to have attributes assigned
 */
public interface AttributeAssignable {

  /**
   * get the logic delegate
   * @return the delegate
   */
  public AttributeAssignBaseDelegate getAttributeDelegate();
  
  /**
   * deal directly with attribute values
   * @return the delegate to deal with attribute values
   */
  public AttributeValueDelegate getAttributeValueDelegate();
}
