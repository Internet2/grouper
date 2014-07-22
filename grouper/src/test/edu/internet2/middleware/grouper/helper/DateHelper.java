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

package edu.internet2.middleware.grouper.helper;
import java.util.Date;

/**
* Date-related helper methods for testing the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: DateHelper.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class DateHelper {

  // Private Class Constants
  //private static final Long OFFSET = new Long(1000);
  private static final long OFFSET = 100000;

  // Protected Class Methods

  public static Date getFutureDate() {
    return new Date( new Date().getTime() + OFFSET );
  } // protected static Date getFutureDate()

  public static Date getPastDate() {
    return new Date( new Date().getTime() - OFFSET );
  } // protected static Date getPastDate()

}

