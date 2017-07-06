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
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.usdu.USDU;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * usdu all
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: usdu.java,v 1.1 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class usdu {

  /** constant to delete unresolvable memberships */
  public static final int DELETE = 1;

  /**
   * usdu all
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack) {
    return invoke(interpreter, stack, 0);
  }

  /**
   * usdu all
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param options 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, int options) {
    GrouperShell.setOurCommand(interpreter, true);
    try {
      GrouperSession  s = GrouperShell.getSession(interpreter);
      return invoke(s, options);
    } catch (Exception e) {
      GrouperShell.error(interpreter, e);
    }
    return "";    
  } // public static boolean invoke(i, stack, name)

  /**
   * @param grouperSession 
   * @return true if succeeds
   */
  public static String invoke(GrouperSession grouperSession) {
    return invoke(grouperSession, 0);
  }
  
  /**
   * @param grouperSession 
   * @param options 
   * @return true if succeeds
   */
  public static String invoke(GrouperSession grouperSession, int options) {
    boolean delete = GrouperUtil.hasOption(options, DELETE);
    USDU.resolveMembers(grouperSession, delete);
    return "usdu completed successfully";   
  }
  
} // public class resetRegistry

