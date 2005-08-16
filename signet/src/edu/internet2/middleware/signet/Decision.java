/*--
$Id: Decision.java,v 1.1 2005-08-16 16:41:08 acohen Exp $
$Date: 2005-08-16 16:41:08 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
 * This class represents a yes-or-no decision regarding whether or not to
 * allow a specific {@link PrivilegedSubject} to perform a specific Signet
 * operation, along with the supporting information to explain why that
 * decision was made.
 */
public interface Decision
{
  /**
   * Indicates whether or not Signet is allowing the attempted operation.
   * 
   * @return <code>true</code> if the operation is allowed, and false otherwise.
   */
  public boolean getAnswer();
  
  /**
   * If {@link #getAnswer()} returns false, then this method returns a code
   * which indicates the reason for the refusal. Otherwise, it returns 
   * <code>null</code>.
   * 
   * @return a code which indicates the reason for a refusal.
   */
  public Reason getReason();
  
  /**
   * If {@link #getReason()} returns <code>Reason.LIMIT</code>, then this
   * method returns the <code>Limit</code> which prevented the operation from
   * succeeding. Otherwise, it returns null.
   * 
   * @return the <code>Limit</code> which prevented the operation from
   * succeeding.
   */
  public Limit getLimit();
}
