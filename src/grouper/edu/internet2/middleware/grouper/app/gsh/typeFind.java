/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Find a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeFind.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
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
      return GroupTypeFinder.find(name, true);
    }
    catch (SchemaException eS) {
      GrouperShell.error(i, eS);
    }
    throw new GrouperShellException(GshErrorMessages.TYPE_NOTFOUND + name);
  } // public static GroupType invoke(i, stack, name)

} // public class typeFind

