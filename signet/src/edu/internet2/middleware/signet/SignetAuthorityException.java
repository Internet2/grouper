/*--
$Id: SignetAuthorityException.java,v 1.5 2006-02-09 10:24:53 lmcrae Exp $
$Date: 2006-02-09 10:24:53 $
 
Copyright 2006 Internet2, Stanford University

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
