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
 * @version $Id: last.java,v 1.1 2006-06-23 17:30:11 blair Exp $
 * @since   0.0.1
 */
public class last {

  // PROTECTED CLASS CONSTANTS //
  protected static final int LAST = -1;


  // PUBLIC CLASS METHODS //

  /**
   * Run last command.
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
    ShellHelper.last(i, LAST); // As -1 will be the currently eval'd
                               // command, -2 will get us the prior command.
                               // TODO Yes, I know, this sucks.
  } // public static void invoke(i, stack, parent, name)

  /**
   * Run the command at position <i>idx</i>.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   idx   Run command <i>idx</i>.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static void invoke(Interpreter i, CallStack stack, int idx) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    ShellHelper.last(i, idx);
  } // public static void invoke(i, stack, idx)

} // public class last

