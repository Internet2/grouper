/*--
 $Id: SignetRuntimeException.java,v 1.3 2005-01-11 20:38:44 acohen Exp $
 $Date: 2005-01-11 20:38:44 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

/**
 * This exception is thrown whenever Signet encounters an error that cannot
 * be reasonably addressed by Signet application programs.
 * 
 */
public class SignetRuntimeException extends RuntimeException
{
  /**
   * 
   */
  public SignetRuntimeException()
  {
    super();
  }

  /**
   * @param message
   */
  public SignetRuntimeException(String message)
  {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public SignetRuntimeException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public SignetRuntimeException(Throwable cause)
  {
    super(cause);
  }

}