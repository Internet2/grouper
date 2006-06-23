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
 * Reset Groups Registry to default state.
 * <p/>
 * @author  blair christensen.
 * @version $Id: resetRegistry.java,v 1.1 2006-06-23 17:30:09 blair Exp $
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

