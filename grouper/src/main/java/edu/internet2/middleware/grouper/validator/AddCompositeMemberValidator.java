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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/** 
 * @author  blair christensen.
 * @version $Id: AddCompositeMemberValidator.java,v 1.3 2009-08-18 23:11:39 shilen Exp $
 * @since   1.2.0
 */
public class AddCompositeMemberValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static AddCompositeMemberValidator validate(Group g) {
    AddCompositeMemberValidator v = new AddCompositeMemberValidator();
    if      ( g.hasComposite() )  {
      v.setErrorMessage(E.GROUP_ACTC);
    }
    else if ( 
      GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField(
        g.getUuid(), Group.getDefaultList(), false
      ).size() > 0 
    )
    {
      v.setErrorMessage(E.GROUP_ACTM);
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } // protected static AddCompositeMemberValidator validate(parent, extn, dExtn)

} // class AddCompositeMemberValidator extends GrouperValidator

