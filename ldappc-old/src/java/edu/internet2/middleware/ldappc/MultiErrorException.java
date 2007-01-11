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

package edu.internet2.middleware.ldappc;

/**
 * This Ldappc exception is thrown when multiple errors have occured. This
 * exception allows multiple exceptions to be collected then thrown as part of a
 * single exception. It is commonly used to maximize the amount of processing to
 * be completed prior to throwing an exception.
 */
public class MultiErrorException extends LdappcException
{
    public static final long serialVersionUID = 1;

    /**
     * Array of errors
     */
    private Exception[] errors;

    /**
     * Constructs a new multiple error exception with null as its detail
     * message. The cause is not initialized, and may subsequently be
     * initialized by a call to
     * {@link java.lang.Throwable#initCause(java.lang.Throwable)}.
     * 
     * @param errors
     *            Array of Exceptions
     */
    public MultiErrorException(Exception[] errors)
    {
        super();
        setErrors(errors);
    }

    /**
     * Constructs a new multiple error exception with the specified detail
     * message. The cause is not initialized, and may subsequently be
     * initialized by a call to
     * {@link java.lang.Throwable#initCause(java.lang.Throwable)}.
     * 
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the {@link java.lang.Throwable#getMessage()}
     *            method.
     * 
     * @param errors
     *            Array of Exceptions
     */
    public MultiErrorException(String message, Exception[] errors)
    {
        super(message);
        setErrors(errors);
    }

    /**
     * Constructs a new multiple error exception with the specified detail
     * message and cause. Note that the detail message associated with cause is
     * not automatically incorporated in this exception's detail message.
     * 
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link java.lang.Throwable#getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link java.lang.Throwable#getCause()} method). (A null value
     *            is permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     * @param errors
     *            Array of Exceptions
     */
    public MultiErrorException(String message, Throwable cause,
            Exception[] errors)
    {
        super(message, cause);
        setErrors(errors);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message
     * of <code>(cause==null ? null : cause.toString())</code> (which
     * typically contains the class and detail message of cause). This
     * constructor is useful for exceptions that are little more than wrappers
     * for other throwables.
     * 
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link java.lang.Throwable#getCause()} method). (A null value
     *            is permitted, and indicates that the cause is nonexistent or
     *            unknown.) Constructs a new exception with the specified cause
     *            and a detail message of (cause==null ? null :
     *            cause.toString()) (which typically contains the class and
     *            detail message of cause).
     * @param errors
     *            Array of Exceptions
     */
    public MultiErrorException(Throwable cause, Exception[] errors)
    {
        super(cause);
        setErrors(errors);
    }

    private void setErrors(Exception[] errors)
    {
        if (errors == null)
        {
            this.errors = new Exception[0];
        }
        else
        {
            this.errors = (Exception[]) errors.clone();
        }
        this.errors = errors;
    }

    /**
     * Returns the array of errors.
     * 
     * @return Array of errors (possibly of length 0)
     */
    public Exception[] getErrors()
    {
        return errors;
    }
    
    /**
     * Returns the detail message string integrating those from the errors.
     * @return the detailed message string
     */
    public String getMessage()
    {
        //
        // Get the message from overridden method
        //
        String message = super.getMessage();
        
        //
        // Add the messages from errors
        //
        for ( int i = 0; i < errors.length; i++ )
        {
            message += "\n[" + i + "] " + errors[i].toString();
        }
        
        return message;
    }
}