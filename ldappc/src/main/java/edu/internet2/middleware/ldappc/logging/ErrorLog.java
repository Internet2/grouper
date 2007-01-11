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

package edu.internet2.middleware.ldappc.logging;

import  org.apache.commons.logging.*;

/** 
 * Ldappc API debug logging.
 *
 * @author Gil Singer
 */
public class ErrorLog {

    /**
     * The Log file created by the log factory for this class.
     * Contains methods for displaying a log message prefixed by the class name in brackets.
     * The logging levels allowed are fatal, error, and warn.
     */  
    private static final Log LOG;
    
    /**
     * flag indicating that one or more fatal errors occurred during execution
     */
    private static boolean fatalOccurred = false;

    /**
     * Setter for fatalOccurred.
     * @param fatalOccurred flag indicating that one or more fatal errors occurred during execution.
     */

    public static void setFatalOccurred(boolean fatalOccurred)
    {
        setFatalOccurred(fatalOccurred);
    }

    /**
     * Getter for fatalOccurred.
     * @return flag indicating that one or more fatal errors occurred during execution.
     * 
     */

    public static boolean getFatalOccurred()
    {
       return fatalOccurred;
    }

    /**
     * Create the Log file created by the log factory for this class.
     */
    static { LOG = LogFactory.getLog(ErrorLog.class); }

    /**
     * Logs warning level messages.
     * @param c The calling class 
     * @param msg The message to be logged.
     */
    public static void warn(Class c, String msg) 
    {
        LOG.warn("[" + c.getName() + "] " + msg);
    } 

    /**
     * Logs error level messages.
     * @param c The calling class 
     * @param msg The message to be logged.
     */
    public static void error(Class c, String msg) 
    {
        LOG.error("[" + c.getName() + "] " + msg);
    } 

    /**
     * Logs fatal level messages.
     * @param c The calling class 
     * @param msg The message to be logged.
     */
    public static void fatal(Class c, String msg) 
    {
        fatalOccurred = true;
        LOG.fatal("[" + c.getName() + "] " + msg);
    } 

}

