/*
 * @author mchyzer
 * $Id: AttributeSecurityFromTypeHook.java,v 1.2 2009-03-15 06:37:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * you can secure certain attributes from group types which can only be edited (added/removed/deleted)
 * based on if the user doing the work is in a certain group (or wheel), or if the user is only a wheel group member.
 * 
 * normally a user with admin rights on a group can edit the group attributes
 * 
 * </pre>
 */
public class AttributeSecurityFromTypeHook extends AttributeHooks {
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostInsert(HooksContext hooksContext,
      HooksAttributeBean postInsertBean) {
    manageSecurity(postInsertBean, "adding");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostDelete(HooksContext hooksContext,
      HooksAttributeBean postDeleteBean) {
    manageSecurity(postDeleteBean, "deleting");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostUpdate(HooksContext hooksContext,
      HooksAttributeBean postUpdateBean) {
    manageSecurity(postUpdateBean, "changing");
  }

  /**
   * @param postInsertBean
   * @param summaryForLog summary for log message
   */
  public static void manageSecurity(HooksAttributeBean postInsertBean, String summaryForLog) {
    boolean useGrouperRequireGroups = GrouperConfig.getPropertyBoolean("grouperIncludeExclude.requireGroups.use", false);
    
    if (!useGrouperRequireGroups) {
      return;
    }
    
    Attribute attribute = postInsertBean.getAttribute();

    Field attributeField = FieldFinder.findById(attribute.getFieldId(), true);
    
    GroupType attributeGroupType = null;
    
    try {
      attributeGroupType = attributeField.getGroupType();
    } catch (IllegalStateException ise) {
      throw new RuntimeException("Cant find group type for field: " + attributeField, ise);
    }
    
    GroupTypeSecurityHook.vetoIfNecessary(postInsertBean.getAttribute().getGroupUuid(), attributeGroupType.getUuid(),
        summaryForLog + " attribute " + attributeField.getName());
  }

}
