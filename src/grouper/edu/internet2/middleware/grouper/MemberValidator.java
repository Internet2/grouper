/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;

/** 
 * @author  blair christensen.
 * @version $Id: MemberValidator.java,v 1.2 2006-10-16 14:39:00 blair Exp $
 * @since   1.1.0
 */
class MemberValidator {

  // PROTECTED CLASS METHODS //

  // @since 1.1.10
  protected static void canSetSubjectId(Member m, String val) 
    throws  InsufficientPrivilegeException 
  {
    _canSetSubjectAttr(m, val, "null subject id");
  } // protected static void canSetSubjectId(m)

  // @since 1.1.10
  protected static void canSetSubjectSourceId(Member m, String val) 
    throws  InsufficientPrivilegeException 
  {
    _canSetSubjectAttr(m, val, "null subject source id");
  } // protected static void canSetSubjectId(m)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static void _canSetSubjectAttr(Member m, String val, String msg)
    throws  InsufficientPrivilegeException
  {
    Validator.argNotNull(val, msg);
    // Subjects from ISA may not be updated
    if (m.getSubjectSourceId().equals(InternalSourceAdapter.ID)) {
      throw new InsufficientPrivilegeException("cannot modify internal subjects");
    }
    if (! RootPrivilegeResolver.isRoot( m.getSession()) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_SET_SUBJECTID);
    }
  } // protected static void _canSetSubjectAttr(m)

} // class MemberValidator

