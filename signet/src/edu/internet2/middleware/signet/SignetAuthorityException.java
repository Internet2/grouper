/*--
  $Id: SignetAuthorityException.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
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
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public SignetAuthorityException(String message)
  {
    super(message);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public SignetAuthorityException(String message, Throwable cause)
  {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public SignetAuthorityException(Throwable cause)
  {
    super(cause);
    // TODO Auto-generated constructor stub
  }

}
