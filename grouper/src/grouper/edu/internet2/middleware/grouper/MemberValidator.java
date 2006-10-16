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
 * @version $Id: MemberValidator.java,v 1.1 2006-10-16 14:11:42 blair Exp $
 * @since   1.1.0
 */
class MemberValidator {

  // PROTECTED CLASS METHODS //

  // @since 1.1.10
  protected static void canSetSubjectId(Member m, String val) 
    throws  InsufficientPrivilegeException 
  {
    Validator.argNotNull(val, "null subjectId");
    // Subjects from ISA may not be updated
    if (m.getSubjectSourceId().equals(InternalSourceAdapter.ID)) {
      throw new InsufficientPrivilegeException("cannot modify internal subjects");
    }
    if (! RootPrivilegeResolver.isRoot( m.getSession()) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_SET_SUBJECTID);
    }
  } // protected static void canSetSubjectId(m)

} // class MemberValidator

