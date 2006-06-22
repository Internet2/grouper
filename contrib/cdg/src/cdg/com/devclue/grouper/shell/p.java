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
 * Print returned results.
 * <p/>
 * @author  blair christensen.
 * @version $Id: p.java,v 1.2 2006-06-22 15:03:09 blair Exp $
 * @since   0.0.1
 */
public class p {

  // PUBLIC CLASS METHODS //

  /**
   * FIXME Print returned results.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   obj   Object to print.
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack, Object obj) {
    i.println("I AM: (" + obj.getClass() + ") (" + obj.toString() + ")");
    return true;
  } // public static boolean invoke(i, stack, obj)

} // public class p

