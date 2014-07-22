/**
 * Copyright 2012 Internet2
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.usdu.USDU;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;

/**
 * usdu by a specific source
 * <p/>
 * @author  Chris Hyzer
 * @version $Id: usduBySource.java,v 1.1 2008-09-29 03:38:28 mchyzer Exp $
 * @since   0.0.1
 */
public class usduBySource {

  /**
   * usdu by source
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param sourceName 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, String sourceName) {
    return invoke(interpreter, stack, sourceName, 0);
  }

  /**
   * usdu by source
   * <p/>
   * @param   interpreter     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param sourceName 
   * @param options 
   * @return  True if succeeds.
   * @since   0.0.1
   */
  public static String invoke(Interpreter interpreter, CallStack stack, String sourceName, int options) {
    GrouperShell.setOurCommand(interpreter, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(interpreter);
      Source source = SubjectFinder.getSource(sourceName);
      boolean delete = GrouperUtil.hasOption(options, usdu.DELETE);
      USDU.resolveMembers(s, source, delete);
      return "usdu completed successfully";
    } catch (Exception e) {
      GrouperShell.error(interpreter, e);
    }
    return "";    
  } // public static boolean invoke(i, stack, name)

} // public class resetRegistry

