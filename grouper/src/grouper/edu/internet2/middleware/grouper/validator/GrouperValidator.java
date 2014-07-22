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

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

/** 
 * @author  blair christensen.
 * @version $Id: GrouperValidator.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.0
 */
public class GrouperValidator {

  // PRIVATE INSTANCE VARIABLES //
  private String  errorMessage  = GrouperConfig.EMPTY_STRING;
  private boolean isValid       = false;


  // CONSTRUCTORS //

  // @since   1.2.0
  public GrouperValidator() {
    super();
  } // public GrouperValidator()


  // public INSTANCE METHODS //

  // @since   1.2.0
  public boolean isInvalid() {
    return !this.getIsValid();
  } // public boolean isInvalid()

  // @since   1.2.0
  public boolean isValid() {
    return this.getIsValid();
  } // public boolean isValid()


  // GETTERS //

  // @since   1.2.0
  public String getErrorMessage() {
    return this.errorMessage;
  }
  // @since   1.2.0
  public boolean getIsValid() {
    return this.isValid;
  }


  // SETTERS //

  // @since   1.2.0
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  // @since   1.2.0
  public void setIsValid(boolean isValid) {
    this.isValid = isValid;
  }

} // class GrouperValidator
 
