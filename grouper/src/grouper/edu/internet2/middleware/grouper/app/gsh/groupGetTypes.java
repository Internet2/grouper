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
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;

/**
 * Get a {@link Group}'s {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: groupGetTypes.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.1.0
 */
public class groupGetTypes {

  // PUBLIC CLASS METHODS //

  /**
   * Get a {@link Group}s {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link Group}.
   * @return  Set of {@link GroupType}s.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static Set invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return invoke(GrouperShell.getSession(i), name);
    }
    catch (GroupNotFoundException eGNF) {
      GrouperShell.error(i, eGNF);
    }
    throw new GrouperShellException(GshErrorMessages.GROUP_GETTYPES + name);
  }
  
  /**
   * Get a {@link Group}s {@link GroupType}.
   * <p/>
   * @param   grouperSession
   * @param   name        Name of {@link Group}.
   * @return  Set of {@link GroupType}s.
   */
  @SuppressWarnings("deprecation")
  public static Set invoke(GrouperSession grouperSession, String name) {
    Group g = GroupFinder.findByName(grouperSession, name, true);
    return g.getTypes();
  }

} // public class groupGetTypes

