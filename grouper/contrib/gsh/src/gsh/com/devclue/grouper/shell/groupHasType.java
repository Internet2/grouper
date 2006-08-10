/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/**
 * Verify whether a {@link Group} has a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: groupHasType.java,v 1.1 2006-08-10 18:47:53 blair Exp $
 * @since   0.0.2
 */
public class groupHasType {

  // PUBLIC CLASS METHODS //

  /**
   * Verify whether a {@link Group} has a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link Group}.
   * @param   type        Name of {@link GroupType}.
   * @return  True if {@link Group} has {@link GroupType}.
   * @throws  GrouperShellException
   * @since   0.0.2
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String name, String type
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      Group     g = GroupFinder.findByName(GrouperShell.getSession(i), name);
      GroupType t = GroupTypeFinder.find(type);
      return g.hasType(t);         
    }
    catch (GroupNotFoundException eGNF) {
      GrouperShell.error(i, eGNF);
    }
    catch (SchemaException eS)          {
      GrouperShell.error(i, eS);
    }
    return false;
  } // public static boolean invoke(i, stack, name, type)

} // public class groupHasType

