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
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Delete a {@link GroupType} from a {@link Group}.
 * 
 * @author  blair christensen.
 * @version $Id: groupDelType.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.1.0
 */
public class groupDelType {

  // PUBLIC CLASS METHODS //

  /**
   * Delete a {@link GroupType} from a {@link Group}.
   * 
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link Group}.
   * @param   type        Name of {@link GroupType}.
   * @return  True if {@link GroupType} deleted from {@link Group}.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String name, String type
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      Group     g = GroupFinder.findByName(GrouperShell.getSession(i), name, true);
      GroupType t = GroupTypeFinder.find(type, true);
      g.deleteType(t);
      return true;
    }
    catch (GroupModifyException eGM)    {
      GrouperShell.error(i, eGM);
    }
    catch (GroupNotFoundException eGNF) {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)          {
      GrouperShell.error(i, eS);
    }
    return false;
  } // public static boolean invoke(i, stack, name, type)

} // public class groupDelType

