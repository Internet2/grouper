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
 * {@link GrouperShell} History.
 * <p/>
 * @author  blair christensen.
 * @version $Id: history.java,v 1.3 2006-06-21 22:33:54 blair Exp $
 * @since   0.0.1
 */
public class history {

  // PUBLIC CLASS METHODS //

  /**
   * Print commands that have been run.
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
    return ShellHelper.history(i, -1);
  } // public static boolean invoke(i, stack, parent, name)

  /**
   * Print the last <i>cnt</i> commands that have been run.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   cnt   Print the last <i>cnt</i> commands.
   * @return  True if succeeds.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack, int cnt) 
    throws  GrouperShellException
  {
    return ShellHelper.history(i, cnt);
  } // public static boolean invoke(i, stack, cnt)

} // public class history

