/*--
$Id: Assignment.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;

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
* For our September release, the following Assignment-related entities
* will be implemented:
* <ul>
* 		<li>Assignment</li>
* 		<li>PrivilegedSubject</li>
* 		<li>TreeNode</li>
* 		<li>Function</li>
* </ul>
*
* The following Subsystem-related entities will appear in subsequent
* releases:
* <ul>
* 		<li>LimitChoice</li>
* 		<li>Condition</li>
* </ul>
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
 * 		 who is the grantee of this Assignment.
 * 
 * @return the {@link PrivilegedSubject}
 * 		 who is the grantee of this Assignment.
 */
public PrivilegedSubject getGrantee();

/**
 * Gets the {@link PrivilegedSubject} 
 * 		who is the grantor of this Assignment.
 * 
 * @return the {@link PrivilegedSubject} 
 * 		who is the grantor of this Assignment.
 */
public PrivilegedSubject getGrantor();


/**
 * Gets the {@link PrivilegedSubject} 
 * 		who is the revoker of this Assignment, if this Assignment has been
 *    revoked.
 * 
 * @return the {@link PrivilegedSubject} 
 * 		who is the grantor of this Assignment.
 */
public PrivilegedSubject getRevoker();

/**
 * Gets the scope (usually an organization) of this Assignment.
 * 
 * @return the scope (usually an organization) of this Assignment.
 */
public edu.internet2.middleware.signet.tree.TreeNode getScope();


/**
 * Gets the Subsystem associated with this Assignment.
 * 
 * @return the Subsystem associated with this Assignment.
 * @throws ObjectNotFoundException
 */
public Subsystem getSubsystem() throws ObjectNotFoundException;

/**
 * Gets the Function which is the subject of this Assignment.
 * 
 * @return the Function which is the subject of this Assignment.
 */
public Function getFunction();

/**
 * Gets the effective date of this Assignment.
 * 
 * @return the effective date of this Assignment.
 */
public Date getEffectiveDate();

/**
 * Indicates whether or not this assignment can be granted to others
 * by its current grantee.
 * 
 * @return true if this assignment can be granted to others
 * by its current grantee.
 */
public boolean isGrantable();

/**
 * Indicates whether or not this assignment can be used directly
 * by its current grantee, or can only be granted to others.
 * 
 * @return true if this assignment can only be granted to others
 * by its current grantee, and not used directly by its current grantee.
 */
public boolean isGrantOnly();

/**
 * Gets the Status of this Assignment.
 * 
 * @return the Status of this Assignment.
 */
public Status getStatus();

/**
 * Gets the date and time this entity was first created.
 * 
 * @return  the date and time this entity was first created.
 */
public Date getCreateDatetime();

/**
 * Revokes this Assignment from its current grantee.
 * 
 * @throws SignetAuthorityException
 * 
 */
public void revoke(PrivilegedSubject revoker)
throws SignetAuthorityException;

///**
// * @return the PrivilegedSubject who is the proxy that created/modified this 
// * 		assignment.
// */
//PrivilegedSubject getProxy();
//
///**
// * @return all Permissions associated with the Function
// * 		at the time this Assignment is created/modified.
// */
//Permission[] getPermissions();
//
///**
// * @return an array of Limits (constraints) applied to this assignment.
//*/
//Limit[] getLimits();
//
///**
// * @return an array of Prerequisites applied to this assignment.  Note 
// * that nothing is implied about whether the prerequisites have been 
// * satisfied or not.
// */
//Prerequisite[] getPrerequisites();
//
///**
// * @param delegatable whether the grantee of this assignment can 
// * delegate this privilege to others.
// */
//void setDelegatable(boolean delegatable);
//
///**
// * @param limit
// * @param values
// * 
// * This method can be used the set the values of a particular limit for 
// * this assignment, just as the overloaded version can be used for the 
// * same purpose.
// */
//void setLimitValues(Limit limit, LimitChoice[] values);
//
///**
// * @param proxy the proxy who created/modified an assignment.
// */
//public void setProxy(PrivilegedSubject proxy);

}
