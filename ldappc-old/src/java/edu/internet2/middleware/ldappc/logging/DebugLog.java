/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
  
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

package edu.internet2.middleware.ldappc.logging;

import org.apache.commons.logging.*;

/** 
 * Ldappc API debug logging.
 * Contains methods for displaying a log message prefixed by the class name in brackets.
 * The logging levels allowed are info and debug.
 
 * 
 * @author Gil Singer.
 */
public class DebugLog {

    /**
     * The Log file created by the log factory for this class.
     */  
     private static final Log LOG;

    /**
     * Create the Log file created by the log factory for this class.
     */
    static { LOG = LogFactory.getLog(DebugLog.class); } 

    /**
     * Logs info level messages.
     * @param c The calling class 
     * @param msg The message to be logged.
     */
    public static void info(Class c, String msg) 
    {
        LOG.info("[" + c.getName() + "] " + msg);
    } 

    /**
     * Logs info level messages.
     * @param msg The message to be logged.
     */
    public static void info(String msg) {
        LOG.info(msg);
    } 

    /**
     * Logs debug level messages.
     * @param c The calling class 
     * @param msg The message to be logged.
     */
    public static void debug(Class c, String msg) 
    {
        LOG.debug("[" + c.getName() + "] " + msg);
    } 

    /**
     * Logs debug level messages.
     * @param msg The message to be logged.
     */
    public static void debug(String msg) {
        LOG.debug(msg);
    } 

}

