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
 * Add stem.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getStems.java,v 1.1 2006-06-20 18:26:43 blair Exp $
 * @since   1.0
 */
public class getStems {

  // PUBLIC CLASS METHODS //

  /**
   * Query for stems by name.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Find stems with <i>name</i> as part of their name.
   * @since   1.0
   */
  public static void invoke(Interpreter i, CallStack stack, String name) 
  {
    StemHelper.getStems(i, name);
  } // public static void invoke(i, stack, name)

} // public class getStems

