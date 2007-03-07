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
 * Add {@link RegistrySubject} to Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: addSubject.java,v 1.8 2007-03-07 16:07:38 blair Exp $
 * @since   0.0.1
 */
public class addSubject {

  // PUBLIC CLASS METHODS //

  /**
   * Add {@link RegistrySubject} to Groups Registry.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @param   name        Subject <i>name</i>.
   * @return  Added {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static RegistrySubject invoke(
    Interpreter i, CallStack stack, String id, String type, String name
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return RegistrySubject.add( GrouperShell.getSession(i), id, type, name );
    }
    catch (GrouperException eG)                 { 
      GrouperShell.error(i, eG);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    return null;
  } // public static RegistrySubject invoke(i, stack, parent, name)

} // public class addSubject

