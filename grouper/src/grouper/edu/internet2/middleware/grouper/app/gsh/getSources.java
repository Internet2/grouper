/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;

/**
 * Find all {@link Subject} sources.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getSources.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
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

