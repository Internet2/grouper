/*******************************************************************************
 * Copyright 2015 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.hooks.examples;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;


/**
 * When groups are created or saved, make sure there are no name conflicts with other object types
 */
public class UniqueObjectGroupHook extends GroupHooks {

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
    checkGroupName(preUpdateBean.getGroup().getName(), preUpdateBean.getGroup().getAlternateName());
  }

  /**
   * make sure name is not used in other object types
   * @param name
   * @param alternateName
   */
  private void checkGroupName(String name, String alternateName) {
    //dont check groups
    UniqueObjectStemHook.assertNoStemsWithThisNameExist(name);
    UniqueObjectAttributeDefHook.assertNoAttributeDefsWithThisNameExist(name);
    UniqueObjectAttributeDefNameHook.assertNoAttributeDefNamesWithThisNameExist(name);
    if (!StringUtils.isBlank(alternateName)) {
      UniqueObjectStemHook.assertNoStemsWithThisNameExist(alternateName);
      UniqueObjectAttributeDefHook.assertNoAttributeDefsWithThisNameExist(alternateName);
      UniqueObjectAttributeDefNameHook.assertNoAttributeDefNamesWithThisNameExist(alternateName);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    checkGroupName(preInsertBean.getGroup().getName(), preInsertBean.getGroup().getAlternateName());
  }

  /**
   * make sure no groups by this name exist
   * @param name
   */
  public static void assertNoGroupsWithThisNameExist(final String name) {

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        Group group = GroupFinder.findByName(grouperSession, name, false);

        if (group != null) {
          throw new HookVeto("veto.uniqueObject.group.name", "The ID is already in use by a group, please use a different ID");
        }

        return null;
      }
    });

  }
  
}
