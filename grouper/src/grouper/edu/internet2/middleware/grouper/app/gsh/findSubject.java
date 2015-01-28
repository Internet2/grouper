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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Find a {@link Subject}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: findSubject.java,v 1.4 2009-11-02 03:50:51 mchyzer Exp $
 * @since   0.0.1
 */
public class findSubject {

  // PUBLIC CLASS METHODS //

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @return  Found {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findByIdOrIdentifier(id, true);
    }
    catch (SubjectNotFoundException eSNF) {
      GrouperShell.error(i, eSNF);
    }
    catch (SubjectNotUniqueException eSNU) {
      GrouperShell.error(i, eSNU);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name)

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @return  Found {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id, String type
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findById(id, true);
    }
    catch (SubjectNotFoundException eSNF) {
      try {
        return SubjectFinder.findByIdentifier(id, true);
      } catch (SubjectNotFoundException snfe) {
        GrouperShell.error(i, eSNF);
      }
    }
    catch (SubjectNotUniqueException eSNU) {
      GrouperShell.error(i, eSNU);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name, type)

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @param   source      Subject <i>source</i>.
   * @return  Found {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id, String type, String source
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findByIdAndSource(id, source, true);
    }
    catch (SourceUnavailableException eSNA) {
      GrouperShell.error(i, eSNA);
    }
    catch (SubjectNotFoundException eSNF) {
      try {
        return SubjectFinder.findByIdentifierAndSource(id, source, true);
      } catch (SubjectNotFoundException snfe) {
        GrouperShell.error(i, eSNF);
      }
    }
    catch (SubjectNotUniqueException eSNU) {
      GrouperShell.error(i, eSNU);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name, type, source)

} // public class findSubject

