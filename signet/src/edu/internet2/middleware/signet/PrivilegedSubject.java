/*--
 $Id: PrivilegedSubject.java,v 1.5 2005-02-21 23:27:34 acohen Exp $
 $Date: 2005-02-21 23:27:34 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;

/**
 * PrivilegedSubject describes the {@link Permission}s possessed by a
 * {@link Subject}.
 */

public interface PrivilegedSubject extends Subject, Comparable
{
  /**
   * This method returns true if this PrivilegedSubject has authority to
   * edit the argument Assignment.  Generally, if the Assignment's
   * SignetSubject matches this SignetSubject, then canEdit would return
   * false, since in most situations it does not make sense to attempt to
   * extend or modify your own authority.
   */
  public boolean canEdit(Assignment anAssignment);
  
  /**
   * This method produces a human-readable String which describes the reason,
   * if any, why this PrivilegedSubject is prevented from editing the
   * specified Assignment.
   * 
   * @param anAssignment
   * @param actor A description of the current PrivilegedSubject in the
   * 	context, used to make the generated explanation more readable. For
   * example, sometimes this PrivilegedSubject will be
   * attempting to act as a "grantor", sometimes as a "revoker".
   * @return A human-readable String.
   */
  public String editRefusalExplanation
    (Assignment anAssignment, String actor);
  
  /**
   * @deprecated This function is superseded by the newer version, which
   * includes a "limitValues" parameter.
   * 
   * Creates an Assignment by granting a Function to a PrivilegedSubject.
   * 
   * @param grantee
   * @param scope
   * @param function
   * @param canGrant
   * @param grantOnly
   * 
   * @return the resulting Assignment
   * 
   * @throws SignetAuthorityException, ObjectNotFoundException
   */
  public Assignment grant
  (PrivilegedSubject 	grantee,
   TreeNode 					scope,
   Function 					function,
   boolean 						canGrant,
   boolean 						grantOnly)
  throws
  	SignetAuthorityException,
  	ObjectNotFoundException;
  
  /**
   * Creates an Assignment by granting a Function to a PrivilegedSubject.
   * 
   * @param grantee
   * @param scope
   * @param function
   * @param limitValues
   * @param canGrant
   * @param grantOnly
   * 
   * @return the resulting Assignment
   * 
   * @throws SignetAuthorityException, ObjectNotFoundException
   */
  public Assignment grant
  (PrivilegedSubject 	grantee,
   TreeNode 					scope,
   Function 					function,
   Set								limitValues,
   boolean 						canGrant,
   boolean 						grantOnly)
  throws
  	SignetAuthorityException,
  	ObjectNotFoundException;
  
  /**
   * Gets all the Assignments which have been received by this
   * PrivilegedSubject.
   * 
   * @param status The Status value to filter the result by. A null value
   * returns all Assignments regardless of Status.
   * @param subsystem
   * @return all the Assignments which have been received by this
   * PrivilegedSubject.
   * @throws ObjectNotFoundException
   */
  public Set getAssignmentsReceived(Status status, Subsystem subsystem)
  throws ObjectNotFoundException;
  
  /**
   * Gets all the Assignments which have been granted by this
   * PrivilegedSubject.
   * @param status
   * @param subsystem
   * @return all the Assignments which have been granted by this
   * PrivilegedSubject.
   * @throws ObjectNotFoundException
   */
  public Set getAssignmentsGranted(Status status, Subsystem subsystem)
  throws ObjectNotFoundException;
  
  /**
   * Gets all of the Subsystems that this PrivilegedSubject holds
   * grantable Assignments for.
   * 
   * @return all of the Subsystems that this PrivilegedSubject holds
   * grantable Assignments for.
   * @throws ObjectNotFoundException
   */
  public Set getGrantableSubsystems()
  throws ObjectNotFoundException;
  
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
   * Gets the ID of the SubjectType which underlies this PrivilegedSubject.
   * @return the ID of the SubjectType which underlies this
   *   PrivilegedSubject.
   */
  public String getSubjectTypeId();
  
  /**
   * Gets the ID of the Subject which underlies this PrivilegedSubject.
   * @return the ID of the Subject which underlies this PrivilegedSubject.
   */
  public String getSubjectId();
  
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
  
  /*
   This method returns all of the active, exercisable privileges held by this
   Subject.
   
   @see Privilege
   */
  //public Privilege[] getPrivileges();
  
}

