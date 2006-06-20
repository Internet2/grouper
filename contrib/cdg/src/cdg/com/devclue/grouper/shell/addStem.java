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
 * @version $Id: addStem.java,v 1.3 2006-06-20 19:53:17 blair Exp $
 * @since   1.0
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
   * @since   1.0
   */
  public static void invoke(
    Interpreter i, CallStack stack, String parent, String extn, String displayExtn
  ) 
  {
    StemHelper.addStem(i, parent, extn, displayExtn);
  } // public static void invoke(i, stack, parent, name)

} // public class addStem

