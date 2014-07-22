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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import  edu.internet2.middleware.grouper.internal.util.U;

/** 
 * @author  blair christensen.
 * @version $Id: AddStemValidator.java,v 1.4 2009-11-17 05:33:43 mchyzer Exp $
 * @since   1.2.0
 */
public class AddStemValidator extends GrouperValidator {

  // PROTECTED CLASS METHODS //

  /**
   * 
   */
  public static final String STEM_ALREADY_EXISTS_ERROR_MESSAGE = "stem already exists";

  // @since   1.2.0
  public static AddStemValidator validate(Stem parent, String extn, String dExtn) {
    AddStemValidator  v   = new AddStemValidator();
    NamingValidator   nv  = NamingValidator.validate(extn);
    if (nv.isInvalid()) {
      v.setErrorMessage( nv.getErrorMessage() );
      return v;
    }
    nv = NamingValidator.validate(dExtn);
    if (nv.isInvalid()) {
      v.setErrorMessage( nv.getErrorMessage() );
      return v;
    }
    try {
      // TODO 20070531 what do i need to do to just make a direct DAO call?
      String stemName = U.constructName( parent.getName(), extn);
      StemFinder.internal_findByName( stemName, true);
      v.setErrorMessage(STEM_ALREADY_EXISTS_ERROR_MESSAGE + ": " + stemName);
    } catch (StemNotFoundException eSNF) {
      v.setIsValid(true); // stem does not exist, which is what we want
    }
    return v;
  } // protected static AddStemValidator validate(parent, extn, dExtn)

} // class AddStemValidator extends GrouperValidator

