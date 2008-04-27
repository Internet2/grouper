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
 * Add a list to to a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeAddList.java,v 1.1.1.1 2008-04-27 14:52:17 tzeller Exp $
 * @since   0.1.0
 */
public class typeAddList {

  // PUBLIC CLASS METHODS //

  /**
   * Add a list to a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   type        Add to this {@link GroupType}.
   * @param   name        Name of list.
   * @param   read        {@link Privilege} required for reading.
   * @param   write       {@link Privilege} required for writing.
   * @return  {@link Field}
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static Field invoke(
    Interpreter i, CallStack stack, String type, String name, Privilege read, Privilege write
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      GroupType       t = GroupTypeFinder.find(type);
      return t.addList(s, name, read, write);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)                  {
      GrouperShell.error(i, eS);
    }
    throw new GrouperShellException(E.TYPE_ADDLIST + name);
  } // public static Field invoke(i, stack, type, name, read, write)

} // public class typeAddList

