/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;

/**
 * find all bad memberships
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: findBadMemberships.java,v 1.1 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class findBadMemberships {

  /**
   * find bad memberships
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack) {
    GrouperShell.setOurCommand(interpreter, true);
    try {
      FindBadMemberships.checkAll();
      return "findBadMemberships completed successfully";
    } catch (SessionException se) {
      throw new RuntimeException(se);
    }
  }

}

