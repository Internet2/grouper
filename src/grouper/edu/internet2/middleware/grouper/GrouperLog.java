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
import  java.util.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;

/** 
 * Grouper API logging.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperLog.java,v 1.13 2006-06-05 19:54:40 blair Exp $
 */
class GrouperLog {

  // FIXME DEPRECATE!

  // PROTECTED CLASS CONSTANTS //
  // TODO Move to _ErrorLog_
  protected static final String ERR_GRS   = "unable to start root session: ";


  // PROTECTED CLASS METHODS //
  protected static void info(Log log, GrouperSession s, String msg) {
    log.info( LogHelper.formatMsg(s, msg) );
  } // protected static void info(log, s, msg)

  // For effective membership event logging
  protected static void info(
    Log log, String sessionToString, String msg
  ) 
  {
    log.info("[" + sessionToString + "] " + msg);
  } // protected static void info(log, sessionToString, msg, sw)

  // For all other event logging
  protected static void info(
    Log log, String sessionToString, String msg, StopWatch sw
  ) 
  {
    log.info("[" + sessionToString + "] " + msg + " (" + sw.getTime() + "ms)");
  } // protected static void info(log, sessionToString, msg, sw)
} // protected class GrouperLog

