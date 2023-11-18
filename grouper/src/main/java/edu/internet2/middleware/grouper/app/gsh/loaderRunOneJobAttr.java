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
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDef;

/**
 * run one loader job by name or group
 * <p/>
 * @author  Chris Hyzer
 * @version $Id$
 * @since   0.0.1
 */
public class loaderRunOneJobAttr {

  /**
   * run one loader job
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param attributeDef 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, AttributeDef attributeDef) {
    GrouperShell.setOurCommand(interpreter, true);
    GrouperSession  grouperSession = null;
    try {
      grouperSession     = GrouperShell.getSession(interpreter);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return invoke(grouperSession, attributeDef);
  }

  /**
   * run one loader job
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param jobName 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, String jobName) {
    GrouperShell.setOurCommand(interpreter, true);
    GrouperSession  grouperSession = null; 
    
    try {
      grouperSession = GrouperShell.getSession(interpreter);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return invoke(grouperSession, jobName);
  }
  
  /**
   * run one loader job
   * <p/>
   * @param grouperSession
   * @param attributeDefOrJobName 
   * @return  True if succeeds.
   */
  public static String invoke(GrouperSession grouperSession, Object attributeDefOrJobName) {
    if (attributeDefOrJobName instanceof AttributeDef) {
      return GrouperLoader.runJobOnceForAttributeDef(grouperSession, (AttributeDef)attributeDefOrJobName);
    }
    
    return GrouperLoader.runOnceByJobName(grouperSession, (String)attributeDefOrJobName);
  }
}

