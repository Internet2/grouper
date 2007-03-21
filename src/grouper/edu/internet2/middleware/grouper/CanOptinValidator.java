/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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
import  edu.internet2.middleware.subject.Subject;

/** 
 * @author  blair christensen.
 * @version $Id: CanOptinValidator.java,v 1.2 2007-03-21 18:02:28 blair Exp $
 * @since   1.2.0
 */
class CanOptinValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static CanOptinValidator validate(Group g, Subject subj, Field f) {
    CanOptinValidator v = new CanOptinValidator();
    if      (
      !
      (
        SubjectHelper.eq( g.getSession().getSubject(), subj ) && Group.getDefaultList().equals(f)
      )
    )
    {
      v.setErrorMessage(E.GROUP_COI);
    }
    else if ( !PrivilegeResolver.internal_canOPTIN(g.getSession(), g, subj) ) {
      v.setErrorMessage(E.CANNOT_OPTIN);
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } // protected static CanOptinValidator validate(g, subj, f)

} // class CanOptinValidator extends GrouperValidator

