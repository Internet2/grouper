/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.value;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignBaseDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;


/**
 *
 */
public class AttributeValueDelegate {

  /**
   * reference to the attribute delegate
   */
  private AttributeAssignBaseDelegate attributeAssignBaseDelegate = null;

  /**
   * 
   * @param attributeAssignBaseDelegate1
   */
  public AttributeValueDelegate(AttributeAssignBaseDelegate attributeAssignBaseDelegate1) {
    this.attributeAssignBaseDelegate = attributeAssignBaseDelegate1;
  }

  /**
   * assign a value of any type 
   * @param value 
   * @return the value object
   */
  public AttributeValueResult assignValue(String attributeDefNameName, String value) {
    AttributeAssignResult attributeAssignResult = this.attributeAssignBaseDelegate
      .assignAttributeByName(attributeDefNameName);
    
    AttributeValueResult attributeValueResult = new AttributeValueResult();
    attributeValueResult.setAttributeAssignResult(attributeAssignResult);
    
    
    
    attributeAssignResult.getAttributeAssign().getValueDelegate().assignValue(value);
    return null;
  }

  
}
