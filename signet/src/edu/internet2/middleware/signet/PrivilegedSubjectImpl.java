/*--
 $Id: PrivilegedSubjectImpl.java,v 1.7 2005-02-23 17:21:30 acohen Exp $
 $Date: 2005-02-23 17:21:30 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;

/**
 *  An object of this class describes the privileges possessed by a Subject
 * (e.g. a person).
 */

/* These are the columns in the Subject table:
 * 
 * subjectTypeID
 * subjectID
 * name
 * description
 * displayID
 * createDatetime
 * createDbAccount
 * createUserID
 * createContext
 * modifyDatetime
 * modifyDbAccount
 * modifyUserID
 * modifyContext
 * comment
 */

class PrivilegedSubjectImpl implements PrivilegedSubject
{
  private Signet     signet;

  private SubjectKey subjectKey;

  private Subject    subject;

  private Set        assignmentsGranted;

  private boolean    assignmentsGrantedNotYetFetched  = true;

  private Set        assignmentsReceived;

  private boolean    assignmentsReceivedNotYetFetched = true;

  /* Hibernate requires every persistent class to have a default
   * constructor.
   */
  public PrivilegedSubjectImpl()
  {
    this.subjectKey = new SubjectKey();
    this.assignmentsReceived = new HashSet();
    this.assignmentsGranted = new HashSet();
  }

  PrivilegedSubjectImpl(Signet signet, Subject subject)
  {
    this.signet = signet;
    this.subject = subject;
    this.subjectKey = new SubjectKey(subject);
    this.assignmentsReceived = new HashSet();
    this.assignmentsGranted = new HashSet();
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#addAssignmentGranted(edu.internet2.middleware.signet.Assignment)
   */
  void addAssignmentGranted(Assignment assignment)
  {
    this.assignmentsGranted.add(assignment);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#addAssignmentReceived(edu.internet2.middleware.signet.Assignment)
   */
  void addAssignmentReceived(Assignment assignment)
  {
    this.assignmentsReceived.add(assignment);
  }

  /**
   * This method returns true if this SignetSubject has authority to edit
   * the argument Assignment.  Generally, if the Assignment's SignetSubject 
   * matches this SignetSubject, then canEdit would return false, since in
   * most situations it does not make sense to attempt to extend or modify
   * your own authority.
   * @throws SubjectNotFoundException
   */
  public boolean canEdit(Assignment anAssignment)
  {
    // First, check to see if this subject and the grantee are the same.
    // No one, not even the SignetSuperSubject, is allowed to grant
    // privileges to herself.
    if (this.equals(anAssignment.getGrantee()))
    {
      return false;
    }

    // Next, , check to see if this subject is the Signet superSubject.
    // That Subject can grant any privilege to anyone.

    PrivilegedSubject superPSubject;

    try
    {
      superPSubject = this.signet.getSuperPrivilegedSubject();
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }

    if (this.equals(superPSubject))
    {
      return true;
    }

    Set grantableScopes = getGrantableScopes(anAssignment.getFunction());

    Iterator grantableScopesIterator = grantableScopes.iterator();
    while (grantableScopesIterator.hasNext())
    {
      TreeNode grantableScope = (TreeNode) (grantableScopesIterator.next());
      if (grantableScope.equals(anAssignment.getScope())
          || grantableScope.isAncestorOf(anAssignment.getScope()))
      {
        return true;
      }
    }

    return false;
  }

  public String editRefusalExplanation(Assignment refusedAssignment,
      String actor) // 'grantor', 'revoker', etc.
  {
    // First, check to see if this subject and the grantee are the same.
    // No one, not even the SignetSuperSubject, is allowed to grant
    // privileges to herself.
    if (this.equals(refusedAssignment.getGrantee()))
    {
      return ("It is illegal to grant an assignment to oneself,"
          + " or to modify or revoke"
          + " an assignment which is granted to oneself.");
    }

    // Next, , check to see if the grantor is the Signet superSubject.
    // That Subject can grant any privilege to anyone.

    PrivilegedSubject SuperPSubject;

    try
    {
      SuperPSubject = this.signet.getSuperPrivilegedSubject();
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }

    if (this.equals(SuperPSubject))
    {
      return "There is no reason to refuse this request.";
    }

    Set grantableScopes = getGrantableScopes(refusedAssignment.getFunction());

    Iterator grantableScopesIterator = grantableScopes.iterator();
    while (grantableScopesIterator.hasNext())
    {
      TreeNode grantableScope = (TreeNode) (grantableScopesIterator.next());
      if (grantableScope.equals(refusedAssignment.getScope())
          || grantableScope.isAncestorOf(refusedAssignment.getScope()))
      {
        return "There is no reason to refuse this request.";
      }
    }

    if (grantableScopes.size() == 0)
    {
      return "The " + actor
          + " has no grantable privileges in regard to this function.";
    }
    else
    {
      return "The " + actor
          + " has grantable privileges in regard to this function,"
          + " but they do not encompass the scope associated with this"
          + " request.";
    }
  }

  private Collection getGrantableAssignments(Subsystem subsystem)
      throws ObjectNotFoundException
  {
    Collection assignments = new HashSet();

    Iterator iterator = this.getAssignmentsReceived(null, null).iterator();
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());

      if (assignment.isGrantable()
          && assignment.getFunction().getSubsystem().equals(subsystem))
      {
        assignments.add(assignment);
      }
    }

    return assignments;
  }

