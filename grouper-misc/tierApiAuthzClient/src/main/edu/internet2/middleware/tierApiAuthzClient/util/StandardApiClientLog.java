/*******************************************************************************
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: StandardApiClientLogtLog.java,v 1.1 2008-12-04 07:51:39 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzClient.util;


import org.apache.commons.logging.Log;

/**
 *
 */
public class StandardApiClientLog implements Log {

  /** debug to console */
  private static ThreadLocal<Boolean> debugToConsole = new ThreadLocal<Boolean>();

  /**
   * if we should debug to console
   * @param theDebugToConsole
   */
  public static void assignDebugToConsole(boolean theDebugToConsole) {
    debugToConsole.set(theDebugToConsole);
  }
  
  /**
   * if we should debug to console
   * @return if debug to console
   */
  public static boolean debugToConsole() {
    Boolean debugToConsoleBoolean = debugToConsole.get();
    return StandardApiClientUtils.defaultIfNull(debugToConsoleBoolean, false);
  }
  
  /**
   * debug to console if needed
   * @param prefix 
   * @param message
   * @param t
   */
  private static void debugToConsoleIfNeeded(String prefix, Object message, Throwable t) {
    if (debugToConsole()) {
      System.err.print(prefix);
      System.err.println(message);
      if (t != null) {
        t.printStackTrace();
      }
    }
  }
  
  /** wrap this log */
  private Log enclosedLog;
  
  /**
   * wrap a logger
   * @param theLog
   */
  public StandardApiClientLog(Log theLog) {
    this.enclosedLog = theLog;
  }
  
  public void debug(Object message) {
    debugToConsoleIfNeeded("DEBUG: ", message, null);
    this.enclosedLog.debug(message);
  }

 
  public void debug(Object message, Throwable t) {
    debugToConsoleIfNeeded("DEBUG: ", message, t);
    this.enclosedLog.debug(message, t);
  }


  public void error(Object message) {
    debugToConsoleIfNeeded("ERROR: ", message, null);
    this.enclosedLog.error(message);
  }


  public void error(Object message, Throwable t) {
    debugToConsoleIfNeeded("ERROR: ", message, t);
    this.enclosedLog.error(message, t);
  }

 
  public void fatal(Object message) {
    debugToConsoleIfNeeded("FATAL: ", message, null);
    this.enclosedLog.fatal(message);
  }

  
  public void fatal(Object message, Throwable t) {
    debugToConsoleIfNeeded("FATAL: ", message, t);
    this.enclosedLog.fatal(message, t);
  }

 
  public void info(Object message) {
    debugToConsoleIfNeeded("INFO: ", message, null);
    this.enclosedLog.info(message);
  }


  public void info(Object message, Throwable t) {
    debugToConsoleIfNeeded("INFO: ", message, t);
    this.enclosedLog.info(message, t);
  }

  
  public boolean isDebugEnabled() {
    return this.enclosedLog.isDebugEnabled() || debugToConsole();
  }


  public boolean isErrorEnabled() {
    return this.enclosedLog.isErrorEnabled();
  }

 
  public boolean isFatalEnabled() {
    return this.enclosedLog.isFatalEnabled();
  }


  public boolean isInfoEnabled() {
    return this.enclosedLog.isInfoEnabled();
  }


  public boolean isTraceEnabled() {
    return this.enclosedLog.isTraceEnabled();
  }

 
  public boolean isWarnEnabled() {
    return this.enclosedLog.isWarnEnabled();
  }


  public void trace(Object message) {
    debugToConsoleIfNeeded("TRACE: ", message, null);
    this.enclosedLog.trace(message);
  }

 
  public void trace(Object message, Throwable t) {
    debugToConsoleIfNeeded("TRACE: ", message, t);
    this.enclosedLog.trace(message, t);
  }


  public void warn(Object message) {
    debugToConsoleIfNeeded("WARN: ", message, null);
    this.enclosedLog.warn(message);
  }

 
  public void warn(Object message, Throwable t) {
    debugToConsoleIfNeeded("WARN: ", message, t);
    this.enclosedLog.warn(message, t);
  }

}
