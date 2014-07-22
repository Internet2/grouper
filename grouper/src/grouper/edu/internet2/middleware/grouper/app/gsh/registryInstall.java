/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

