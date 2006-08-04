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

package edu.internet2.middleware.grouper.eg;
import  org.apache.commons.logging.*;

/**
 * Logger for Grouper example code.
 * <p/>
 * @author  blair christensen.
 * @version $Id: EgLog.java,v 1.1 2006-08-04 19:02:11 blair Exp $
 * @since   1.0.1
 */
class EgLog {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(EgLog.class);


  // PROTECTED CLASS METHODS //

  // @since 1.0.1
  protected static void error(Class c, String msg) {
    LOG.error("[" + c.getName() + "] " + msg);
  } // protected static void error(msg)

  // @since 1.0.1
  protected static void info(Class c, String msg) {
    LOG.info("[" + c.getName() + "] " + msg);
  } // protected static void info(msg)

} // class EgLog

