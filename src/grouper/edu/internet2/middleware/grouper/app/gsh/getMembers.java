/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;

import  java.util.*;

/**
 * Get members of a group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getMembers.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
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

