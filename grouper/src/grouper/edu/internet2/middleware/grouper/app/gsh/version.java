/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import edu.internet2.middleware.grouper.misc.GrouperInfo;
import bsh.CallStack;
import bsh.Interpreter;

/**
 * Get version information.
 * <p/>
 * @author  blair christensen.
 * @version $Id: version.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
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
    GrouperInfo.grouperInfo();
    return "";
  } // public static String invoke(i, stack)

} // public class version

