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
/**
 * 
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Dynamic groups hook.
 * This hook managed the creation of the dynamic groups.
 * @author GIP RECIA - A. Deman
 * 9 February. 2009
 *
 */
public class ESCOGroupHooks extends GroupHooks implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 8623167847413079614L;

    /** The logger to use. */
    private static final Log LOGGER = GrouperUtil.getLog(ESCOAttributeHooks.class);

    /**
     * Builds an instance of ESCOGroupHooks.
     */
    public ESCOGroupHooks() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Creation of an hooks of class: " + getClass().getSimpleName());
        }
    }


    /**
     * Post commit insert hook point.
     * @param hooksContext The hook context.
     * @param postCommitInsertBean The available Grouper information.
     * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostCommitInsert(HooksContext, HooksGroupBean)
     */
    @Override
    public void groupPostCommitInsert(final HooksContext hooksContext, 
            final HooksGroupBean postCommitInsertBean) {

        Group group = postCommitInsertBean.getGroup();

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("postCommitInsert group: " + group.getName() + ", types: " + groupTypes(group));
        }
    }
    
    /**
     * group types
     * @param group
     * @return the types
     */
    private static String groupTypes(Group group) {
      StringBuilder result = new StringBuilder();
      Set<GroupType> groupTypes = group.getTypes();
      for (GroupType groupType : groupTypes) {
        result.append(groupType.getName()).append(", ");
      }
      return result.toString();
    }
    
    /**
     * Post delete hook point.
     * @param hooksContext The hook context.
     * @param postDeleteBean The available Grouper information.
     */
    @Override
    public void groupPostDelete(final HooksContext hooksContext, 
            final HooksGroupBean postDeleteBean) {

      Group group = postDeleteBean.getGroup();

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("postDelete group: " + group.getName() + ", types: " + groupTypes(group));
      }
    }


    /**
     * Pre update hook point.
     * @param hooksContext The hook context.
     * @param preUpdateBean The available Grouper information.
     * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(HooksContext, HooksGroupBean)
     */
    @Override
    public void groupPreUpdate(final HooksContext hooksContext, final HooksGroupBean preUpdateBean) {
      Group group = preUpdateBean.getGroup();

      if (LOGGER.isDebugEnabled()) {
        
        StringBuilder attributeValues = new StringBuilder("different fields: ");
        for (String differentField : group.dbVersionDifferentFields()) {
          attributeValues.append("[").append(differentField).append(": '")
            .append(attributeOrFieldValue(group.dbVersion(), differentField)).append("' => '")
            .append(attributeOrFieldValue(group, differentField)).append("'], ");
        }
        
        LOGGER.debug("preUpdate group: " + group.getName() + ", types: " + groupTypes(group) + ", " + attributeValues);
      }
    }
    
    /**
     * 
     * @param group
     * @param attributeOrField
     * @return the object
     */
    private static Object attributeOrFieldValue(Group group, String attributeOrField) {
      Field field = GrouperUtil.field(group.getClass(), attributeOrField, true, false);
      if (field != null) {
        return GrouperUtil.propertyValue(group, attributeOrField);
      }
      return group.getAttributeValue(attributeOrField, false, true);
    }
    
}
