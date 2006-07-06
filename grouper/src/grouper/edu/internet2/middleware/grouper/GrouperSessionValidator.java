/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * @version $Id: GrouperSessionValidator.java,v 1.6 2006-07-06 16:32:03 blair Exp $
 * @since   1.0
 */
class GrouperSessionValidator {

  // PROTECTED CLASS METHODS //

  // @since   1.0
  protected static void validate(GrouperSession s)
    throws  ModelException
  {
    try {
      Validator.valueNotNull( s                 , E.SV_O );
      Validator.valueNotNull( s.getMember_id()  , E.SV_M );
      Validator.valueNotNull( s.getSession_id() , E.SV_I );
      Validator.valueNotNull( s.getStart_time() , E.SV_T );
    }
    catch (NullPointerException eNP) {
      throw new ModelException(eNP.getMessage(), eNP);
    }
  } // protected static void validate(s)

} // class GrouperSessionValidator

