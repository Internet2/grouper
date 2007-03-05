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
 * @version $Id: FieldTypeValidator.java,v 1.1 2007-03-05 20:04:17 blair Exp $
 * @since   1.2.0
 */
class FieldTypeValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // TODO 20070305 this is *really* misnamed.  this is really for field types acceptable
  //               for use with groups.  which, well, yeah, is most of them.
  protected static FieldTypeValidator validate(Field f) {
    FieldTypeValidator v = new FieldTypeValidator();
    if (
      !
      (
            f.getType().equals( FieldType.ACCESS    )
        ||  f.getType().equals( FieldType.ATTRIBUTE )
        ||  f.getType().equals( FieldType.LIST      )
      )
    )
    {
      v.setErrorMessage( E.FIELD_INVALID_TYPE + f.getType() );
    }
    else {
      v.setIsValid(true);
    }
    return v;
  } // protected static FieldTypeValidator validate(f)

} // class FieldTypeValidator extends GrouperValidator

