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

/** 
 * @author  blair christensen.
 * @version $Id: ModifyGroupTypeValidator.java,v 1.3 2007-08-24 14:18:15 blair Exp $
 * @since   1.2.0
 */
class ModifyGroupTypeValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static ModifyGroupTypeValidator validate(GrouperSession s, GroupType type) {
    ModifyGroupTypeValidator v = new ModifyGroupTypeValidator();
    if      ( !PrivilegeHelper.isRoot(s) )  {
      v.setErrorMessage(E.GROUPTYPE_CANNOT_MODIFY_TYPE);
    }
    else if ( type.isSystemType() )     {
      v.setErrorMessage(E.GROUPTYPE_CANNOT_MODIFY_SYSTEM_TYPES);
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } 

} 

