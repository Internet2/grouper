/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  java.util.*;

/**
 * Run previous {@link GrouperShell} commands.
 * <p/>
 * @author  blair christensen.
 * @version $Id: last.java,v 1.1 2006-06-20 19:53:17 blair Exp $
 * @since   1.0
 */
public class last {

  // PUBLIC CLASS METHODS //

  /**
   * Run last command.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @since   1.0
   */
  public static void invoke(Interpreter i, CallStack stack) {
    ShellHelper.eval(i, -2);  // As -1 will be the currently eval'd
                              // command, -2 will get us the prior command.
  } // public static void invoke(i, stack, parent, name)

  /**
   * Run the command at position <i>idx</i>.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   idx   Run command <i>idx</i>.
   * @since   1.0
   */
  public static void invoke(Interpreter i, CallStack stack, int idx) {
    ShellHelper.eval(i, idx);
  } // public static void invoke(i, stack, idx)

} // public class last

