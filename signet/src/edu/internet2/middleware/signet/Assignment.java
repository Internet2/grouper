/*--
$Id: Assignment.java,v 1.11 2005-08-16 01:26:31 acohen Exp $
$Date: 2005-08-16 01:26:31 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.SignetAuthorityException;

/**
* 
* An Assignment represents some authority which has been granted to a 
* {@link PrivilegedSubject} (often a person).  The granularity of an Assignment
* is always {@link Function}; that is, a <code>Function</code> is the smallest unit
* of authority which can be assigned.  An Assignment always has an
* organizational scope associated with it, and in the future, will also have a
* condition organization associated with it.  
* <p>
* The organizational scope represents the highest level in the
* organizational hierarchy at which the <code>PrivilegedSubject</code> can
* exercise the <code>Function</code>; 
* the condition organization, when it is introduced, will be an organization to
* which the <code>PrivilegedSubject</code> must belong for the authority to be
* active. For example, an Assignment can be interpreted to mean:
* <p>
* "(<code>PrivilegedSubject</code>) can perform (<code>Function</code>) in
* (organizational scope) as long as (<code>PrivilegedSubject</code>)
* belongs to (condition organization)".
* <p>
* In addition, there can be {@link Limit}s (constraints) on the assignment.
* <code>getLimitValues</code> returns the <code>Limit</code>s applicable to this
* assignment, along with the values of those <code>Limit</code>s.
* <p>
* Also, an Assignment may or may not be grantable.  If the Assignment is
* grantable, then the <code>PrivilegedSubject</code> may assign this
* <code>Function</code>, with scope equal to or more restrictive than his own,
* and with <code>Limit</code>-values equal to or more restrictive than his own,
* to another <code>PrivilegedSubject</code>.
* <p>
* An existing Assignment may be modified. To save the modified Assignment,
* call Assignment.save().
* 
* @see PrivilegedSubject
* @see Function
* 
*/

public interface Assignment
{
  /**
   * Gets the unique identifier of this Assignment.
   * 
   * @return the unique identifier of this Assignment.
   */
  public Integer getId();

  /**
   * Gets the <code>PrivilegedSubject</code>
   * who is the grantee of this Assignment.
   * 
   * @return the <code>PrivilegedSubject</code>
   * who is the grantee of this Assignment.
   */
  public PrivilegedSubject getGrantee();

  /**
   * Gets the <code>PrivilegedSubject</code> 
   * who is the grantor of this Assignment.
   * 
   * @return the <code>PrivilegedSubject</code> 
   * who is the grantor of this Assignment.
   */
  public PrivilegedSubject getGrantor();


  /**
   * Gets the <code>PrivilegedSubject</code> 
   * who revoked this Assignment, or <code>null</code> if this Assignment has
   * not yet been revoked.
   * 
   * @return the <code>PrivilegedSubject</code> 
   * who revoked this Assignment, or <code>null</code> if this Assignment has
   * not yet been revoked.
   */
  public PrivilegedSubject getRevoker();

  /**
   * Gets the scope (usually an organization) of this Assignment.
   * 
   * @return the scope (usually an organization) of this Assignment.
   */
  public edu.internet2.middleware.signet.tree.TreeNode getScope();

  /**
   * Gets the <code>Function</code> which is the subject of this Assignment.
   * 
   * @return the <code>Function</code> which is the subject of this Assignment.
   */
  public Function getFunction();

  /**
   * Gets the effective date of this Assignment. This is the date on which
   * this Assignment is scheduled to change from {@link Status} value PENDING to
   * <code>Status</code> value ACTIVE.
   * 
   * @return the scheduled effective-date of this Assignment.
   */
  public Date getEffectiveDate();
  
  /**
   * Changes the effective date of an existing Assignment. To save this change
   * to the database, call Assignment.save().
   * 
   * @param actor the <code>PrivilegedSubject</code> who is responsible for
   * this change.
   * 
   * @param effectiveDate the date on which this Assignment should be scheduled
   * to change from {@link Status} value PENDING to <code>Status</code> value
   * ACTIVE.
   * 
   * @throws SignetAuthorityException
   */
  public void setEffectiveDate(PrivilegedSubject actor, Date effectiveDate)
  throws SignetAuthorityException;
  
  /**
   * Gets the date and time when this Assignment actually changed from
   * {@link Status} value PENDING to <code>Status</code> value ACTIVE. If that
   * change has not yet occurred, then this method will return
   * <code>null</code>.
   * 
   * @return the actual date and time this Assignment became active.
   */
  public Date getActualStartDatetime();
  
