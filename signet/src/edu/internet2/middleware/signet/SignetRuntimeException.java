/*--
$Id: SignetRuntimeException.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

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
      // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public SignetRuntimeException(String message)
  {
      super(message);
      // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public SignetRuntimeException(String message, Throwable cause)
  {
      super(message, cause);
      // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public SignetRuntimeException(Throwable cause)
  {
      super(cause);
      // TODO Auto-generated constructor stub
  }

}
