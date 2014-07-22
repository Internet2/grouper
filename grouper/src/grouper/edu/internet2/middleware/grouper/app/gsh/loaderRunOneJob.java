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
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;

/**
 * run one loader job by name or group
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: loaderRunOneJob.java,v 1.2 2008-11-08 08:15:33 mchyzer Exp $
 * @since   0.0.1
 */
public class loaderRunOneJob {

  /**
   * run one loader job
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param group 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, Group group) {
    GrouperShell.setOurCommand(interpreter, true);
    GrouperSession  grouperSession = null;
    try {
      grouperSession     = GrouperShell.getSession(interpreter);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return GrouperLoader.runJobOnceForGroup(grouperSession, group);
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

    return GrouperLoader.runOnceByJobName(grouperSession, jobName);
  }
}

