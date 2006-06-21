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
 * Add stem.
 * <p/>
 * @author  blair christensen.
 * @version $Id: addStem.java,v 1.4 2006-06-21 20:28:55 blair Exp $
 * @since   0.0.1
 */
public class addStem {

  // PUBLIC CLASS METHODS //

  /**
   * Add a stem.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   parent      <i>name</i> of parent {@link Stem}.
   * @param   extn        <i>extension</i> of {@link Stem}.
   * @param   displayExtn <i>displayExtension</i> of {@link Stem}.
   * @since   0.0.1
   */
  public static void invoke(
    Interpreter i, CallStack stack, String parent, String extn, String displayExtn
  ) 
  {
    StemHelper.addStem(i, parent, extn, displayExtn);
  } // public static void invoke(i, stack, parent, name)

} // public class addStem