  private Set getGrantableAssignments(Function function)
      throws ObjectNotFoundException
  {
    Set assignments = new HashSet();

    Iterator iterator = this.getAssignmentsReceived(null, null).iterator();
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());

      if (assignment.isGrantable() && assignment.getFunction().equals(function))
      {
        assignments.add(assignment);
      }
    }

    return assignments;
  }

  public Set getGrantableFunctions(Category category)
      throws ObjectNotFoundException
  {
    try
    {
      // First, check to see if the we are the Signet superSubject.
      // That Subject can grant any function in any category to anyone.
      if (this.equals(this.signet.getSuperPrivilegedSubject()))
      {
        return category.getFunctions();
      }
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }

    Set functions = new HashSet();

    Iterator iterator = this.getAssignmentsReceived(null, null).iterator();
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());
      Function candidateFunction = assignment.getFunction();
      Category candidateCategory = candidateFunction.getCategory();

      if (assignment.isGrantable() && candidateCategory.equals(category))
      {
        functions.add(candidateFunction);
      }
    }

    return UnmodifiableSet.decorate(functions);
  }

  public Set getGrantableSubsystems() throws ObjectNotFoundException
  {
    Set grantableSubsystems = new HashSet();

    try
    {
      // First, check to see if the we are the Signet superSubject.
      // That Subject can grant any privilege in any subsystem to
      // anyone, as long as that subsystem actually contains at
      // least one function, and has a non-empty scope-tree
      // associated with it.
      if (this.equals(this.signet.getSuperPrivilegedSubject()))
      {
        Set allSubsystems = this.signet.getSubsystems();
        Iterator allSubsystemsIterator = allSubsystems.iterator();
        while (allSubsystemsIterator.hasNext())
        {
          Subsystem candidateSubsystem = (Subsystem) (allSubsystemsIterator
              .next());
          Tree tree = candidateSubsystem.getTree();
          if (tree == null)
          {
            // This Subsystem has no Tree, and so none of its Functions
            // can be granted.
            continue;
          }

          if (tree.getRoots().size() == 0)
          {
            // This Tree contains no TreeNodes, and so none of the
            // Functions in this Subsystem can actually be granted.
            continue;
          }

          if (candidateSubsystem.getFunctions().size() == 0)
          {
            // This Subsystem contains no Functions, so there's
            // no granting to be done nohow.
            continue;
          }

          grantableSubsystems.add(candidateSubsystem);
        }
      }
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }

    Iterator iterator = this.getAssignmentsReceived(Status.ACTIVE, null)
        .iterator();
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());
      Function candidateFunction = assignment.getFunction();
      Subsystem candidateSubsystem = candidateFunction.getSubsystem();

      if (assignment.isGrantable())
      {
        grantableSubsystems.add(candidateSubsystem);
      }
    }

    return grantableSubsystems;
  }

  public Set getGrantableScopes(Function aFunction)
  {
    Set grantableScopes = new HashSet();

    try
    {
      // First, check to see if the we are the Signet superSubject.
      // That Subject can grant any function at any scope to anyone.
      if (this.equals(this.signet.getSuperPrivilegedSubject()))
      {
        Tree tree = aFunction.getSubsystem().getTree();

        if (tree != null)
        {
          grantableScopes.addAll(tree.getRoots());
        }
      }
      else
      {
        Set grantableAssignments = getGrantableAssignments(aFunction);
        Iterator grantableAssignmentsIterator = grantableAssignments.iterator();
        while (grantableAssignmentsIterator.hasNext())
        {
          Assignment grantableAssignment = (Assignment) (grantableAssignmentsIterator
              .next());

          grantableScopes.add(grantableAssignment.getScope());
        }
      }
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }

    return UnmodifiableSet.decorate(grantableScopes);
  }

  public Set getGrantableCategories(Subsystem subsystem)
      throws ObjectNotFoundException
  {
    try
    {
      // First, check to see if the we are the Signet superSubject.
      // That Subject can grant any privilege in any category to anyone.
      if (this.equals(this.signet.getSuperPrivilegedSubject()))
      {
        return subsystem.getCategories();
      }
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }

    Set grantableCategories = new HashSet();
    Collection grantableAssignments = getGrantableAssignments(subsystem);
    Iterator assignmentsIterator = grantableAssignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment) (assignmentsIterator.next());
      grantableCategories.add(assignment.getFunction().getCategory());
    }

    return UnmodifiableSet.decorate(grantableCategories);
  }

  /**
   * @return Returns the assignmentsGranted.
   * @throws ObjectNotFoundException
   */
  public Set getAssignmentsGranted(Status status, Subsystem subsystem)
      throws ObjectNotFoundException
  {
    // I really want to handle this purely through Hibernate mappings, but
    // I haven't figured out how yet.

    if (this.assignmentsGrantedNotYetFetched == true)
    {
      if (this.subjectKey.isComplete(this.signet))
      {
        // We have not yet fetched the assignments granted by this
        // PrivilegedSubject from the database. Let's make a copy of
        // whatever in-memory assignments we DO have, because they represent
        // granted-but-not-necessarily-yet-persisted assignments.
        Set inMemoryAssignments = this.assignmentsGranted;
        this.assignmentsGranted = this.signet
            .getAssignmentsByGrantor(this.subjectKey);
        this.assignmentsGranted.addAll(inMemoryAssignments);

        this.assignmentsGrantedNotYetFetched = false;
      }
      else
      {
        throw new SignetRuntimeException(
            "An attempt was made to fetch a set of granted Assignments via a"
                + "PrivilegedSubject using an incomplete SubjectKey: ["
                + this.subjectKey + "]");
      }
    }

    Set resultSet = UnmodifiableSet.decorate(this.assignmentsGranted);
    resultSet = filterAssignments(resultSet, status);
    resultSet = filterAssignments(resultSet, subsystem);

    return resultSet;
  }

  /**
   * @param assignmentsGranted The assignmentsGranted to set.
   */
  void setAssignmentsGranted(Set assignmentsGranted)
  {
    this.assignmentsGranted = assignmentsGranted;
  }

  /**
   * @return Returns the assignmentsReceived.
   * @throws ObjectNotFoundException
   */
  public Set getAssignmentsReceived(Status status, Subsystem subsystem)
      throws ObjectNotFoundException
  {
    // I really want to handle this purely through Hibernate
    // mappings, but I haven't figured out how yet.

    if (this.assignmentsReceivedNotYetFetched == true)
    {
      if (this.subjectKey.isComplete(this.signet))
      {
        // We have not yet fetched the assignments received by this
        // PrivilegedSubject from the database. Let's make a copy of
        // whatever in-memory assignments we DO have, because they represent
        // received-but-not-necessarily-yet-persisted assignments.
        Set unsavedAssignmentsReceived = this.assignmentsReceived;

        this.assignmentsReceived = this.signet
            .getAssignmentsByGrantee(this.subjectKey);
        this.assignmentsReceived.addAll(unsavedAssignmentsReceived);

        this.assignmentsReceivedNotYetFetched = false;
      }
      else
      {
        throw new SignetRuntimeException(
            "An attempt was made to fetch a set of received Assignments via a"
                + "PrivilegedSubject using an incomplete SubjectKey: ["
                + this.subjectKey + "]");
      }
    }

    Set resultSet = UnmodifiableSet.decorate(this.assignmentsReceived);
    resultSet = filterAssignments(resultSet, status);
    resultSet = filterAssignments(resultSet, subsystem);

    return resultSet;
  }

  private Set filterAssignments(Set all, Status status)
  {
    if (status == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment) (iterator.next());
      if (candidate.getStatus().equals(status))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  private Set filterAssignments(Set all, Subsystem subsystem)
      throws ObjectNotFoundException
  {
    if (subsystem == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment) (iterator.next());
      if (candidate.getFunction().getSubsystem().equals(subsystem))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  /**
   * @param assignmentsReceived The assignmentsReceived to set.
   */
  void setAssignmentsReceived(Set assignmentsReceived)
  {
    this.assignmentsReceived = assignmentsReceived;
  }

  /**
   * @param name
   * @return
   */
  public String[] getAttributeValues(String name)
  {
    return this.subject.getAttributeValues(name);
  }

  /**
   * @return
   */
  public String getDescription()
  {
    return this.subject.getDescription();
  }

  /**
   * @return
   * @throws SubjectNotFoundException
   * @throws ObjectNotFoundException
   */
  public String getDisplayId() throws SubjectNotFoundException,
      ObjectNotFoundException
  {
    return this.getSubject().getDisplayId();
  }

  /**
   * @return the Name of the Subject which underlies this PrivilegedSubject.
   */
  public String getName() throws ObjectNotFoundException
  {
    return this.getSubject().getName();
  }

  /**
   * @return
   */
  public SubjectType getSubjectType()
  {
    return this.subjectKey.getSubjectType(this.signet);
  }

  public void setSubjectType(SubjectType subjectType)
  {
    this.subjectKey.setSubjectType(subjectType);
  }

  /**
   * @return
   */
  public String getSubjectTypeId()
  {
    return this.subjectKey.getSubjectType(this.signet).getId();
  }

  public String getSubjectId()
  {
    return this.subjectKey.getSubjectId();
  }

  void setSubjectTypeId(String subjectTypeId)
  {
    try
    {
      this.subjectKey.setSubjectType(this.signet.getSubjectType(subjectTypeId));
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  public String getId()
  {
    return this.subjectKey.getSubjectId();
  }

  public void setId(String id)
  {
    this.subjectKey.setSubjectId(id);
  }

  Subject getSubject() throws ObjectNotFoundException
  {
    if (this.subject == null)
    {
      if (this.subjectKey.isComplete(this.signet))
      {
        try
        {
          this.subject = this.subjectKey.getSubjectType(this.signet)
              .getAdapter().getSubject(
                  this.subjectKey.getSubjectType(this.signet),
                  this.subjectKey.getSubjectId());
        }
        catch (SubjectNotFoundException snfe)
        {
          throw new ObjectNotFoundException(snfe);
        }
      }
      else
      {
        throw new SignetRuntimeException(
            "An attempt was made to fetch a Subject via a PrivilegedSubject"
                + " using an incomplete SubjectKey: [" + this.subjectKey + "]");
      }
    }

    if (this.subject instanceof SubjectImpl)
    {
      ((SubjectImpl) (this.subject)).setSignet(this.signet);
    }

    return this.subject;
  }

//  public Assignment grant
//  	(PrivilegedSubject 	grantee,
//  	 TreeNode 					scope,
//     Function 					function,
//     boolean 						canGrant,
//     boolean 						grantOnly)
//  throws
//  	SignetAuthorityException,
//  	ObjectNotFoundException
//  {
//    return this.grant
//    	(grantee,
//    	 scope,
//    	 function,
//    	 new HashSet(0),
//    	 canGrant,
//    	 grantOnly);
//  }

  public Assignment grant
  	(PrivilegedSubject 	grantee,
  	 TreeNode 					scope,
     Function 					function,
     Set								limitValues,
     boolean 						canGrant,
     boolean 						grantOnly)
  throws
  	SignetAuthorityException,
  	ObjectNotFoundException
  {
    if (function == null)
    {
      throw new IllegalArgumentException
      	("It's illegal to grant an Assignment on a NULL Function.");
    }

    Assignment newAssignment = null;

    newAssignment
    	= new AssignmentImpl
    			(this.signet,
    			 this,
    			 grantee,
    			 scope,
    			 function,
    			 limitValues,
    			 canGrant,
    			 grantOnly);

    this.addAssignmentGranted(newAssignment);
    ((PrivilegedSubjectImpl) grantee).addAssignmentReceived(newAssignment);

    return newAssignment;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (!(obj instanceof PrivilegedSubjectImpl))
    {
      return false;
    }

    PrivilegedSubjectImpl rhs = (PrivilegedSubjectImpl) obj;
    Subject thisSubject = null;
    Subject rhsSubject = null;

    try
    {
      thisSubject = this.getSubject();
      rhsSubject = rhs.getSubject();
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(
          "Unable to fetch some part of the underlying Subject of a PrivilegedSubject",
          onfe);
    }

    return new EqualsBuilder().append(thisSubject, rhsSubject).isEquals();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    Subject thisSubject = null;

    try
    {
      thisSubject = this.getSubject();
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(
          "Unable to fetch some part of the underlying Subject of a PrivilegedSubject",
          onfe);
    }

    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).append(thisSubject).toHashCode();
  }

  /**
   * @return Returns the subjectKey.
   */
  SubjectKey getSubjectKey()
  {
    return this.subjectKey;
  }

  /**
   * @param subjectKey The subjectKey to set.
   */
  void setSubjectKey(SubjectKey subjectKey)
  {
    this.subjectKey = subjectKey;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.subject.Subject#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String name, String value)
  {
    this.subject.addAttribute(name, value);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    Subject subject = null;
    String displayId = null;
    String name = null;
    String typeId = null;
    String id = null;

    try
    {
      displayId = this.getDisplayId();
      name = this.getName();
      typeId = this.getSubjectTypeId();
      id = this.getId();
    }
    catch (SubjectNotFoundException snfe)
    {
      throw new SignetRuntimeException(
          "Unable to fetch the underlying Subject of a PrivilegedSubject", snfe);
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(
          "Unable to fetch some part of the underlying Subject of a PrivilegedSubject",
          onfe);
    }

    return "displayId='" + displayId + "', name='" + name + "', typeId = '"
        + typeId + "', id = '" + id + "'";
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    String thisName = null;
    String otherName = null;

    try
    {
      thisName = this.getName();
      otherName = ((PrivilegedSubject) o).getName();
    }
    catch (ObjectNotFoundException onfe)
    {
      // Well, we can't say they match, that's for sure.
      return -1;
    }

    return thisName.compareToIgnoreCase(otherName);
  }

  /**
   * @param signet2
   */
  void setSignet(Signet signet)
  {
    this.signet = signet;
  }
}