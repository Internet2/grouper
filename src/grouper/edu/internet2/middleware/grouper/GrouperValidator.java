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
 * @version $Id: GrouperValidator.java,v 1.1 2007-02-19 17:53:48 blair Exp $
 * @since   1.2.0
 */
class GrouperValidator {

  // PRIVATE INSTANCE VARIABLES //
  private String  errorMessage  = GrouperConfig.EMPTY_STRING;
  private boolean isValid       = false;


  // CONSTRUCTORS //

  // @since   1.2.0
  protected GrouperValidator() {
    super();
  } // protected GrouperValidator()


  // GETTERS //

  // @since   1.2.0
  protected String getErrorMessage() {
    return this.errorMessage;
  }
  // @since   1.2.0
  protected boolean getIsValid() {
    return this.isValid;
  }


  // SETTERS //

  // @since   1.2.0
  protected void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  // @since   1.2.0
  protected void setIsValid(boolean isValid) {
    this.isValid = isValid;
  }

} // class GrouperValidator
 
