/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.logging;

/**
 * Ldappc API debug logging.
 * 
 * @author Gil Singer
 */
public final class ExceptionHandler {

  /**
   * Exception level for warning messages.
   */
  public static final String WARN = "warn";

  /**
   * Exception level for error messages.
   */
  public static final String ERROR = "error";

  /**
   * Exception level for fatal messages.
   */
  public static final String FATAL = "fatal";

  /**
   * Prevent instantiation.
   */
  private ExceptionHandler() {
  }

  /**
   * Show the entire chain of exception messages and, optionally, the chain for
   * StackTraces. This is for the error log only, not the debug log.
   * 
   * @param throwable
   *          the throwable from which the message and stack info is to be extracted
   * @param level
   *          The level of the exception (e.g. warn, error, or fatal.
   * @param doStackTrace
   *          flag if true indicating the stack traces are to be processed
   */
  public static void logExceptionMessages(Throwable throwable, String level,
      boolean doStackTrace) {
    Throwable thrown = throwable;
    do {
      System.out.println(thrown.getMessage());

      /*
       * if (WARN.equals(level)) { ErrorLog.warn(thrown.getMessage()); } if
       * (ERROR.equals(level)) { ErrorLog.info(thrown.getMessage()); } if
       * (FATAL.equals(level)) { ErrorLog.fatal(thrown.getMessage()); } else { // Also do
       * fatal if level is not recogized because this is // programming error.
       * ErrorLog.fatal(thrown.getMessage()); }
       */

      thrown = thrown.getCause();

      if (doStackTrace) {
        thrown.printStackTrace();
      }
    } while (thrown != null);
  }

}