  /**
   * Gets the expiration date of this Assignment. This is the date on which
   * this Assignment is scheduled to change from {@link Status} value ACTIVE
   * to <code>Status</code> value INACTIVE.
   * 
   * @return the scheduled expiration-date of this Assignment.
   */
  public Date getExpirationDate();
  
  /**
   * Changes the expiration date of an existing Assignment. To save this change
   * to the database, call <code>Assignment.save()</code>.
   * 
   * @param editor the PrivilegedSubject who is responsible for this change.
   * 
   * @param expirationDate the date on which this Assignment should be scheduled
   * to change from {@link Status} value ACTIVE to <code>Status</code> value
   * INACTIVE.
   * 
   * @throws SignetAuthorityException
   */
  public void setExpirationDate(PrivilegedSubject editor, Date expirationDate)
  throws SignetAuthorityException;
  
  /**
   * Gets the date and time when this Assignment actually changed from
   * {@link Status} value ACTIVE to <code>Status</code> value INACTIVE. If that
   * change has not yet occurred, this this method will return
   * <code>null</code>.
   * 
   * @return the actual date and time this Assignment became inactive.
   */
  public Date getActualEndDatetime();

  /**
   * Indicates whether or not this Assignment can be granted to others
   * by its current grantee.
   * 
   * @return <code>true</code> if this Assignment can be granted to others
   * by its current grantee.
   */
  public boolean isGrantable();
  
  /**
   * Changes the grantability of an existing Assignment. To save this change
   * to the database, call <code>Assignment.save()</code>.
   * 
   * @param editor the <code>PrivilegedSubject</code> who is responsible for
   * this change.
   *
   * @param isGrantable <code>true</code> if this Assignment should be grantable
   * to others by its current grantee, and <code>false</code> otherwise.
   * 
   * @throws SignetAuthorityException
   */
  public void setGrantable(PrivilegedSubject editor, boolean isGrantable)
  throws SignetAuthorityException;

  /**
   * Indicates whether or not this Assignment can be used directly
   * by its current grantee, or can only be granted to others.
   * 
   * @return <code>true</code> if this Assignment can only be granted to others
   * by its current grantee, and not used directly by its current grantee.
   */
  public boolean isGrantOnly();
  
  /**
   * Changes the direct usability of an existing Assignment. To save this change
   * to the database, call <code>Assignment.save()</code>;
   * 
   * @param editor the <code>PrivilegedSubject</code> who is responsible for
   * this change.
   * 
   * @param isGrantOnly <code>true</code> if this Assignment should only be
   * granted to others (and not directly used) by its current grantee, and
   * <code>false</code> otherwise.
   * 
   * @throws SignetAuthorityException
   */
  public void setGrantOnly(PrivilegedSubject editor, boolean isGrantOnly)
  throws SignetAuthorityException;

  /**
   * Gets the <code>Status</code> of this Assignment. An Assignment may have
   * <code>Status</code> of ACTIVE, INACTIVE, or PENDING.
   * 
   * @return the <code>Status</code> of this Assignment.
   */
  public Status getStatus();

  /**
   * Gets the {@link Limit}s and <code>Limit</code>-values applied to this
   * Assignment.
   * 
   * @return a set of {@link LimitValue} objects, which represents all of the
   * <code>Limit</code>s (constraints) applied to this Assignment, along with
   * the values of those <code>Limit</code>s.
   */
  public Set getLimitValues();
  
  /**
   * Changes the {@link Limit}-values applied to an existing Assignment. To save
   * this change in the database, call Assignment.save().
   * 
   * @param editor the <code>PrivilegedSubject</code> who is responsible for
   * this change.
   * 
   * @param limitValues the complete Set of {@link LimitValue}s that should be
   * associated with this Assignment.
   * 
   * @throws SignetAuthorityException
   *
   */
  public void setLimitValues(PrivilegedSubject editor, Set limitValues)
  throws SignetAuthorityException;

  /**
   * Revokes the specified Assignment from its current grantee. Note that in the
   * case of duplicate or overlapping Assignments, the grantee may still retain
   * a given {@link Privilege} even after the revocation of a single
   * Assignment that grants that <code>Privilege</code>.
   * 
   * @throws SignetAuthorityException
   * 
   * @see #findDuplicates()
   * 
   */
  public void revoke(PrivilegedSubject revoker)
  throws SignetAuthorityException;
  
  /**
   * Finds all pending and active Assignments in the database which are
   * duplicates of this Assignment. Duplicate Assignments are those which
   * have the same grantee, function, scope, and limit-values.
   * 
   * @return a Set of duplicate Assignments, or an empty Set if none are found.
   */
  public Set findDuplicates();
  
  /**
   * Persists the current state of this Assignment.
   *
   */
  public void save();
}