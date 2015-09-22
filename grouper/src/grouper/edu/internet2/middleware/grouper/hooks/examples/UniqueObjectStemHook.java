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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.StemHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksStemBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

/**
 * When folders are created or saved, make sure there are no name conflicts with other object types
 */
public class UniqueObjectStemHook extends StemHooks {

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPreUpdate(HooksContext hooksContext, HooksStemBean preUpdateBean) {
    checkStemName(preUpdateBean.getStem().getName(), preUpdateBean.getStem().getAlternateName());
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.StemHooks#stemPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksStemBean)
   */
  @Override
  public void stemPreInsert(HooksContext hooksContext, HooksStemBean preInsertBean) {
    checkStemName(preInsertBean.getStem().getName(), preInsertBean.getStem().getAlternateName());
  }

  /**
   * make sure name is not used in other object types
   * @param name
   * @param alternateName
   */
  private void checkStemName(String name, String alternateName) {
    //dont check stems
    UniqueObjectGroupHook.assertNoGroupsWithThisNameExist(name);
    UniqueObjectAttributeDefHook.assertNoAttributeDefsWithThisNameExist(name);
    UniqueObjectAttributeDefNameHook.assertNoAttributeDefNamesWithThisNameExist(name);
    if (!StringUtils.isBlank(alternateName)) {
      UniqueObjectGroupHook.assertNoGroupsWithThisNameExist(alternateName);
      UniqueObjectAttributeDefHook.assertNoAttributeDefsWithThisNameExist(alternateName);
      UniqueObjectAttributeDefNameHook.assertNoAttributeDefNamesWithThisNameExist(alternateName);
    }
  }

  /**
   * make sure no groups by this name exist
   * @param name
   */
  public static void assertNoStemsWithThisNameExist(final String name) {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Stem stem = StemFinder.findByName(grouperSession, name, false);
        
        if (stem != null) {
          throw new HookVeto("veto.uniqueObject.stem.name", "The ID is already in use by a folder, please use a different ID");
        }
        
        return null;
      }
    });
        
  }
  
}
