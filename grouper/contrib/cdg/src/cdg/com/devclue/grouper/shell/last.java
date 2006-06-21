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
 * @version $Id: last.java,v 1.3 2006-06-21 22:33:54 blair Exp $
 * @since   0.0.1
 */
public class last {

  // PUBLIC CLASS METHODS //

  /**
   * Run last command.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @return  True if succeeds.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack) 
    throws  GrouperShellException
  {
    return ShellHelper.eval(i, -2); // As -1 will be the currently eval'd
                                    // command, -2 will get us the prior command.
                                    // TODO Yes, I know, this sucks.
  } // public static boolean invoke(i, stack, parent, name)

  /**
   * Run the command at position <i>idx</i>.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   idx   Run command <i>idx</i>.
   * @return  True if succeeds.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack, int idx) 
    throws  GrouperShellException
  {
    return ShellHelper.eval(i, idx);
  } // public static boolean invoke(i, stack, idx)

} // public class last

