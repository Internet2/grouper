/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;

/**
 * Get version information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: version.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
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
    return GrouperShellVersion.VERSION;
  } // public static String invoke(i, stack)

} // public class version

