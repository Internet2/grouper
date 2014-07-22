/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: AttributeIncludeExcludeHook.java,v 1.4 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.SchemaException;
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
    boolean useGrouperRequireGroups = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperIncludeExclude.requireGroups.use", false);
    
    if (!useGrouperRequireGroups) {
      return;
    }
    
    Attribute attribute = postInsertBean.getAttribute();
    
    //make sure this is the right type
    String requireGroupsTypeName = GrouperConfig.retrieveConfig().propertyValueString("grouperIncludeExclude.requireGroups.type.name");

    String groupUuid = attribute.getGroupUuid();

    //there better be a session now!
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();

    try {
      GroupType requireGroupsType = GroupTypeFinder.find(requireGroupsTypeName, false);
      
      GroupType attributeGroupType = null;
      
      try {
        attributeGroupType = attribute.internal_getGroupType();
      } catch (SchemaException ise) {
        throw new RuntimeException("Problem in group: " + groupUuid + ", attribute: " + attribute.getAttrName(), ise);
      }
      
      if (!attributeGroupType.equals(requireGroupsType)) {
        return;
      }
      
      //not sure why this would be null... might get a stale object state
      Group typedGroup = attribute.retrieveGroup(true);

      GroupTypeTupleIncludeExcludeHook.manageIncludesExcludesAndGroups(grouperSession, typedGroup, 
          summaryForLog + " attribute '" + attribute.getAttrName() + "' for group: " + typedGroup.getExtension());
      
    } catch (Exception e) {
      throw new RuntimeException("Error doing include/exclude on group: " + groupUuid, e);
    }
  }

}
