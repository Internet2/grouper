/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Add a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeAdd.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.1.0
 */
public class typeAdd {

  // PUBLIC CLASS METHODS //

  /**
   * Add a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link GroupType} to add.
   * @return  {@link GroupType}
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static GroupType invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return GroupType.createType(
        GrouperShell.getSession(i), name
      );
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)                  {
      GrouperShell.error(i, eS);
    }
    throw new GrouperShellException(GshErrorMessages.TYPE_ADD + name);
  } // public static GroupType invoke(i, stack, name)

} // public class typeAdd

