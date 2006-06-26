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
import  net.sf.hibernate.*;

/**
 * Find a {@link Subject}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: findSubject.java,v 1.2 2006-06-26 14:29:24 blair Exp $
 * @since   0.0.1
 */
public class findSubject {

  // PUBLIC CLASS METHODS //

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @return  Found {@link HibernateSubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findById(id);
    }
    catch (SubjectNotFoundException eSNF) {
      GrouperShell.error(i, eSNF);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name)

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @return  Found {@link HibernateSubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id, String type
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findById(id, type);
    }
    catch (SubjectNotFoundException eSNF) {
      GrouperShell.error(i, eSNF);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name, type)

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @param   source      Subject <i>source</i>.
   * @return  Found {@link HibernateSubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id, String type, String source
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findById(id, type, source);
    }
    catch (SourceUnavailableException eSNA) {
      GrouperShell.error(i, eSNA);
    }
    catch (SubjectNotFoundException eSNF) {
      GrouperShell.error(i, eSNF);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name, type, source)

} // public class findSubject

