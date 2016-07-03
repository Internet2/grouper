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
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.RegistrySubjectAttribute;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;

/**
 * Add {@link RegistrySubject} to Groups Registry.
 * 
 * @author  blair christensen.
 * @version $Id: addSubject.java,v 1.3 2009-02-13 13:51:58 mchyzer Exp $
 * @since   0.0.1
 */
public class addSubject {

  // PUBLIC CLASS METHODS //

  /**
   * Add {@link RegistrySubject} to Groups Registry.
   * 
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @param   name        Subject <i>name</i>.
   * @return  Added {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static RegistrySubject invoke(
    Interpreter i, CallStack stack, String id, String type, String name
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return RegistrySubject.add( GrouperShell.getSession(i), id, type, name );
    }
    catch (GrouperException eG)                 { 
      GrouperShell.error(i, eG);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    return null;
  } // public static RegistrySubject invoke(i, stack, parent, name)

  /**
   * Add {@link RegistrySubject} to Groups Registry.
   * 
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @param   name        Subject <i>name</i>.
   * @param description subject description
   * @return  Added {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static RegistrySubject invoke(
      Interpreter i, CallStack stack, String id, String type, String name, String description) 
      throws  GrouperShellException {
    GrouperShell.setOurCommand(i, true);
    try {
      RegistrySubject registrySubject = RegistrySubject.add( GrouperShell.getSession(i), id, type, name );
      RegistrySubjectAttribute registrySubjectAttribute = new RegistrySubjectAttribute();
      registrySubjectAttribute.setName("description");
      registrySubjectAttribute.setSearchValue(description.toLowerCase());
      registrySubjectAttribute.setSubjectId(id);
      registrySubjectAttribute.setValue(description);
      HibernateSession.byObjectStatic().saveOrUpdate(registrySubjectAttribute);
      return registrySubject;
    }
    catch (GrouperException eG)                 { 
      GrouperShell.error(i, eG);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    return null;
  } 

} // public class addSubject

