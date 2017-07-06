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
import edu.internet2.middleware.grouper.GrouperSession;
import bsh.CallStack;
import bsh.Interpreter;

/**
 * Assert condition for GrouperShell testing.
 * <p/>
 * @author  blair christensen.
 * @version $Id: assertTrue.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class assertTrue {

  // PUBLIC CLASS METHODS //

  /**
   * Assert truth of condition.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   msg         Message text for assertion.
   * @param   cond        Boolean conditional.
   * @since   0.0.1
   */
  public static void invoke(
    Interpreter i, CallStack stack, String msg, boolean cond
  ) 
  {
    GrouperShell.setOurCommand(i, true);
    if (cond) {
      i.println("OK (" + msg + ")");
    }
    else {
      String error = "FAIL (" + msg + ") " 
    + "Line: "  + i.getNameSpace().getInvocationLine()
    + " : "     + i.getNameSpace().getInvocationText();
      i.error(
          error
      );
      if (GrouperShell.exitOnFailure) {
        System.exit(1);
      } else {
        //print it, which junit will pick up on
        System.err.println("Error: " + error);
      }
    }
  }
  
  /**
   * Assert truth of condition.
   * <p/>
   * @param   grouperSession
   * @param   msg         Message text for assertion.
   * @param   cond        Boolean conditional.
   * @since   0.0.1
   */
  public static void invoke(GrouperSession grouperSession, String msg, boolean cond) {
    if (cond) {
      System.out.println("OK (" + msg + ")");
    } else {
      System.err.println("Error: FAIL (" + msg + ")");
    }
  }
  
} // public class assertTrue

