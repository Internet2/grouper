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
 * Restore the Groups Registry to a default state.
 * <p/>
 * @author  blair christensen.
 * @version $Id: registryReset.java,v 1.1 2006-06-21 20:28:55 blair Exp $
 * @since   0.0.1
 */
public class registryReset {

  // PUBLIC CLASS METHODS //

  /**
   * Restore the Groups Registry to a default state.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack) {
    return RegistryHelper.reset(i);
  } // public static void invoke(i, stack, parent, name)

} // public class registryReset

