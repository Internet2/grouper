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
import edu.internet2.middleware.grouper.misc.FindBadMemberships;

/**
 * find all bad memberships by group
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: findBadMembershipsByGroup.java,v 1.1 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class findBadMembershipsByGroup {

  /**
   * find bad memberships by group
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param group 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, Group group) {
    GrouperShell.setOurCommand(interpreter, true);
    try {
      FindBadMemberships.checkGroup(group);
      return "findBadMembershipsByGroup completed successfully";
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}

