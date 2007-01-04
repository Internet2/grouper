/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;

/**
 * {@link GrouperShell} History.
 * <p/>
 * @author  blair christensen.
 * @version $Id: history.java,v 1.3 2007-01-04 17:17:45 blair Exp $
 * @since   0.0.1
 */
public class history {

  // PUBLIC CLASS METHODS //

  /**
   * Print commands that have been run.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static void invoke(Interpreter i, CallStack stack) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    ShellHelper.history(i, -1);
  } // public static void invoke(i, stack, parent, name)

  /**
   * Print the last <i>cnt</i> commands that have been run.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   cnt   Print the last <i>cnt</i> commands.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static void invoke(Interpreter i, CallStack stack, int cnt) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    ShellHelper.history(i, cnt);
  } // public static void invoke(i, stack, cnt)

} // public class history

