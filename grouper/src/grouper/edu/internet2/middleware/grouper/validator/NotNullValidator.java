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

/** 
 * @author  blair christensen.
 * @version $Id: NotNullValidator.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.0
 */
public class NotNullValidator extends GrouperValidator {

  // PROTECTED CLASS CONSTANTS //
  public static final String INVALID = "null value";


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static NotNullValidator validate(Object value) {
    NotNullValidator v = new NotNullValidator();
    if (value == null) {
      v.setErrorMessage(INVALID);
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } // protected static NotNullValidator validate(value)

} // class NotNullValidator extends GrouperValidator

