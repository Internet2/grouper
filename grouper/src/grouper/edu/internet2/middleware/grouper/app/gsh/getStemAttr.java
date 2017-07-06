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
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Get {@link Stem} attribute value.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getStemAttr.java,v 1.4 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.0.1
 */
public class getStemAttr {

  // PUBLIC CLASS METHODS //

  /**
   * Get {@link Stem} attribute value.
   * <p/>
   * @param   i     BeanShell interpreter.
   * @param   stack BeanShell call stack.
   * @param   name  <i>name</i> of {@link Stem} to retrieve attribute on.
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
      GrouperSession  s   = GrouperShell.getSession(i);
      return invoke(s, name, attr);
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
    catch (SubjectNotFoundException eSNF)       {
      GrouperShell.error(i, eSNF);
    }
    throw new GrouperShellException(GshErrorMessages.STEM_ATTR_INVALID + attr);
  }
  
  /**
   * Get {@link Stem} attribute value.
   * <p/>
   * @param   grouperSession
   * @param   name  <i>name</i> of {@link Stem} to retrieve attribute on.
   * @param   attr  Name of attribute to retrieve.
   * @return  Value of attribute.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Object invoke(GrouperSession grouperSession, String name, String attr) {
    
    Stem            ns  = StemFinder.findByName(grouperSession, name, true);
    if (attr.equals("createSubject"))    {
      return ns.getCreateSubject();
    }
    else if (attr.equals("createTime"))       {
      return ns.getCreateTime();
    }
    else if (attr.equals("description"))      {
      return ns.getDescription();
    }
    else if (attr.equals("displayExtension")) {
      return ns.getDisplayExtension();
    }
    else if (attr.equals("displayName"))      {
      return ns.getDisplayName();     
    }
    else if (attr.equals("extension"))        {
      return ns.getExtension();
    }
    else if (attr.equals("modifySubject"))    {
      return ns.getModifySubject();
    }
    else if (attr.equals("modifyTime"))       {
      return ns.getModifyTime();
    }
    else if (attr.equals("name"))             {
      return ns.getName();
    }
    
    throw new GrouperShellException(GshErrorMessages.STEM_ATTR_INVALID + attr);
  }

} // public class getStemAttr

