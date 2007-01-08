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
 * @version $Id: GrouperSessionValidator.java,v 1.9 2007-01-08 16:43:56 blair Exp $
 * @since   1.0
 */
class GrouperSessionValidator {

  // PROTECTED CLASS METHODS //

  // @throws  GrouperRuntimeException
  // @since   1.2.0
  protected static void internal_validate(GrouperSession s)
    throws  GrouperRuntimeException
  {
    try {
      Validator.internal_valueNotNull( s                 , E.SV_O );
      Validator.internal_valueNotNull( s.getMember_id()  , E.SV_M );
      Validator.internal_valueNotNull( s.getSession_id() , E.SV_I );
      Validator.internal_valueNotNull( s.getStart_time() , E.SV_T );
    }
    catch (NullPointerException eNP) {
      throw new GrouperRuntimeException(eNP.getMessage(), eNP);
    }
  } // protected static void internal_validate(s)

} // class GrouperSessionValidator

