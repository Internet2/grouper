/*--
 $Id: SignetAuthorityException.java,v 1.3 2005-01-11 20:38:44 acohen Exp $
 $Date: 2005-01-11 20:38:44 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

/**
 * This exception is thrown whenever Signet refuses to perform an operation
 * because the Subject responsible for the operation lacks the authority
 * to perform that operation.
 * 
 */
public class SignetAuthorityException extends Exception
{

  /**
   * 
   */
  public SignetAuthorityException()
  {
    super();
  }

  /**
   * @param message
   */
  public SignetAuthorityException(String message)
  {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public SignetAuthorityException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public SignetAuthorityException(Throwable cause)
  {
    super(cause);
  }

}