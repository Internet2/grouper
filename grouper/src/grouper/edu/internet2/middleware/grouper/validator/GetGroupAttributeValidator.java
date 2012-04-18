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
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.E;

/** 
 * @author  blair christensen.
 * @version $Id: GetGroupAttributeValidator.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.0
 */
public class GetGroupAttributeValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static GetGroupAttributeValidator validate(Group g, Field f) {
    GetGroupAttributeValidator  v     = new GetGroupAttributeValidator();
    NotNullValidator            vNull = NotNullValidator.validate(f);
    try {
      if      (vNull.isInvalid()) {
        v.setErrorMessage( vNull.getErrorMessage() );
      }
      else if ( !g.hasType( f.getGroupType() ) ) {
        v.setErrorMessage( E.GROUP_DOES_NOT_HAVE_TYPE + f.getGroupType() );
      }
      else if ( !g.canReadField(f) ) {
        v.setErrorMessage("attribute not found");
      }
      else {
        v.setIsValid(true);
      }
    }
    catch (SchemaException eS) {
      v.setErrorMessage( eS.getMessage() );
    }
    return v;
  } // protected static GetGroupAttributeValidator validate(g, f)

} // class GetGroupAttributeValidator extends GrouperValidator

