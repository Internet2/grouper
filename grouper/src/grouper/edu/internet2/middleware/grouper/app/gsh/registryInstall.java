/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.registry.RegistryInstall;

/**
 * Install default data in the registry if it is not already there
 * <p/>
 * @author  blair christensen.
 * @version $Id: registryInstall.java,v 1.1 2008-09-13 03:16:54 mchyzer Exp $
 * @since   0.0.1
 */
public class registryInstall {

  // PUBLIC CLASS METHODS //

  /**
   * Install default data in the registry if it is not already there
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter i, CallStack stack) {
    GrouperShell.setOurCommand(i, true);
    RegistryInstall.install();
    return "Registry installed: default data inserted if it was not already there, e.g. root stem";    
  } // public static boolean invoke(i, stack, name)

} // public class resetRegistry

