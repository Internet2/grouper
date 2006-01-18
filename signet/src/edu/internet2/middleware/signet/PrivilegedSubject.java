/*--
$Id: PrivilegedSubject.java,v 1.24 2006-01-18 17:11:59 acohen Exp $
$Date: 2006-01-18 17:11:59 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.Set;

import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;

/**
* PrivilegedSubject describes the {@link Permission}s possessed by a
* {@link Subject}.
*/

public interface PrivilegedSubject extends Comparable
{
  /**
   * Gets the unique identifier of this <code>PrivilegedSubject</code>.
   * 
   * @return the unique identifier of this <code>PrivilegedSubject</code>.
   */
  public Integer getId();
  
 /**
  * This method returns a <code>Decision</code> object that describes whether
  * or not this PrivilegedSubject has authority to edit the argument
  * <code>Grantable</code> object (either a {@link Proxy} or an
  * {@link Assignment}), and if not, why not.
  * 
  * Generally, if the <code>Grantable</code> object's
  * grantee matches this PrivilegedSubject, then <code>canEdit()</code> would
  * return a negatuve <code>Decision</code>, since in most situations it does
  * not make sense to attempt to extend or modify your own authority.
  */
 public Decision canEdit
   (Grantable grantableInstance);
 
 /**
  * Creates a Proxy relationship by delegating privileges to a
  * <code>PrivilegedSubject</code>.
  * 
  * @param grantee
  * 
  * @param subsystem The <code>Subsystem</code> in which the grantee will be
  *        allowed to act in the name of this PrivilegedSubject. Note that
  *        it is legal to grant a <code>Proxy</code> in any
  *        <code>Subsystem</code>, regardless of whether or not the grantor
  *        currently has any privileges in that <code>Subsystem</code>. If
  *        this parameter is <code>null</code>, then this <code>Proxy</code>
  *        will be available in every <code>Subsystem</code>.
  *
  * @param canUse When <code>true</true>, means that the grantee, when acting
  *        as a Proxy for this grantor, can use this Proxy to directly
  *        grant {@link Assignment}s to other <code>PrivilegedSubject</code>s.
  *
  * @param canExtend When <code>true</code>, means that this grantee, when
  *        acting as a Proxy for this grantor, can grant the grantor's Proxy
  *        to some third <code>PrivilegedSubject</code>. Someday, we may
  *        add some mechanism to limit the length of these Proxy-chains.
  *
  * @param effectiveDate
  * @param expirationDate
  * 
  * @return the new Proxy object which describes the Proxy relationship.
  * @throws SignetAuthorityException
  */
 public Proxy grantProxy
  (PrivilegedSubject grantee,
   Subsystem         subsystem,
   boolean           canUse,
   boolean           canExtend,
   Date              effectiveDate,
   Date              expirationDate)
 throws SignetAuthorityException;
 
 /**
  * Creates an Assignment (possibly by exercising a {@link Proxy}) to grant a
  * Function to a PrivilegedSubject.
  * 
  * @param grantee
  * @param scope
  * @param function
  * @param limitValues
  * @param canUse
  * @param canGrant
  * @param effectiveDate
  * @param expirationDate
  * 
  * @return the resulting Assignment
  * 
  * @throws SignetAuthorityException, ObjectNotFoundException
  */
 public Assignment grant
 (PrivilegedSubject  grantee,
  TreeNode           scope,
  Function           function,
  Set                limitValues,
  boolean            canUse,
  boolean            canGrant,
  Date               effectiveDate,
  Date               expirationDate)
 throws
   SignetAuthorityException;
 
 /**
  * Gets all the {@link Proxy}s which have been received by this
  * <code>PrivilegedSubject</code>.
  * 
  * @return all the {@link Proxy}s which have been received by this
  * <code>PrivilegedSubject</code>.
  */
 public Set getProxiesReceived();
 
