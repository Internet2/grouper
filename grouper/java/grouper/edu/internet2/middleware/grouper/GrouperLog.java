/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * Grouper API logging.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperLog.java,v 1.3 2005-12-09 07:35:38 blair Exp $
 *     
*/
class GrouperLog implements Serializable {

  // Protected Class Methods

  protected static void debug(Log log, GrouperSession s, String msg) {
    log.debug( _formatMsg(s, msg) );
  } // protected static void debug(log, s, msg)

  protected static void error(Log log, GrouperSession s, String msg) {
    log.error( _formatMsg(s, msg) );
  } // protected static void error(log, s, msg)

  protected static void warn(Log log, GrouperSession s, String msg) {
    log.warn( _formatMsg(s, msg) );
  } // protected static void warn(log, s, msg)


  // Private Class Methods
  private static String _formatMsg(GrouperSession s, String msg) {
    return "[" + s.toString() + "] " + msg;
  } // private static String _formatMsg(s, msg)

} // protected class GrouperLog

