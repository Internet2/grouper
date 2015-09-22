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
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.AttributeDefHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;


/**
 * When attribute defs are created or saved, make sure there are no name conflicts with other object types
 */
public class UniqueObjectAttributeDefHook extends AttributeDefHooks {


  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPreUpdate(HooksContext hooksContext,
      HooksAttributeDefBean preUpdateBean) {
    checkNameOfAttributeDef(preUpdateBean.getAttributeDef().getName());
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPreInsert(HooksContext hooksContext,
      HooksAttributeDefBean preInsertBean) {
    checkNameOfAttributeDef(preInsertBean.getAttributeDef().getName());
  }

  /**
   * make sure name is not used in other object types
   * @param name
   */
  private void checkNameOfAttributeDef(String name) {
    UniqueObjectGroupHook.assertNoGroupsWithThisNameExist(name);
    UniqueObjectStemHook.assertNoStemsWithThisNameExist(name);
    //dont check attribute defs
    UniqueObjectAttributeDefNameHook.assertNoAttributeDefNamesWithThisNameExist(name);
  }

  
  /**
   * make sure no groups by this name exist
   * @param name
   */
  public static void assertNoAttributeDefsWithThisNameExist(final String name) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        AttributeDef attributeDef = AttributeDefFinder.findByName(name, false);
        
        if (attributeDef != null) {
          throw new HookVeto("veto.uniqueObject.attributeDef.name", "The ID is already in use by an attribute definition, please use a different ID");
        }
        
        return null;
      }
    });
        
  }
  
}
