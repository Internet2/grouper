/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;

/**
 * Get version information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: version.java,v 1.3 2007-01-04 17:17:45 blair Exp $
 * @since   0.0.1
 */
public class version {

  // PUBLIC CLASS METHODS //

  /**
   * Get version information.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @return  {@link GrouperShell} version.
   * @since   0.0.1
   */
  public static String invoke(Interpreter i, CallStack stack) {
    GrouperShell.setOurCommand(i, true);
    return GrouperShell.VERSION;
  } // public static String invoke(i, stack)

} // public class version

