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

package edu.internet2.middleware.ldappc;

import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * This abstract parent class for provisioners.
 */
public abstract class Provisioner {

  /**
   * Delimiter used in messages.
   */
  private static final String MSG_DELIMITER = " :: ";

  /**
   * Cache for subjects retrieved from the subject sources.
   */
  private SubjectCache subjectCache;

  /**
   * Constructor.
   * 
   * @param subjectCache
   *          the subject cache.
   */
  public Provisioner(SubjectCache subjectCache) {
    this.subjectCache = subjectCache;
  }

  /**
   * Gets the subject cache.
   * 
   * @return the subject cache
   */
  public SubjectCache getSubjectCache() {
    return subjectCache;
  }

  /**
   * Utility method for logging a <code>Throwable</code> error.
   * 
   * @param throwable
   *          Throwable to log
   */
  protected void logThrowableError(Throwable throwable) {
    logThrowableError(throwable, null);
  }

  /**
   * Utility method for logging a <code>Throwable</code> error.
   * 
   * @param throwable
   *          Throwable to log
   * @param errorData
   *          Additional data for helping to debug
   */
  protected void logThrowableError(Throwable throwable, String errorData) {
    ErrorLog.error(getClass(), buildThrowableMsg(throwable, errorData));
  }

  /**
   * Utility method for logging a <code>Throwable</code> warning.
   * 
   * @param throwable
   *          Throwable to log
   */
  protected void logThrowableWarning(Throwable throwable) {
    logThrowableWarning(throwable, null);
  }

  /**
   * Utility method for logging a <code>Throwable</code> warning.
   * 
   * @param throwable
   *          Throwable to log
   * @param errorData
   *          Additional data for helping to debug
   */
  protected void logThrowableWarning(Throwable throwable, String errorData) {
    ErrorLog.warn(getClass(), buildThrowableMsg(throwable, errorData));
  }

  /**
   * Builds the message for logging throwables.
   * 
   * @param throwable
   *          Throwable to log
   * @param errorData
   *          Additional data for helping to debug
   * @return message string
   */
  protected String buildThrowableMsg(Throwable throwable, String errorData) {
    return throwable.getClass().getName() + MSG_DELIMITER + throwable.getMessage()
        + (errorData == null ? "" : MSG_DELIMITER + errorData);
  }
}
