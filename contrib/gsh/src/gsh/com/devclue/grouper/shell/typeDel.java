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
 * Delete a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeDel.java,v 1.4 2007-01-04 17:17:45 blair Exp $
 * @since   0.1.0
 */
public class typeDel {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a {@link Field} from a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Delete this {@link GroupType}.
   * @return  True if {@link GroupType} was deleted.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      GroupType       t = GroupTypeFinder.find(name);
      t.delete(s);
      return true;
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)                  {
      GrouperShell.error(i, eS);
    }
    return false;
  } // public static Field invoke(i, stack, name)

} // public class typeDel

