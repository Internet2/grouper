/*
 * @author mchyzer
 * $Id: GroupAttributeNameValidationAttrHook.java,v 1.1 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * you can retrict certain attributes of a group to be within a certain regex
 * 
 * </pre>
 */
public class GroupAttributeNameValidationAttrHook extends AttributeHooks {
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostInsert(HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostInsert(HooksContext hooksContext, HooksAttributeBean preInsertBean) {
    Attribute attribute = preInsertBean.getAttribute();
    
    GroupAttributeNameValidationHook.groupPreChangeAttribute(attribute.getAttrName(), attribute.getValue());
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePreUpdate(HooksContext, HooksAttributeBean)
   */
  @Override
  public void attributePreUpdate(HooksContext hooksContext, HooksAttributeBean preUpdateBean) {
    Attribute attribute = preUpdateBean.getAttribute();
    
    GroupAttributeNameValidationHook.groupPreChangeAttribute(attribute.getAttrName(), attribute.getValue());
  }
  
}
