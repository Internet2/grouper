/*--
$Id: Assignment.java,v 1.8 2005-06-17 23:24:28 acohen Exp $
$Date: 2005-06-17 23:24:28 $

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

/**
* 
* An Assignment represents some authority which has been granted to a 
* Subject (often a Person).  The granularity of an assignment is always 
* Function; that is, a Function is the smallest unit of authority which 
* can be assigned.  An assignment always has an organizational scope 
* associated with it, and a condition Organization associated with it.  
* <p>
* The scope Organization represents the highest level in the
* organizational hierarchy at which the subject can exercise the Function; 
* the condition Organization is an organization to which the Subject must 
* belong for the authority to be active.  For example, an assignment can 
* be interpreted to mean:
* <p>
* "(Subject) can perform (Function) in (Scope) as long as (Subject)
* belongs to (condition organization)".
* <p>
* In addition, there can be limits (constraints) on the assignment.  An 
* array of applicable limit names can be retrieved using the getLimitIds()
* method; getLimitValues(String limitId) returns the limits applicable
* to this assignment.
* <p>
* Also, an assignment may or may not be delegatable.  If the assignment is
* delegatable, then the Subject may assign this function, with this scope,
* and with limits equal to or more restrictive than his own, to another 
* Subject.
* <p>
* An existing Assignment may be modified. To save the modified Assignment,
* call Assignment.save().
* 
* @see Function
* @see Scope
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
   * Gets the {@link PrivilegedSubject}
   * who is the grantee of this Assignment.
   * 
   * @return the {@link PrivilegedSubject}
   * who is the grantee of this Assignment.
   */
  public PrivilegedSubject getGrantee();

  /**
   * Gets the {@link PrivilegedSubject} 
   * who is the grantor of this Assignment.
   * 
   * @return the {@link PrivilegedSubject} 
   * who is the grantor of this Assignment.
   */
  public PrivilegedSubject getGrantor();


  /**
   * Gets the {@link PrivilegedSubject} 
   * who is the revoker of this Assignment, if this Assignment has been
   * revoked.
   * 
   * @return the {@link PrivilegedSubject} 
   * who is the grantor of this Assignment.
   */
  public PrivilegedSubject getRevoker();

  /**
   * Gets the scope (usually an organization) of this Assignment.
   * 
   * @return the scope (usually an organization) of this Assignment.
   */
  public edu.internet2.middleware.signet.tree.TreeNode getScope();

  /**
   * Gets the Function which is the subject of this Assignment.
   * 
   * @return the Function which is the subject of this Assignment.
   */
  public Function getFunction();

  /**
   * Gets the effective date of this Assignment. This is the date on which
   * this Assignment is scheduled to change from Status value PENDING to
   * Status value ACTIVE.
   * 
   * @return the scheduled effective-date of this Assignment.
   */
  public Date getEffectiveDate();
  
  /**
   * Changes the effective date of an existing Assignment. To save this change
   * to the database, call Assignment.save().
   * 
   * @param effectiveDate the date on which this Assignment should be scheduled
   * to change from Status value PENDING to Status value ACTIVE.
   */
  public void setEffectiveDate(Date effectiveDate);
  
  /**
   * Gets the date and time when this Assignment actually changed from Status
   * value PENDING to Status value ACTIVE. If that change has not yet occurred,
   * then this method will return null.
   * 
   * @return the actual date and time this Assignment became active.
   */
  public Date getActualStartDatetime();
  
  /**
   * Gets the expiration date of this Assignment. This is the date on which
   * this Assignment is scheduled to change from Status value ACTIVE to Status
   * value INACTIVE.
   * 
   * @return the scheduled expiration-date of this Assignment.
   */
  public Date getExpirationDate();
  
  /**
   * Changes the expiration date of an existing Assignment. To save this change
   * to the database, call Assignment.save().
   * 
   * @param expirationDate the date on which this Assignment should be scheduled
   * to change from Status value ACTIVE to Status value INACTIVE.
   */
  public void setExpirationDate(Date expirationDate);
  
  /**
   * Gets the date and time when this Assignment actually changed from Status
   * value ACTIVE to Status value INACTIVE. If that change has not yet occurred,
   * this this method will return null.
   * 
   * @return the actual date and time this Assignment became inactive.
   */
  public Date getActualEndDatetime();

  /**
   * Indicates whether or not this assignment can be granted to others
   * by its current grantee.
   * 
   * @return true if this assignment can be granted to others
   * by its current grantee.
   */
  public boolean isGrantable();
  
  /**
   * Changes the grantability of an existing Assignment. To save this change
   * to the database, call Assignment.save().
   *
   * @param isGrantable true if this Assignment should be grantable to others
   * by its current grantee, and false otherwise.
   */
  public void setGrantable(boolean isGrantable);

  /**
   * Indicates whether or not this assignment can be used directly
   * by its current grantee, or can only be granted to others.
   * 
   * @return true if this assignment can only be granted to others
   * by its current grantee, and not used directly by its current grantee.
   */
  public boolean isGrantOnly();
  
  /**
   * Changes the direct usability of an existing Assignment. To save this change
   * to the database, call Assignment.save();
   * 
   * @param isGrantOnly true if this Assignment should only be granted to others
   * (and not directly used) by its current grantee, and false otherwise.
   */
  public void setGrantOnly(boolean grantOnly);

  /**
   * Gets the Status of this Assignment. An Assignment may have Status of
   * ACTIVE, INACTIVE, or PENDING.
   * 
   * @return the Status of this Assignment.
   */
  public Status getStatus();

  /**
   * Gets the Limits and Limit-values applied to this Assignment.
   * 
   * @return a set of LimitValue objects, which represents all of the Limits
   * (constraints) applied to this Assignment, along with the values of those
   * Limits.
   */
  public Set getLimitValues();
  
  /**
   * Changes the Limit-values applied to an existing Assignment. To save this
   * change in the database, call Assignment.save().
   * 
   * @param limitValues the complete Set of LimitValues that should be
   * associated with this Assignment.
   *
   */
  public void setLimitValues(Set limitValues);

  /**
   * Revokes the specified Assignment from its current grantee. Note that in the
   * case of duplicate or overlapping Assignments, the grantee may still retain
   * a given Privilege even after the revocation of a single Assignment that
   * grants that Privilege.
   * 
   * @throws SignetAuthorityException
   * 
   * @see Signet.getMatchingAssignments()
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
}