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
 * @version $Id: history.java,v 1.1 2006-06-20 19:40:47 blair Exp $
 * @since   1.0
 */
public class history {

  // PUBLIC CLASS METHODS //

  /**
   * Print commands that have been run.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @since   1.0
   */
  public static void invoke(Interpreter i, CallStack stack) {
    ShellHelper.history(i, -1);
  } // public static void invoke(i, stack, parent, name)

  /**
   * Print the last <i>cnt</i> commands that have been run.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   cnt   Print the last <i>cnt</i> commands.
   * @since   1.0
   */
  public static void invoke(Interpreter i, CallStack stack, int cnt) {
    ShellHelper.history(i, cnt);
  } // public static void invoke(i, stack, cnt)

} // public class history

