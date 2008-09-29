/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;

/**
 * Get members of a group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getMembers.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class getMembers {

  // PUBLIC CLASS METHODS //

  /**
   * Get members of a group.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   group       Get {@link Member}s of this {@link Group}.
   * @return  {@link Set} of {@link Member}s.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Set invoke(
    Interpreter i, CallStack stack, String group
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      Group           g = GroupFinder.findByName(s, group);
      return g.getMembers();
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    return null;
  } // public static Set invoke(i, stack, group)

} // public class getMembers

