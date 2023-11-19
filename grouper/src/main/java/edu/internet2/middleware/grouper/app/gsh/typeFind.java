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
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Find a {@link GroupType}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeFind.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.1.0
 */
public class typeFind {

  // PUBLIC CLASS METHODS //

  /**
   * Find a {@link GroupType}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link GroupType} to find.
   * @return  {@link GroupType}
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static GroupType invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return invoke(null, name);
    }
    catch (SchemaException eS) {
      GrouperShell.error(i, eS);
    }
    throw new GrouperShellException(GshErrorMessages.TYPE_NOTFOUND + name);
  }

  /**
   * Find a {@link GroupType}.
   * <p/>
   * @param   grouperSession
   * @param   name        Name of {@link GroupType} to find.
   * @return  {@link GroupType}
   */
  @SuppressWarnings("deprecation")
  public static GroupType invoke(GrouperSession grouperSession, String name) {
    return GroupTypeFinder.find(name, true);
  }
} // public class typeFind

