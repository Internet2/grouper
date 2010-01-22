/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * Reset Groups Registry to default state.
 * <p/>
 * @author  blair christensen.
 * @version $Id: resetRegistry.java,v 1.3 2008-09-29 03:38:28 mchyzer Exp $
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
  public static String invoke(Interpreter i, CallStack stack) {
    GrouperShell.setOurCommand(i, true);
    RegistryReset.reset();
    return "Registry reset: all data deleted, and default data inserted, e.g. root stem";    
  } // public static boolean invoke(i, stack, name)

} // public class resetRegistry

