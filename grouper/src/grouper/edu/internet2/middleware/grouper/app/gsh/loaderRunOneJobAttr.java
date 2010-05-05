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
    return GrouperLoader.runJobOnceForAttributeDef(grouperSession, attributeDef);
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

