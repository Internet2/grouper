/*--
$Id: History.java,v 1.8 2006-10-25 00:08:28 ddonn Exp $
$Date: 2006-10-25 00:08:28 $

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

import java.util.Date;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

/**
 * @author Andy Cohen
 *
 */
public interface History
{
  /**
   * Returns the grantor associated with this historical record.
   * Note that this return value reflects the current state of that
   * <code>PrivilegedSubject</code>, not its state at the time this historical
   * record was created.
   * 
   * @return the grantor associated with this historical record. 
   * Note that this return value reflects the current state of that
   * <code>PrivilegedSubject</code>, not its state at the time this historical
   * record was created.
   */
  public SignetSubject getGrantor();

  /**
   * Returns the grantee associated with this historical record.
   * Note that this return value reflects the current state of that
   * <code>PrivilegedSubject</code>, not its state at the time this historical
   * record was created.
   * 
   * @return the grantee associated with this historical record. 
   * Note that this return value reflects the current state of that
   * <code>PrivilegedSubject</code>, not its state at the time this historical
   * record was created.
   */
  public SignetSubject getGrantee();

  /**
   * Returns the revoker associated with this historical record, or
   * <code>null</code> if there is no revoker associated with this historical
   * record. Note that this return value reflects the current state of that
   * <code>PrivilegedSubject</code>, not its state at the time this historical
   * record was created.
   * 
   * @return the revoker associated with this historical record, or
   * <code>null</code> if there is no revoker associated with this historical
   * record. Note that this return value reflects the current state of that
   * <code>PrivilegedSubject</code>, not its state at the time this historical
   * record was created.
   */
  public SignetSubject getRevoker();

  /**
   * Returns the proxy-subject associated with this historical record, or
   * <code>null</code> if there is no proxy-subject associated with this
   * historical record. Note that this return value reflects the current state
   * of that <code>PrivilegedSubject</code>, not its state at the time this
   * historical record was created.
   * 
   * @return the proxy-subject associated with this historical record, or
   * <code>null</code> if there is no proxy-subject associated with this
   * historical record. Note that this return value reflects the current state
   * of that <code>PrivilegedSubject</code>, not its state at the time this
   * historical record was created.
   */
  public SignetSubject getProxySubject();
  
  /**
   * Returns the <code>Status</code> associated with this historical record.
   * 
   * @return the <code>Status</code> associated with this historical record.
   */
  public Status getStatus();

  /**
   * Returns the effective-date associated with this historical record.
   * 
   * @return the effective-date associated with this historical record.
   */
  public Date getEffectiveDate();

  /**
   * Returns the expiration-date associated with this historical record.
   * 
   * @return the expiration-date associated with this historical record.
   */
  public Date getExpirationDate();
  
  /**
   * Returns the date and time this historical event occurred.
   * 
   * @return the date and time this historical event occurred.
   */
  public Date getDate();
}
