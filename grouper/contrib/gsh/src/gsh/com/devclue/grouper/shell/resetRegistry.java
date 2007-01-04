/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;

/**
 * Reset Groups Registry to default state.
 * <p/>
 * @author  blair christensen.
 * @version $Id: resetRegistry.java,v 1.3 2007-01-04 17:17:45 blair Exp $
 * @since   0.0.1
 */
public class resetRegistry {

  // PUBLIC CLASS METHODS //

  /**
   * Reset Groups Registry to default state.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack) {
    GrouperShell.setOurCommand(i, true);
    RegistryReset.reset();
    return true;    
  } // public static boolean invoke(i, stack, name)

} // public class resetRegistry

