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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;


/**
 * When attribute def names are created or saved, make sure there are no name conflicts with other object types
 */
public class UniqueObjectAttributeDefNameHook extends AttributeDefNameHooks {


  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePreUpdate(HooksContext hooksContext,
      HooksAttributeDefNameBean preUpdateBean) {
    checkNameOfAttributeDefName(preUpdateBean.getAttributeDefName().getName());
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePreInsert(HooksContext hooksContext,
      HooksAttributeDefNameBean preInsertBean) {
    checkNameOfAttributeDefName(preInsertBean.getAttributeDefName().getName());

  }

  /**
   * make sure name is not used in other object types
   * @param name
   */
  private void checkNameOfAttributeDefName(String name) {
    UniqueObjectGroupHook.assertNoGroupsWithThisNameExist(name);
    UniqueObjectStemHook.assertNoStemsWithThisNameExist(name);
    UniqueObjectAttributeDefHook.assertNoAttributeDefsWithThisNameExist(name);
    //dont check attribute def names
  }

  /**
   * make sure no groups by this name exist
   * @param name
   */
  public static void assertNoAttributeDefNamesWithThisNameExist(final String name) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(name, false);
        
        if (attributeDefName != null) {
          throw new HookVeto("veto.uniqueObject.attributeDefName.name", "The ID is already in use by an attribute, please use a different ID");
        }
        
        return null;
      }
    });
        
  }
  
}
