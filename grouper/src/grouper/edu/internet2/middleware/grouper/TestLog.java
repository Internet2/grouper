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
import  org.apache.commons.logging.*;

/** 
 * (Some) Grouper test logging.
 * <p/>
 * @author  blair christensen.
 * @version $Id: TestLog.java,v 1.1 2007-03-07 16:57:00 blair Exp $
 * @since   1.2.0
 */
class TestLog {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG;


  // STATIC
  static {
    LOG = LogFactory.getLog(TestLog.class);
  } // static


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void lookInto(Class c, String msg) {
    LOG.info( LogHelper.internal_formatClass(c) + "LOOK INTO: " + msg );
  } // protected static void lookInto(c, msg)

  // @since 1.0
  protected static void stillFailing(Class c, String msg) {
    LOG.info( LogHelper.internal_formatClass(c) + "still failing: " + msg );
  } // protected static void stilLFailing(c, msg)

} // class TestLog

