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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.misc.E;

/** 
 * @author  blair christensen.
 * @version $Id: DeleteFieldFromGroupTypeValidator.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.0
 */
public class DeleteFieldFromGroupTypeValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static DeleteFieldFromGroupTypeValidator validate(GroupType type, Field f) {
    DeleteFieldFromGroupTypeValidator v = new DeleteFieldFromGroupTypeValidator();
    if ( !f.getGroupType().equals(type) ) {
      v.setErrorMessage( E.FIELD_DOES_NOT_BELONG_TO_TYPE + f.getGroupType() );
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } // protected static DeleteFieldFromGroupTypeValidator validate(type, f)

} // class DeleteFieldFromGroupTypeValidator extends GrouperValidator

