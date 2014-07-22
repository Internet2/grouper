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
import  java.util.regex.Pattern;

import edu.internet2.middleware.grouper.misc.E;

/** 
 * @author  blair christensen.
 * @version $Id: NamingValidator.java,v 1.1 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.2.0
 */
public class NamingValidator extends GrouperValidator {

  // PROTECTED CLASS CONSTANTS //
  public static final String E_WS = "must contain a non-whitespace character";
  
  
  // PRIVATE CLASS CONSTANTS //
  private static final Pattern  RE_COLON  = Pattern.compile(":");
  private static final Pattern  RE_WS     = Pattern.compile("^\\s*$");


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  public static NamingValidator validate(String name) {
    NamingValidator nv = new NamingValidator();
    if      (name == null)                    {
      nv.setErrorMessage(E.ATTR_NULL);
    }
    else if ( RE_COLON.matcher(name).find() ) {
      nv.setErrorMessage(E.ATTR_COLON);
    }
    else if ( RE_WS.matcher(name).find() )    {
      nv.setErrorMessage(E_WS);
    }
    else {
      nv.setIsValid(true);
    }
    return nv;
  } // protected static NamingValidator validate(name)

} // class NamingValidator extends GrouperValidator

