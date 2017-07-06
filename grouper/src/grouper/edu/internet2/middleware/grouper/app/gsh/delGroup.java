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
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;

/**
 * Delete a group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: delGroup.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.0.1
 */
public class delGroup {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a  stem.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Group} to delete.
   * @return  True if {@link Group} deleted.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      return invoke(s, name);
    }
    catch (GroupDeleteException eGD)            {
      GrouperShell.error(i, eGD);
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    return false;
  }
  
  /**
   * Delete a  stem.
   * <p/>
   * @param   grouperSession
   * @param   name  <i>name</i> of {@link Group} to delete.
   * @return  True if {@link Group} deleted.
   */
  public static boolean invoke(GrouperSession grouperSession, String name) {
    Group           g = GroupFinder.findByName(grouperSession, name, true);
    g.delete();
    return true;
  }

} // public class delGroup

