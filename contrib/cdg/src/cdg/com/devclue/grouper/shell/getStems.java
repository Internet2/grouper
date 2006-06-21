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

/**
 * Query for stems by name.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getStems.java,v 1.4 2006-06-21 22:33:54 blair Exp $
 * @since   0.0.1
 */
public class getStems {

  // PUBLIC CLASS METHODS //

  /**
   * Query for stems by name.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Find stems with <i>name</i> as part of their name.
   * @return  {@link Set} of {@link Stem}s.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Set invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Stem            root  = StemFinder.findRootStem(s);
      GrouperQuery    gq    = GrouperQuery.createQuery(
        s, 
        new StemNameAnyFilter(name, root)
      );
      return gq.getStems();
    }
    catch (QueryException eQ) {
      throw new GrouperShellException(eQ);
    }
  } // protected static Set getStems(i, name)

} // public class getStems

