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
import  java.io.Serializable;

/** 
 * @author  blair christensen.
 * @version $Id: GrouperSessionValidator.java,v 1.4 2006-06-15 00:07:02 blair Exp $
 * @since   1.0
 */
class GrouperSessionValidator implements Serializable {

  // PROTECTED CLASS CONSTANTS //
  // TODO Move to *E*
  protected static final String ERR_I = "null session id";
  protected static final String ERR_O = "null session object";
  protected static final String ERR_M = "null session member";
  protected static final String ERR_T = "null session start time";


  // PROTECTED CLASS METHODS //
  protected static void validate(GrouperSession s)
    throws  ModelException
  {
    try {
      Validator.valueNotNull( s                 , ERR_O );
      Validator.valueNotNull( s.getMember_id()  , ERR_M );
      Validator.valueNotNull( s.getSession_id() , ERR_I );
      Validator.valueNotNull( s.getStart_time() , ERR_T );
    }
    catch (NullPointerException eNP) {
      throw new ModelException(eNP.getMessage(), eNP);
    }
  } // protected static void validate(s)

}

