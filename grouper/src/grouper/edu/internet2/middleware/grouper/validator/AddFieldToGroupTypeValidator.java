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

import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/** 
 * @author  blair christensen.
 * @version $Id: AddFieldToGroupTypeValidator.java,v 1.2 2008-07-27 07:37:24 mchyzer Exp $
 * @since   1.2.0
 */
public class AddFieldToGroupTypeValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static AddFieldToGroupTypeValidator validate(String name, boolean allowExist) {
    AddFieldToGroupTypeValidator v = new AddFieldToGroupTypeValidator();
    if (!allowExist && GrouperDAOFactory.getFactory().getField().existsByName(name) ) {
      v.setErrorMessage(E.FIELD_ALREADY_EXISTS + name);
    }
    else {
      v.setIsValid(true); // the field doesn't exist
    }
    return v;
  } // protected static AddFieldToGroupTypeValidator validate(name)

} // class AddFieldToGroupTypeValidator extends GrouperValidator

