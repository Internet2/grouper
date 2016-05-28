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
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Get {@link Group} attribute value.
 * 
 * @author  blair christensen.
 * @version $Id: getGroupAttr.java,v 1.6 2009-04-14 07:41:24 mchyzer Exp $
 * @since   0.0.1
 */
public class getGroupAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Get {@link Group} attribute value.
   * 
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Group} to retrieve attribute on.
   * @param   attr  Name of attribute to retrieve.
   * @return  Value of attribute.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Object invoke(Interpreter i, CallStack stack, String name, String attr) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s = GrouperShell.getSession(i);
      Group           g = GroupFinder.findByName(s, name, true);
      if (attr.equals("createSubject"))    {
        return g.getCreateSubject();
      }
      else if (attr.equals("createTime"))       {
        return g.getCreateTime();
      }
      else if (attr.equals("modifySubject"))    {
        return g.getModifySubject();
      }
      else if (attr.equals("modifyTime"))       {
        return g.getModifyTime();
      }
      else {
        return g.getAttributeValue(attr, true, false);
      }
    }
    catch (AttributeNotFoundException eANF)     {
      GrouperShell.error(i, eANF);  
    }
    catch (GroupNotFoundException eNSNF)        {
      GrouperShell.error(i, eNSNF);
    }
    catch (SubjectNotFoundException eSNF)       {
      GrouperShell.error(i, eSNF);
    }
    throw new GrouperShellException(GshErrorMessages.GROUP_ATTR_INVALID + attr);
  } // public static boolean invoke(i, stack, name, attr)

} // public class getGroupAttr

