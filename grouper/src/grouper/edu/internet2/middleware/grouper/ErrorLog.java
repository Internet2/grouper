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
import  org.apache.commons.logging.*;

/** 
 * Grouper API error logging.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ErrorLog.java,v 1.4 2006-08-22 19:48:22 blair Exp $
 * @since   1.0
 */
class ErrorLog {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG;


  // STATIC
  static {
    LOG = LogFactory.getLog(ErrorLog.class);
  } // static


  // PROTECTED CLASS METHODS //
  // @since 1.0
  protected static void error(Class c, String msg) {
    LOG.error(LogHelper.formatClass(c) + msg);
  } // protected static void error(c, msg)

  // @since 1.0
  protected static void fatal(Class c, String msg) {
    LOG.fatal(LogHelper.formatClass(c) + msg);
  } // protected static void fatal(c, msg)

}

