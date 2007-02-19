/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
 * @version $Id: AttributeValidator.java,v 1.10 2007-02-19 17:53:48 blair Exp $
 */
class AttributeValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static boolean internal_isPermittedName(String name) {
    return Validator.internal_isNotNullOrBlank(name);
  } // protected static boolean internal_isPermittedName(name)
  
  // @since   1.1.0
  protected static boolean internal_isPermittedValue(String value) {
    return Validator.internal_isNotNullOrBlank(value);
  } // protected static boolean internal_isPermittedValue(value)
  

} // class AttributeValidator

