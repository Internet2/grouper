/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;

/**
 * Find all {@link Subject} sources.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getSources.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class getSources {

  // PUBLIC CLASS METHODS //

  /**
   * Find all {@link Subject} sources.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @return  Set of sources.
   * @since   0.0.1
   */
  public static Set invoke(Interpreter i, CallStack stack) {
    GrouperShell.setOurCommand(i, true);
    return SubjectFinder.getSources();
  } // public static Stem invoke(i, stack, parent, name)

} // public class getSources

