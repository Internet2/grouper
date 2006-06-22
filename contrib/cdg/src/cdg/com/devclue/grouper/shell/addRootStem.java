/*
 * Copyright (C) 2006 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;

/**
 * Add stem.
 * <p/>
 * @author  blair christensen.
 * @version $Id: addRootStem.java,v 1.4 2006-06-22 17:46:29 blair Exp $
 * @since   0.0.1
 */
public class addRootStem {

  // PUBLIC CLASS METHODS //

  /**
   * Add a root stem.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   extn        <i>extension</i> of {@link Stem}.
   * @param   displayExtn <i>displayExtension</i> of {@link Stem}.
   * @return  Added {@link Stem}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Stem invoke(
    Interpreter i, CallStack stack, String extn, String displayExtn
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    return StemHelper.addStem(i, null, extn, displayExtn);
  } // public static Stem invoke(i, stack, name)

} // public class addRootStem

