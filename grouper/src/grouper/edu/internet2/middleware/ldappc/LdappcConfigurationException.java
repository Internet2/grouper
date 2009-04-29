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

/**
 * This indicates that a configuration error occurred.
 */
public class LdappcConfigurationException extends LdappcRuntimeException {

  /**
   * Need this because class implements Serializable.
   */
  public static final long serialVersionUID = 1L;

  /**
   * Constructs a new configuration exception with null as its detail message. The cause
   * is not initialized, and may subsequently be initialized by a call to
   * {@link java.lang.Throwable#initCause(java.lang.Throwable)}.
   */
  public LdappcConfigurationException() {
    super();
  }

  /**
   * Constructs a new configuration exception with the specified detail message. The cause
   * is not initialized, and may subsequently be initialized by a call to
   * {@link java.lang.Throwable#initCause(java.lang.Throwable)}.
   * 
   * @param message
   *          the detail message. The detail message is saved for later retrieval by the
   *          {@link java.lang.Throwable#getMessage()} method.
   */
  public LdappcConfigurationException(String message) {
    super(message);
  }

  /**
   * Constructs a new configuration exception with the specified detail message and cause.
   * Note that the detail message associated with cause is not automatically incorporated
   * in this configuration exception's detail message.
   * 
   * @param message
   *          the detail message (which is saved for later retrieval by the
   *          {@link java.lang.Throwable#getMessage()} method).
   * @param cause
   *          the cause (which is saved for later retrieval by the
   *          {@link java.lang.Throwable#getCause()} method). (A null value is permitted,
   *          and indicates that the cause is nonexistent or unknown.)
   */
  public LdappcConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new configuration exception with the specified cause. The detail message
   * is determined by <code>(cause==null ? null : cause.toString())</code> (which
   * typically contains the class and detail message of cause). This constructor is useful
   * for configuration exceptions that are little more than wrappers for other throwables.
   * 
   * @param cause
   *          the cause (which is saved for later retrieval by the
   *          {@link java.lang.Throwable#getCause()} method). (A null value is permitted,
   *          and indicates that the cause is nonexistent or unknown.) Constructs a new
   *          configuration exception with the specified cause and a detail message of
   *          (cause==null ? null : cause.toString()) (which typically contains the class
   *          and detail message of cause).
   */
  public LdappcConfigurationException(Throwable cause) {
    super(cause);
  }

}