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

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.regex.*;
import  org.apache.commons.logging.*;


/** 
 * @author  blair christensen.
 * @version $Id: AttributeValidator.java,v 1.1.2.1 2006-04-19 22:55:14 blair Exp $
 */
class AttributeValidator implements Serializable {

  // Protected Class Constants //
  protected static final String ERR_AV  = "empty attribute value";
  protected static final String ERR_VCC = "value contains colon";

  // Private Class Constants //
  private static final Log      LOG       = LogFactory.getLog(AttributeValidator.class);
  private static final Pattern  RE_COLON  = Pattern.compile(":");
  private static final Pattern  RE_WS     = Pattern.compile("^\\s*$");

  // Protected Class Methods //
  protected static void namingValue(String value)
    throws  ModelException
  {
    if (value == null) {
      throw new ModelException(ERR_AV);
    }
    Matcher m = RE_COLON.matcher(value);
    if (m.find()) {
      throw new ModelException(ERR_VCC);
    }
    m = RE_WS.matcher(value);
    if (m.find()) {
      throw new ModelException(ERR_AV);
    }
  } // protected static void namingValue(value)
    
}

