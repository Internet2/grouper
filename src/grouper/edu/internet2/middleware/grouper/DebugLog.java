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
 * Grouper API debug logging.
 * <p/>
 * @author  blair christensen.
 * @version $Id: DebugLog.java,v 1.5 2007-04-19 16:28:49 blair Exp $
 * @since   1.0
 */
public class DebugLog {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG;


  // STATIC
  static {
    LOG = LogFactory.getLog(DebugLog.class);
  }


  // PUBLIC CLASS METHODS //
  
  /**
   * Send <code>info</code> message to <code>DebugLog</code>.
   * @since   1.2.0
   */
  public static void info(Class c, String msg) {
    LOG.info(msg);
  } 

}

