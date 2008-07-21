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
 * Get a {@link Group}'s {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: groupGetTypes.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.1.0
 */
public class groupGetTypes {

  // PUBLIC CLASS METHODS //

  /**
   * Get a {@link Group}s {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link Group}.
   * @return  Set of {@link GroupType}s.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static Set invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      Group g = GroupFinder.findByName(GrouperShell.getSession(i), name);
      return g.getTypes();
    }
    catch (GroupNotFoundException eGNF) {
      GrouperShell.error(i, eGNF);
    }
    throw new GrouperShellException(GshErrorMessages.GROUP_GETTYPES + name);
  } // public static Set invoke(i, stack, name)

} // public class groupGetTypes

