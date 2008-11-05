/*
 * @author mchyzer
 * $Id: AttributeIncludeExcludeHook.java,v 1.2 2008-11-05 05:10:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.AttributeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * you can auto create groups to facilitate include and exclude lists, and required dependent complement groups
 * 
 * </pre>
 */
public class AttributeIncludeExcludeHook extends AttributeHooks {
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostInsert(HooksContext hooksContext,
      HooksAttributeBean postInsertBean) {
    
    manageIncludesExcludesAndGroups(postInsertBean, "insert");
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostDelete(HooksContext hooksContext,
      HooksAttributeBean postDeleteBean) {
    manageIncludesExcludesAndGroups(postDeleteBean, "delete");
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostUpdate(HooksContext hooksContext,
      HooksAttributeBean postUpdateBean) {
    manageIncludesExcludesAndGroups(postUpdateBean, "update");
  }

  /**
   * @param postInsertBean
   * @param summaryForLog summary for log message
   */
  public static void manageIncludesExcludesAndGroups(HooksAttributeBean postInsertBean, String summaryForLog) {
    boolean useGrouperRequireGroups = GrouperConfig.getPropertyBoolean("grouperIncludeExclude.requireGroups.use", false);
    
    if (!useGrouperRequireGroups) {
      return;
    }
    
    Attribute attribute = postInsertBean.getAttribute();

    Field attributeField = FieldFinder.findById(attribute.getFieldId());
    
    //make sure this is the right type
    String requireGroupsTypeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.type.name");

    String groupUuid = attribute.getGroupUuid();

    //there better be a session now!
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();

    try {
      GroupType requireGroupsType = GroupTypeFinder.find(requireGroupsTypeName, false);
      
      GroupType attributeGroupType = null;
      
      try {
        attributeGroupType = attributeField.getGroupType();
      } catch (IllegalStateException ise) {
        throw new RuntimeException("Problem in group: " + groupUuid + ", attribute: " + attribute.getAttrName(), ise);
      }
      
      if (!attributeGroupType.equals(requireGroupsType)) {
        return;
      }
      
      Group typedGroup = GroupFinder.findByUuid(grouperSession, groupUuid);

      GroupTypeTupleIncludeExcludeHook.manageIncludesExcludesAndGroups(grouperSession, typedGroup, 
          summaryForLog + " attribute '" + attributeField.getName() + "' for group: " + typedGroup.getExtension());
      
    } catch (Exception e) {
      throw new RuntimeException("Error doing include/exclude on group: " + groupUuid, e);
    }
  }

}
