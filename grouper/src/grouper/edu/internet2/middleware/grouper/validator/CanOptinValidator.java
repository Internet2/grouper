/*******************************************************************************
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
 ******************************************************************************/
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

package edu.internet2.middleware.grouper.validator;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import  edu.internet2.middleware.subject.Subject;

/** 
 * @author  blair christensen.
 * @version $Id: CanOptinValidator.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.0
 */
public class CanOptinValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static CanOptinValidator validate(Group g, Subject subj, Field f) {
    CanOptinValidator v = new CanOptinValidator();
    if      (
      !
      (
        SubjectHelper.eq( GrouperSession.staticGrouperSession().getSubject(), subj ) && Group.getDefaultList().equals(f)
      )
    )
    {
      v.setErrorMessage(E.GROUP_COI);
    }
    else if ( !PrivilegeHelper.canOptin(GrouperSession.staticGrouperSession(), g, subj) ) {
      v.setErrorMessage(E.CANNOT_OPTIN);
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } 

} 

