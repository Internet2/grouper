/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Stem;

/**
 * Add stem.
 * <p/>
 * @author  blair christensen.
 * @version $Id: addStem.java,v 1.2 2008-09-29 03:38:28 mchyzer Exp $
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
   * @return  Added {@link Stem}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Stem invoke(
    Interpreter i, CallStack stack, String parent, String extn, String displayExtn
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    return StemHelper.addStem(i, parent, extn, displayExtn);
  } // public static Stem invoke(i, stack, parent, name)

} // public class addStem

