/*--
 $Id: History.java,v 1.5 2005-12-06 22:34:51 acohen Exp $
 $Date: 2005-12-06 22:34:51 $

 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Date;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
  public PrivilegedSubject getGrantor();

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
  public PrivilegedSubject getGrantee();

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
  public PrivilegedSubject getRevoker();

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
  public PrivilegedSubject getProxySubject();
  
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
   * Returns the date and time this historical event occurred.
   * 
   * @return the date and time this historical event occurred.
   */
  public Date getDate();
}