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
import  org.apache.commons.logging.*;


/** 
 * @author  blair christensen.
 * @version $Id: GrouperSessionValidator.java,v 1.1.2.1 2006-04-17 18:19:54 blair Exp $
 *     
*/
class GrouperSessionValidator implements Serializable {

  // Protected Class Constants //
  protected static final String ERR_I = "null session id";
  protected static final String ERR_O = "null session object";
  protected static final String ERR_M = "null session member";
  protected static final String ERR_T = "null session start time";

  // Private Class Constants //
  private static final Log LOG = LogFactory.getLog(GrouperSessionValidator.class);


  // Protected Class Methods //
  protected static void validate(GrouperSession s)
    throws  ModelException
  {
    if (s == null) {
      throw new ModelException(ERR_O);
    }
    if (s.getMember()     == null) {
      throw new ModelException(ERR_M);
    }
    if (s.getSessionId()  == null) {
      throw new ModelException(ERR_I);
    }
    if (s.getStartTime()  == null) {
      throw new ModelException(ERR_T);
    }
  } // protected static void validate(s)

}

