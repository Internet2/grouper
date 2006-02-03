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

package test.edu.internet2.middleware.grouper;

import  java.util.*;
import  junit.framework.*;

/**
* Date-related helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: DateHelper.java,v 1.3 2006-02-03 19:38:53 blair Exp $
 */
public class DateHelper {

  // Private Class Constants
  //private static final Long OFFSET = new Long(1000);
  private static final long OFFSET = 100000;

  // Protected Class Methods

  protected static Date getFutureDate() {
    return new Date( new Date().getTime() + OFFSET );
  } // protected static Date getFutureDate()

  protected static Date getPastDate() {
    return new Date( new Date().getTime() - OFFSET );
  } // protected static Date getPastDate()

}

