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
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;

/**
 * Add an <code>Attribute</code> to a <code>GroupType</code>.
 * <p/>
 * @author  blair christensen.
 * @version $Id: typeAddAttr.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.1.0
 */
public class typeAddAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Add an <code>Attribute</code> to a <code>GroupType</code>.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   type        Add to this <code>GroupType</code>.
   * @param   name        Name of <code>Attribute</code>.
   * @return  <code>Field</code>
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static AttributeDefName invoke(
    Interpreter i, CallStack stack, String type, String name
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      return invoke(s, type, name);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)                  {
      GrouperShell.error(i, eS);
    }
    throw new GrouperShellException(GshErrorMessages.TYPE_ADDATTR + type);
  }
  
  /**
   * Add an <code>Attribute</code> to a <code>GroupType</code>.
   * <p/>
   * @param   grouperSession
   * @param   type        Add to this <code>GroupType</code>.
   * @param   name        Name of <code>Attribute</code>.
   * @return  <code>Field</code>
   */
  @SuppressWarnings("deprecation")
  public static AttributeDefName invoke(GrouperSession grouperSession, String type, String name) {
    GroupType       t = GroupTypeFinder.find(type, true);
    return t.addAttribute(grouperSession, name);
  }

} // public class typeAddAttr

