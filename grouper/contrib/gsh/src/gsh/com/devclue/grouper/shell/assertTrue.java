/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;

/**
 * Assert condition for GrouperShell testing.
 * <p/>
 * @author  blair christensen.
 * @version $Id: assertTrue.java,v 1.1 2006-08-22 15:56:36 blair Exp $
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
      i.error(
          "FAIL (" + msg + ") " 
        + "Line: "  + i.getNameSpace().getInvocationLine()
        + " : "     + i.getNameSpace().getInvocationText()
      );
      System.exit(1);
    }
  } // public static void invoke(i, stack, msg, cond)

} // public class assertTrue

