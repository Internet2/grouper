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
import java.util.LinkedHashSet;
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Find <code>Field</code>s belonging to a <code>GroupType</code>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeGetFields.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.1.0
 */
public class typeGetFields {

  // PUBLIC CLASS METHODS //

  /**
   * Find <code>Field</code>s belonging to a <code>GroupType</code>.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Find this <code>GroupType</code>'s <code>Field</code>s.
   * @return  {@link Set} of {@link Field}s.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static Set invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    Set fields = new LinkedHashSet();
    GrouperShell.setOurCommand(i, true);
    try {
      GroupType t = GroupTypeFinder.find(name, true);
      fields = t.getFields();
    }
    catch (SchemaException eS) {
      GrouperShell.error(i, eS);
    }
    return fields;
  } // public static Set invoke(i, stack, name)

} // public class typeGetFields

