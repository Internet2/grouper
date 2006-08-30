/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  net.sf.hibernate.*;

/**
 * Add {@link HibernateSubject} to Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: addSubject.java,v 1.3 2006-08-30 18:35:38 blair Exp $
 * @since   0.0.1
 */
public class addSubject {

  // PUBLIC CLASS METHODS //

  /**
   * Add {@link HibernateSubject} to Groups Registry.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @param   name        Subject <i>name</i>.
   * @return  Added {@link HibernateSubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static HibernateSubject invoke(
    Interpreter i, CallStack stack, String id, String type, String name
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return HibernateSubject.add(id, type, name);
    }
    catch (HibernateException eH) { 
      GrouperShell.error(i, eH);
    }
    return null;
  } // public static HibernateSubject invoke(i, stack, parent, name)

} // public class addSubject

