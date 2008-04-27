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
 * Add an <code>Attribute</code> to a <code>GroupType</code>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeAddAttr.java,v 1.1.1.1 2008-04-27 14:52:17 tzeller Exp $
 * @since   0.1.0
 */
public class typeAddAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Add an <code>Attribute</code> to a <code>GroupType</code>.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   type        Add to this <code>GroupType</code>.
   * @param   name        Name of <code>Attribute</code>.
   * @param   read        <code>Privilege</code> required for reading.
   * @param   write       <code>Privilege</code> required for writing.
   * @param   req         Is <code>Attribute</code> required.
   * @return  <code>Field</code>
   * @throws  GrouperShellException
   * @since   0.1.0
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

