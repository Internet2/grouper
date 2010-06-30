/**
 * 
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.io.Serializable;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Hooks to handle the field associated to the dynamic group type.
 * @author GIP RECIA - A. Deman
 * 18 May 2009
 *
 */
public class ESCOAttributeHooks extends AttributeHooks implements Serializable {
    
    /** Serial version UID.*/
    private static final long serialVersionUID = 9122480003264627999L;

    /** The logger to use. */
    private static final Log LOGGER = GrouperUtil.getLog(ESCOAttributeHooks.class);

    /**
     * Builds an instance of ESCOAttributeHooks.
     */
    public ESCOAttributeHooks() {
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Creation of an hooks of class: " + getClass().getSimpleName());
        }
    }

    /**
     * Tests and veto if needed a modification of an attribute that contains the logic definition of a group.
     * @param hooksContext The hook context.
     * @param preUpdateBean The available Grouper information.
     * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePreUpdate(HooksContext, HooksAttributeBean)
     */
    @Override
    public void attributePreUpdate(final HooksContext hooksContext, final HooksAttributeBean preUpdateBean) {

      Attribute attribute = preUpdateBean.getAttribute();
      
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("preUpdate attribute: " + attribute.getAttrName() 
            + " = '" + attribute.getValue() + "'");
      }
    }
    

    /**
     * Handles the fact that a dynamic groups becomes a static group.
     * @param hooksContext The hook context.
     * @param postDeleteBean The available Grouper information.
     */
    @Override
    public void attributePostDelete(final HooksContext hooksContext, 
            final HooksAttributeBean postDeleteBean) {
        
        Attribute attribute = postDeleteBean.getAttribute();
      
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("postDelete attribute: " 
                    + attribute.getGroupUuid() + ", " + attribute.getAttrName());
        }
    }

  
    /**
     * 
     * @param hooksContext
     * @param preInsertBean
     * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePreInsert(HooksContext, HooksAttributeBean)
     */
    @Override
    public void attributePreInsert(final HooksContext hooksContext, final HooksAttributeBean preInsertBean) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("preInsert attribute: " + preInsertBean.getAttribute().getAttrName() 
              + " = '" + preInsertBean.getAttribute().getValue() + "'");
        }
    }
   
}