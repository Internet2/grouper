/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;

/**
 * Assert condition for GrouperShell testing.
 * <p/>
 * @author  blair christensen.
 * @version $Id: assertTrue.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
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
  } // public static void invoke(i, stack, msg, cond)

} // public class assertTrue