 /**
  * Gets all the {@link Proxy}s which have been granted by this
  * <code>PrivilegedSubject</code>.
  * 
  * @return all the {@link Proxy}s which have been granted by this
  * <code>PrivilegedSubject</code>.
  */
 public Set getProxiesGranted();
 
 /**
  * Gets all the Assignments which have been received by this
  * PrivilegedSubject.
  *
  * @return all the Assignments which have been received by this
  * PrivilegedSubject.
  */
 public Set getAssignmentsReceived();
 
 /**
  * Gets all the {@link Assignment}s which have been granted by this
  * <code>PrivilegedSubject</code>.
  * 
  * @return all the {@link Proxy}s which have been granted by this
  * <code>PrivilegedSubject</code>.
  */
 public Set getAssignmentsGranted();
 
 /**
  * Gets all of the {@link Subsystem}s that this <code>PrivilegedSubject</code>
  * can grant {@link Assignment}s for. The result of this method will vary
  * depending upon the grantable <code>Assignment</code>s held by this
  * <code>PrivilegedSubject</code>, or, if this <code>PrivilegedSubject</code>
  * is "acting for" some other, the usable {@link Proxy}s held by this
  * <code>PrivilegedSubject</code>.
  * 
  * @return all of the Subsystems that this PrivilegedSubject can grant
  * Assignments for
  * .
  * @throws ObjectNotFoundException
  */
 public Set getGrantableSubsystemsForAssignment();

 
 /**
  * Gets all of the {@link Subsystem}s that this <code>PrivilegedSubject</code>
  * can grant {@link Proxy}s for. The result of this method will vary
  * depending upon whether or not this <code>PrivilegedSubject</code>
  * is "acting for" some other. If this <code>PrivilegedSubject</code> is
  * acting only under its own authority, then any <code>Subsystem</code> is
  * proxyable, without regard to any <code>Assignment</code>s or
  * <code>Proxy</code>s held by this <code>PrivilegedSubject</code>. If, on the
  * other hand, this <code>PrivilegedSubject</code> is "acting for" another,
  * then only those <code>Subsystem</code> associated with active, extensible
  * <code>Proxy</code>s received from that other are grantable.
  * 
  * @return all of the Subsystems that this PrivilegedSubject can grant
  * Proxies for
  * .
  * @throws ObjectNotFoundException
  */
 public Set getGrantableSubsystemsForProxy();
 
 /**
  * Gets all of the Categories that this PrivilegedSubject holds
  * grantable Assignments for in the specified Subsystem.
  * 
  * @param subsystem
  * 
  * @return all of the Categories that this PrivilegedSubject holds
  * grantable Assignments for.
  * @throws ObjectNotFoundException
  */
 public Set getGrantableCategories(Subsystem subsystem)
 throws ObjectNotFoundException;
 
 /**
  * Gets all of the Functions that this PrivilegedSubject holds
  * grantable Assignments for in the specified Category.
  * 
  * @param category
  * 
  * @return all of the Functions that this PrivilegedSubject holds
  * grantable Assignments for.
  * @throws ObjectNotFoundException
  */
 public Set getGrantableFunctions(Category category)
 throws ObjectNotFoundException;
 
 /**
  * Gets all of the TreeNodes that this PrivilegedSubject holds
  * grantable Assignments for regarding the specified Function.
  * 
  * @param function
  * 
  * @return all of the TreeNodes that this PrivilegedSubject holds
  * grantable Assignments for regarding the specified Function.
  */
 public Set getGrantableScopes(Function function);
 
 /**
  * Gets the Subject which underlies this PrivilegedSubject.
  * @return the ID of the Subject which underlies this PrivilegedSubject.
 * @throws ObjectNotFoundException
  */
 public Subject getSubject()
 throws ObjectNotFoundException;
 
 /**
  * Gets the ID of the Subject which underlies this PrivilegedSubject.
  * @return the ID of the Subject which underlies this PrivilegedSubject.
  */
 public String getSubjectId();

