/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;

/**
 * Add an {@link Attribute} to a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeAddAttr.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 * @since   0.0.2
 */
public class typeAddAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Add an {@link Attribute} to a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   type        Add to this {@link GroupType}.
   * @param   name        Name of {@link Attribute}.
   * @param   read        {@link Privilege} required for reading.
   * @param   write       {@link Privilege} required for writing.
   * @param   req         Is {@link Attribute} required.
   * @return  {@link Field}
   * @throws  GrouperShellException
   * @since   0.0.2
   */
  public static Field invoke(
    Interpreter i, CallStack stack, String type, String name, Privilege read, 
    Privilege write, boolean req
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      GroupType       t = GroupTypeFinder.find(type);
      return t.addAttribute(s, name, read, write, req);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)                  {
      GrouperShell.error(i, eS);
    }
    throw new GrouperShellException(E.TYPE_ADDATTR + name);
  } // public static Field invoke(i, stack, type, name, read, write, req)

} // public class typeAddAttr

