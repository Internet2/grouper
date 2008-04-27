/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;

/**
 * Find a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeFind.java,v 1.1.1.1 2008-04-27 14:52:17 tzeller Exp $
 * @since   0.1.0
 */
public class typeFind {

  // PUBLIC CLASS METHODS //

  /**
   * Find a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link GroupType} to find.
   * @return  {@link GroupType}
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static GroupType invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return GroupTypeFinder.find(name);
    }
    catch (SchemaException eS) {
      GrouperShell.error(i, eS);
    }
    throw new GrouperShellException(E.TYPE_NOTFOUND + name);
  } // public static GroupType invoke(i, stack, name)

} // public class typeFind

