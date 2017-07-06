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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;

/**
 * Set {@link Stem} attribute value.
 * <p/>
 * @author  blair christensen.
 * @version $Id: setStemAttr.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.0.1
 */
public class setStemAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Set {@link Stem} attribute value.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Stem} to retrieve attribute on.
   * @param   attr  Name of attribute to set.
   * @param   val   New attribute value.
   * @return  True if attribute set to new value.1
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String name, String attr, String val
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      return invoke(s, name, attr, val);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (StemModifyException eNSM)            {
      GrouperShell.error(i, eNSM);  
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
    return false;
  }

  /**
   * Set {@link Stem} attribute value.
   * <p/>
   * @param   grouperSession
   * @param   name  <i>name</i> of {@link Stem} to retrieve attribute on.
   * @param   attr  Name of attribute to set.
   * @param   val   New attribute value.
   * @return  True if attribute set to new value.
   */
  public static boolean invoke(GrouperSession grouperSession, String name, String attr, String val) {
    Stem            ns  = StemFinder.findByName(grouperSession, name, true);
    if (attr.equals("description"))      {
      ns.setDescription(val);
      ns.store();
      return true;
    }
    else if (attr.equals("displayExtension")) {
      ns.setDisplayExtension(val);
      ns.store();
      return true;
    }
    else {
      throw new GrouperShellException(GshErrorMessages.STEM_ATTR_INVALID + attr);
    }
  }
} // public class setStemAttr

