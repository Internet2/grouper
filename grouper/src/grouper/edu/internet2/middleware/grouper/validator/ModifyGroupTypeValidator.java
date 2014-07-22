/**
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

import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

/** 
 * @author  blair christensen.
 * @version $Id: ModifyGroupTypeValidator.java,v 1.2 2009-01-27 12:09:24 mchyzer Exp $
 * @since   1.2.0
 */
public class ModifyGroupTypeValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static ModifyGroupTypeValidator validate(GrouperSession s, GroupType type) {
    //note, no need for GrouperSession inverse of control
    ModifyGroupTypeValidator v = new ModifyGroupTypeValidator();
    if      ( !PrivilegeHelper.isRoot(s) )  {
      v.setErrorMessage(E.GROUPTYPE_CANNOT_MODIFY_TYPE + ": " + (type == null ? null : type.getName()));
    }
    else if ( type.isSystemType() )     {
      v.setErrorMessage(E.GROUPTYPE_CANNOT_MODIFY_SYSTEM_TYPES + ": " + (type == null ? null : type.getName()));
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } 

} 

