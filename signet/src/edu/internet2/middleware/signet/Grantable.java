/*--
$Id: Grantable.java,v 1.10 2007-02-24 02:11:32 ddonn Exp $
$Date: 2007-02-24 02:11:32 $

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
import java.util.Set;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

/**
 * This interface encapsulates some attributes that are common to
 * {@link Assignment} and {@link Proxy}.
 */
public interface Grantable
extends Entity, Comparable
{
  /**
   * Gets the persisted database-unique identifier of this grantable entity.
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
  public SignetSubject getGrantee();

  /**
   * Gets the <code>PrivilegedSubject</code> 
   * who is the grantor of this grantable entity.
   * 
   * @return the <code>PrivilegedSubject</code> 
   * who is the grantor of this grantable entity.
   */
  public SignetSubject getGrantor();

  /**
   * Gets the <code>PrivilegedSubject</code> 
   * who acually granted this grantable entity while "acting as" a proxy for the
   * official grantor, or <code>null</code>.
   * 
   * @return the <code>PrivilegedSubject</code> 
   * who acted as a proxy for the official grantor when granting this grantable
   * entity, or <code>null</code> if there was no such proxy involved.
   */
  public SignetSubject getProxy();


  /**
   * Gets the <code>PrivilegedSubject</code> 
   * who revoked this grantable entity, or <code>null</code> if this grantable entity has
   * not yet been revoked.
   * 
   * @return the <code>PrivilegedSubject</code> 
   * who revoked this grantable entity, or <code>null</code> if this grantable entity has
   * not yet been revoked.
   */
  public SignetSubject getRevoker();

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
   * @param actor the <code>PrivilegedSubject</code> who is responsible for
   * this change.
   * @param effectiveDate the date on which this grantable entity should be scheduled
   * to change from {@link Status} value PENDING to <code>Status</code> value
   * ACTIVE.
   * @param checkAuth Flag to indicate whether to check for Edit authority by given actor.
   * Note that quite often several values may be set/updated for a Grantable for
   * the actor. Setting checkAuth to false assumes that the caller of the 'set'
   * methods has already called checkEditAuthority(SignetSubject).
   * @throws SignetAuthorityException
   */
  public void setEffectiveDate(SignetSubject actor, Date effectiveDate, boolean checkAuth)
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
   * @param editor the PrivilegedSubject who is responsible for this change.
   * @param expirationDate the date on which this grantable entity should be scheduled
   * to change from {@link Status} value ACTIVE to <code>Status</code> value
   * INACTIVE.
   * @param checkAuth Flag to indicate whether to check for Edit authority by given actor.
   * Note that quite often several values may be set/updated for a Grantable for
   * the actor. Setting checkAuth to false assumes that the caller of the 'set'
   * methods has already called checkEditAuthority(SignetSubject).
   * @throws SignetAuthorityException
   */
  public void setExpirationDate(SignetSubject editor, Date expirationDate, boolean checkAuth)
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
   * @param revoker the <code>PrivilegedSubject</code> who is attempting to
   * revoke this <code>Grantable</code> entity.
   * 
   * @throws SignetAuthorityException
   * 
   * @see #findDuplicates()
   * 
   */
  public void revoke(SignetSubject revoker)
  throws SignetAuthorityException;
  
  /**
   * Evaluate the conditions and pre-requisites associated with this grantable
   * entity (including effectiveDate and expirationDate) to update the
   * <code>Status</code> of this grantable-entity relationship.
   * 
   * @return <code>true</code> if the <code>Status</code> value of this
   * <code>Assignment</code> was changed, and <code>false</code> otherwise.
   */
  public boolean evaluate();
  
  /**
   * Evaluate the conditions and pre-requisites associated with this grantable
   * entity (including effectiveDate and expirationDate) to update the
   * <code>Status</code> of this grantable-entity relationship.
   * 
   * @param date the <code>Date</code> value to use as the current date and time
   * when evaluating effectiveDate and expirationDate.
   * 
   * @return <code>true</code> if the <code>Status</code> value of this
   * <code>Assignment</code> was changed, and <code>false</code> otherwise.
   */
  public boolean evaluate(Date date);
  
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
	 * Generate a History object from this and add it to the history Set.
	 */
	public void createHistoryRecord();

  /**
   * Retrieves the <code>Set</code> of objects which describe the history of
   * this grantable entity.
   * 
   * @return a set of history objects.
   */
  public Set getHistory();

	/**
	 * Check whether the Subject has Edit permissions for this Grantable
	 * @param actor
	 * @throws NullPointerException
	 * @throws SignetAuthorityException
	 */
	public void checkEditAuthority(SignetSubject actor)
		throws NullPointerException, SignetAuthorityException;
}
