/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;

/**
 * find all bad memberships by stem
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: findBadMembershipsByStem.java,v 1.1 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class findBadMembershipsByStem {

  /**
   * find bad memberships by stem
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param stem 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, Stem stem) {
    GrouperShell.setOurCommand(interpreter, true);
    try {
      FindBadMemberships.checkStem(stem);
      return "findBadMembershipsByStem completed successfully";
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}

