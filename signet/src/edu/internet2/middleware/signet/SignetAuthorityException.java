/*--
 $Id: SignetAuthorityException.java,v 1.4 2005-08-25 20:31:35 acohen Exp $
 $Date: 2005-08-25 20:31:35 $
 
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
  private Decision decision = null;
  
  public SignetAuthorityException(Decision decision)
  {
    super();
    this.decision = decision;
  }
  
  public Decision getDecision()
  {
    return this.decision;
  }

//  /**
//   * 
//   */
//  public SignetAuthorityException()
//  {
//    super();
//  }
//
//  /**
//   * @param message
//   */
//  public SignetAuthorityException(String message)
//  {
//    super(message);
//  }
//
//  /**
//   * @param message
//   * @param cause
//   */
//  public SignetAuthorityException(String message, Throwable cause)
//  {
//    super(message, cause);
//  }
//
//  /**
//   * @param cause
//   */
//  public SignetAuthorityException(Throwable cause)
//  {
//    super(cause);
//  }

}