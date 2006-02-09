/*--
$Id: Decision.java,v 1.2 2006-02-09 10:19:23 lmcrae Exp $
$Date: 2006-02-09 10:19:23 $
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