 /**
  * Gets the type ID of the Subject which underlies this PrivilegedSubject.
  * @return the type ID of the Subject which underlies this PrivilegedSubject.
  */
 public String getSubjectTypeId();
 
 /**
  * Gets the name of the Subject which underlies this PrivilegedSubject.
  * @return the name of the Subject which underlies this PrivilegedSubject.
  */
 public String getName();
 
 /**
  * Gets the description of the Subject which underlies this PrivilegedSubject.
  * @return the description of the Subject which underlies this PrivilegedSubject.
  */
 public String getDescription();
 
 /**
  * This method produces a Set of those Choice values which are grantable
  * by this PrivilegedSubject in the context of the specified Function and
  * scope.
  * 
  * @param function The Function for which grantable Choices should be found.
  * @param scope The TreeNode for which grantable Choices should be found.
  * @param limit The Limit for which grantable Choices should be found.
  * @return a Set of the grantable Choices for this combination of Function,
  * Tree, and Limit.
  */
 public Set getGrantableChoices
   (Function function,
    TreeNode scope,
    Limit    limit);
 
 // All method declarations below this point were originally commented out
 // by Minh.
 
 /*
  This method checks to see if this SignetSubject can
  designate a proxy for the specified ProxyType.
  If the proxy function can only be designated to
  one person; this method returns false if a ProxyAssignment
  already exists.
  */
 //public boolean canDesignateProxy(ProxyType type);
 
 /*
  This method returns true if this SignetSubject can designate
  the argument ProxyType for argument SignetSubject.<p>
  A SignetSubject can designate a ProxyAssignment if
  1) The argument SignetSubject equals this SignetSubject, or
  2) If this SignetSubject is proxy for argument SignetSubject and argument
  proxy function is not a granting proxy, or
  3) If SignetSubject can edit <b>all</b> assignments for authorization functions
  of the proxy function.<p>
  This method does not check if target SignetSubject has the appropriate
  assignment/pre-requisite to designate a proxy.
  */
 //public boolean canDesignateProxyFor(ProxyType type, PrivilegedSubject
 //otherSignetSubject);
 
 /*
  This method returns true if this SignetSubject has authority to edit
  the argument ProxyAssignment.
  */
 //public boolean canEdit(ProxyAssignment aProxyAssignment);
 
 /*
  This method returns an array of SignetSubjects that this SignetSubject can act for
  by ProxyType.
  */
 //public PrivilegedSubject[] canProxyFor(ProxyType type);

 
 /**
  * Gets all the Privileges which are currently held by this
  * PrivilegedSubject.
  * 
  * @return all the Privileges currently held by this PrivilegedSubject which
  * are related to the specified Subsystem.
  */
 public Set getPrivileges();
 
 public void setActingAs(PrivilegedSubject actingAs)
 throws SignetAuthorityException;
 
 PrivilegedSubject getEffectiveEditor();
 

 

 
 /**
   * Evaluate the conditions and pre-requisites associated with all of this
   * <code>PrivilegedSubject</code>'s {@link Assignment}s and {@link Proxy}s
   * (including effectiveDate and expirationDate) to update the
   * <code>Status</code> of those <code>Assignment</code>s and
   * <code>Proxy</code>s.
  * 
  * @return a <code>Set</code> of all Grantable entities whose
  * <code>Status</code> values were changed by this method.
  */
 public Set reconcile();
 
  /**
   * Evaluate the conditions and pre-requisites associated with all of this
   * <code>PrivilegedSubject</code>'s {@link Assignment}s and {@link Proxy}s
   * (including effectiveDate and expirationDate) to update the
   * <code>Status</code> of those <code>Assignment</code>s and
   * <code>Proxy</code>s.
   * 
   * @param date the <code>Date</code> value to use as the current date and time
   * when evaluating effectiveDate and expirationDate.
   * 
   * @return a <code>Set</code> of all Grantable entities whose
   * <code>Status</code> values were changed by this method.
   */
  public Set reconcile(Date date);
}

