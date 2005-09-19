/*--
$Id: Grantable.java,v 1.2 2005-09-19 06:37:04 acohen Exp $
$Date: 2005-09-19 06:37:04 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.Set;

/**
 * This interface encapsulates some attributes that are common to Assignment
 * and Proxy.
 */
public interface Grantable
extends Entity, Comparable
{
  /**
   * Gets the unique identifier of this grantable entity.
   * 
   * @return the unique identifier of this grantable entity.
   */
  public Integer getId();

  /**
   * Gets the <code>PrivilegedSubject</code>
   * who is the grantee of this grantable entity.
   * 
   * @return the <code>PrivilegedSubject</code>
   * who is the grantee of this grantable entity.
   */
  public PrivilegedSubject getGrantee();

  /**
   * Gets the <code>PrivilegedSubject</code> 
   * who is the grantor of this grantable entity.
   * 
   * @return the <code>PrivilegedSubject</code> 
   * who is the grantor of this grantable entity.
   */
  public PrivilegedSubject getGrantor();

  /**
   * Gets the <code>PrivilegedSubject</code> 
   * who acually granted this grantable entity while "acting as" a proxy for the
   * official grantor, or <code>null</code>.
   * 
   * @return the <code>PrivilegedSubject</code> 
   * who acted as a proxy for the official grantor when granting this grantable
   * entity, or <code>null</code> if there was no such proxy involved.
   */
  public PrivilegedSubject getProxy();


  /**
   * Gets the <code>PrivilegedSubject</code> 
   * who revoked this grantable entity, or <code>null</code> if this grantable entity has
   * not yet been revoked.
   * 
   * @return the <code>PrivilegedSubject</code> 
   * who revoked this grantable entity, or <code>null</code> if this grantable entity has
   * not yet been revoked.
   */
  public PrivilegedSubject getRevoker();

  /**
   * Gets the effective date of this grantable entity. This is the date on which
   * this grantable entity is scheduled to change from {@link Status} value PENDING to
   * <code>Status</code> value ACTIVE.
   * 
   * @return the scheduled effective-date of this grantable entity.
   */
  public Date getEffectiveDate();
  
  /**
   * Changes the effective date of an existing grantable entity. To save this change
   * to the database, call <code>save()</code>.
   * 
   * @param actor the <code>PrivilegedSubject</code> who is responsible for
   * this change.
   * 
   * @param effectiveDate the date on which this grantable entity should be scheduled
   * to change from {@link Status} value PENDING to <code>Status</code> value
   * ACTIVE.
   * 
   * @throws SignetAuthorityException
   */
  public void setEffectiveDate
    (PrivilegedSubject  actor,
     Date               effectiveDate)
  throws SignetAuthorityException;
  
  /**
   * Gets the date and time when this grantable entity actually changed from
   * {@link Status} value PENDING to <code>Status</code> value ACTIVE. If that
   * change has not yet occurred, then this method will return
   * <code>null</code>.
   * 
   * @return the actual date and time this grantable entity became active.
   */
  public Date getActualStartDatetime();
  
  /**
   * Gets the expiration date of this grantable entity. This is the date on which
   * this grantable entity is scheduled to change from {@link Status} value ACTIVE
   * to <code>Status</code> value INACTIVE.
   * 
   * @return the scheduled expiration-date of this grantable entity.
   */
  public Date getExpirationDate();
  
  /**
   * Changes the expiration date of an existing grantable entity. To save this change
   * to the database, call <code>save()</code>.
   * 
   * @param editor the PrivilegedSubject who is responsible for this change.
   * 
   * @param expirationDate the date on which this grantable entity should be scheduled
   * to change from {@link Status} value ACTIVE to <code>Status</code> value
   * INACTIVE.
   * 
   * @throws SignetAuthorityException
   */
  public void setExpirationDate
    (PrivilegedSubject  editor,
     Date               expirationDate)
  throws SignetAuthorityException;
  
  /**
   * Gets the date and time when this grantable entity actually changed from
   * {@link Status} value ACTIVE to <code>Status</code> value INACTIVE. If that
   * change has not yet occurred, this this method will return
   * <code>null</code>.
   * 
   * @return the actual date and time this grantable entity became inactive.
   */
  public Date getActualEndDatetime();

  /**
   * Gets the <code>Status</code> of this grantable entity. A grantable entity may have
   * <code>Status</code> of ACTIVE, INACTIVE, or PENDING.
   * 
   * @return the <code>Status</code> of this grantable entity.
   */
  public Status getStatus();

  /**
   * Revokes the specified grantable entity from its current grantee. Note that in the
   * case of duplicate or overlapping grantable entities, the grantee may still retain
   * a given {@link Privilege} even after the revocation of a single
   * grantable entity that grants that <code>Privilege</code>.
   * 
   * @throws SignetAuthorityException
   * 
   * @see #findDuplicates()
   * 
   */
  public void revoke(PrivilegedSubject revoker)
  throws SignetAuthorityException;
  
  /**
   * Evaluate the conditions and pre-requisites associated with this grantable entity
   * relationship (including effectiveDate and expirationDate) to update the
   * <code>Status</code> of this grantable-entity relationship.
   * 
   * @return the new <code>Status</code> value
   */
  public Status evaluate();
  
  /**
   * Finds all pending and active Proxies in the database which are
   * duplicates of this grantable entity. Duplicate Proxies are those which
   * have the same grantor, grantee, and {@link Subsystem}.
   * 
   * @return a Set of duplicate Proxies, or an empty <code>Set</code> if none
   * are found.
   */
  public Set findDuplicates();
  
  /**
   * Persists the current state of this grantable entity.
   *
   */
  public void save();

}
