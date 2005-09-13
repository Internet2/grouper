/*--
 $Id: PrivilegedSubjectImpl.java,v 1.24 2005-09-13 22:25:36 acohen Exp $
 $Date: 2005-09-13 22:25:36 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceNotFoundException;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

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

  private SubjectKey	subjectKey;
  
  private Subject    subject;

  private Set        assignmentsGranted;
  private boolean    assignmentsGrantedNotYetFetched  = true;

  private Set        assignmentsReceived;
  private boolean    assignmentsReceivedNotYetFetched = true;

  private Set        proxiesGranted;
  private boolean    proxiesGrantedNotYetFetched  = true;

  private Set        proxiesReceived;
  private boolean    proxiesReceivedNotYetFetched = true;

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
    this.proxiesReceived = new HashSet();
    this.proxiesGranted = new HashSet();
  }


  void addAssignmentGranted(Assignment assignment)
  {
    this.assignmentsGranted.add(assignment);
  }

  void addAssignmentReceived(Assignment assignment)
  {
    this.assignmentsReceived.add(assignment);
  }
  
  void addProxyGranted(Proxy proxy)
  {
    this.proxiesGranted.add(proxy);
  }

  void addProxyReceived(Proxy proxy)
  {
    this.proxiesReceived.add(proxy);
  }

  /**
   * This method returns true if this SignetSubject has authority to edit
   * the argument Assignment.  Generally, if the Assignment's SignetSubject 
   * matches this SignetSubject, then canEdit would return false, since in
   * most situations it does not make sense to attempt to extend or modify
   * your own authority.
   * @throws SubjectNotFoundException
   * 
   * @TODO: This method must also check to see if all Limit-values in the
   * current Assignment are grantable by by this PrivilegedSubject. If ANY
   * of those Limit-values are beyond the capability of this PrivilegedSubject's
   * granting abilities, then this Assignment is not editable by this
   * PrivilegedSubject.
   */
  public Decision canEdit(Grantable grantableInstance)
  {
    boolean sufficientScopeFound = false;
    
    // First, check to see if this subject and the grantee are the same.
    // No one, not even the SignetSuperSubject, is allowed to grant
    // privileges to herself.
    if (this.equals(grantableInstance.getGrantee()))
    {
      return new DecisionImpl(false, Reason.SELF, null);
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
      return new DecisionImpl(true, null, null);
    }
    
    if (grantableInstance instanceof Assignment)
    {
      Assignment anAssignment = (Assignment)grantableInstance;
      
      // Next, let's see whether or not this Assignment is in a Scope that we can
      // grant this particular Function in, and if so, whether or not this
      // Assignment has any Limit-values that exceed the ones we're allowed to
      // work with in this particular combination of Scope and Function.

      Set grantableScopes = getGrantableScopes(anAssignment.getFunction());

      Iterator grantableScopesIterator = grantableScopes.iterator();
      while (grantableScopesIterator.hasNext())
      {
        TreeNode grantableScope = (TreeNode) (grantableScopesIterator.next());
        if (grantableScope.equals(anAssignment.getScope())
            || grantableScope.isAncestorOf(anAssignment.getScope()))
        {
          sufficientScopeFound = true;
        
          // This scope is indeed one that we can grant this Function in.
          // Now, let's see whether or not we're allowed to work with all of the
          // Limit-values in this particular Assignment.
          Set limitValues = anAssignment.getLimitValues();
          Iterator limitValuesIterator = limitValues.iterator();
          while (limitValuesIterator.hasNext())
          {
            LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
            Limit limit = limitValue.getLimit();
            Choice choice = null;
          
            try
            {
              choice
              	= limit
              			.getChoiceSet()
              				.getChoiceByValue
              					(limitValue.getValue());
            }
            catch (Exception e)
            {
              throw new SignetRuntimeException(e);
            }
          
            Set grantableChoices
            	= this.getGrantableChoices
            			(anAssignment.getFunction(), anAssignment.getScope(), limit);
            
            if (grantableChoices.contains(choice) == false)
            {
              return new DecisionImpl(false, Reason.LIMIT, limit);
            } 
          }
        }
      }
    
      if (sufficientScopeFound == false)
      {
        // None of our grantable Scopes were high and mighty enough to edit
        // this Assignment.
        return new DecisionImpl(false, Reason.SCOPE, null);
      }
    }

    // If we've gotten this far, the Grantable object must be editable by this
    // PrivilegedSubject.
    return new DecisionImpl(true, null, null);
  }

  public String editRefusalExplanation
  	(Assignment refusedAssignment,
     String			actor) // 'grantor', 'revoker', etc.
  {
    // First, check to see if this subject and the grantee are the same.
    // No one, not even the SignetSuperSubject, is allowed to grant
    // privileges to herself.
    if (this.equals(refusedAssignment.getGrantee()))
    {
      return
      	("It is illegal to grant an assignment to oneself,"
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
    boolean scopeIsGrantable = false;
    
    Iterator grantableScopesIterator = grantableScopes.iterator();
    while (grantableScopesIterator.hasNext())
    {
      TreeNode grantableScope = (TreeNode) (grantableScopesIterator.next());
      if (grantableScope.equals(refusedAssignment.getScope())
          || grantableScope.isAncestorOf(refusedAssignment.getScope()))
      {
        scopeIsGrantable = true;
        
        // This scope is indeed one that we can grant this Function in.
        // Now, let's see whether or not we're allowed to work all of the
        // Limit-values in this particular Assignment.
        Set limitValues = refusedAssignment.getLimitValues();
        Iterator limitValuesIterator = limitValues.iterator();
        while (limitValuesIterator.hasNext())
        {
          LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
          Limit limit = limitValue.getLimit();
          Choice choice = null;
          
          try
          {
            choice
            	= limit
            			.getChoiceSet()
            				.getChoiceByValue
            					(limitValue.getValue());
          }
          catch (Exception e)
          {
            throw new SignetRuntimeException(e);
          }
          
          Set grantableChoices
          	= this.getGrantableChoices
          			(refusedAssignment.getFunction(),
          			 refusedAssignment.getScope(),
          			 limit);
          
          if (grantableChoices.contains(choice) == false)
          {
            return
            	"The "
              + actor
              + " has grantable privileges in regard to this function,"
              + " and they do encompass the scope of this request, but"
              + " this assignment includes the Limit-value '"
              + choice.getDisplayValue()
              + "' for the limit '"
              + limit.getName()
              + "', which this "
              + actor
              + " does not have sufficient privileges to work with.";
          } 
        }
      }
    }

    if (grantableScopes.size() == 0)
    {
      return "The " + actor
          + " has no grantable privileges in regard to this function.";
    }
    else if (scopeIsGrantable == false)
    {
      return "The " + actor
          + " has grantable privileges in regard to this function,"
          + " but they do not encompass the scope associated with this"
          + " request.";
    }
    else
    {
      return "There is no reason to refuse this request.";
    }
  }

  private Collection getGrantableAssignments(Subsystem subsystem)
  {
    Collection assignments = new HashSet();

    Iterator iterator = this.getAssignmentsReceived(null, null, null).iterator();
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());

      if (assignment.canGrant()
          && assignment.getFunction().getSubsystem().equals(subsystem))
      {
        assignments.add(assignment);
      }
    }

    return assignments;
  }

  private Set getGrantableAssignments(Function function)
  {
    Set assignments = new HashSet();

    Iterator iterator = this.getAssignmentsReceived(null, null, null).iterator();
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());

      if (assignment.canGrant() && assignment.getFunction().equals(function))
      {
        assignments.add(assignment);
      }
    }

    return assignments;
  }

  public Set getGrantableFunctions(Category category)
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

    Iterator iterator = this.getAssignmentsReceived(null, null, null).iterator();
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());
      Function candidateFunction = assignment.getFunction();
      Category candidateCategory = candidateFunction.getCategory();

      if (assignment.canGrant() && candidateCategory.equals(category))
      {
        functions.add(candidateFunction);
      }
    }

    return UnmodifiableSet.decorate(functions);
  }

  public Set getGrantableSubsystems()
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

    Iterator iterator
      = this.getAssignmentsReceived(Status.ACTIVE, null, null)
          .iterator();
    
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());
      Function candidateFunction = assignment.getFunction();
      Subsystem candidateSubsystem = candidateFunction.getSubsystem();

      if (assignment.canGrant())
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
  public Set getAssignmentsGranted
    (Status             status,
     Subsystem          subsystem,
     PrivilegedSubject  grantee)
  {
    // I really want to handle this purely through Hibernate mappings, but
    // I haven't figured out how yet. When we switch to a persistent
    // PrivilegedSubject with its own synthetic, simple ID, this code will
    // be simplified considerably.

    if (this.assignmentsGrantedNotYetFetched == true)
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

    Set resultSet = UnmodifiableSet.decorate(this.assignmentsGranted);
    resultSet = filterAssignments(resultSet, status);
    resultSet = filterAssignments(resultSet, subsystem);
    resultSet = filterAssignmentsByGrantee(resultSet, grantee);

    return resultSet;
  }
  
  public Set getProxiesGranted
    (Status             status,
     Subsystem          subsystem,
     PrivilegedSubject  grantee)
  {
    // I really want to handle this purely through Hibernate mappings, but
    // I haven't figured out how yet. When we switch to a persistent
    // PrivilegedSubject with its own synthetic, simple ID, this code will
    // be simplified considerably.

    if (this.proxiesGrantedNotYetFetched == true)
    {
        // We have not yet fetched the Proxies granted by this
        // PrivilegedSubject from the database. Let's make a copy of
        // whatever in-memory Proxies we DO have, because they represent
        // granted-but-not-necessarily-yet-persisted Proxies.
        Set inMemoryProxies = this.proxiesGranted;
        this.proxiesGranted = this.signet
            .getProxiesByGrantor(this.subjectKey);
        this.proxiesGranted.addAll(inMemoryProxies);

        this.proxiesGrantedNotYetFetched = false;
    }

    Set resultSet = UnmodifiableSet.decorate(this.proxiesGranted);
    resultSet = filterProxies(resultSet, status);
    resultSet = filterProxies(resultSet, subsystem);
    resultSet = filterProxiesByGrantee(resultSet, grantee);

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
  public Set getAssignmentsReceived
    (Status status, Subsystem subsystem, Function function)
  {
    // I really want to handle this purely through Hibernate
    // mappings, but I haven't figured out how yet.

    if (this.assignmentsReceivedNotYetFetched == true)
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

    Set resultSet = UnmodifiableSet.decorate(this.assignmentsReceived);
    resultSet = filterAssignments(resultSet, status);
    resultSet = filterAssignments(resultSet, subsystem);
    resultSet = filterAssignments(resultSet, function);

    return resultSet;
  }

  public Set getProxiesReceived
    (Status status, Subsystem subsystem, PrivilegedSubject grantor)
  {
    // I really want to handle this purely through Hibernate
    // mappings, but I haven't figured out how yet. This method will be
    // considerably simplified when the PrivilegedSubject table, which will
    // use a simple synthetic key, is re-instated.

    if (this.proxiesReceivedNotYetFetched == true)
    {
        // We have not yet fetched the proxies received by this
        // PrivilegedSubject from the database. Let's make a copy of
        // whatever in-memory proxies we DO have, because they represent
        // received-but-not-necessarily-yet-persisted proxies.
        Set unsavedProxiesReceived = this.proxiesReceived;

        this.proxiesReceived
          = this.signet.getProxiesByGrantee(this.subjectKey);
        this.proxiesReceived.addAll(unsavedProxiesReceived);

        this.proxiesReceivedNotYetFetched = false;
    }

    Set resultSet = UnmodifiableSet.decorate(this.proxiesReceived);
    resultSet = filterProxies(resultSet, status);
    resultSet = filterProxies(resultSet, subsystem);
    resultSet = filterProxiesByGrantor(resultSet, grantor);

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

  private Set filterProxies(Set all, Status status)
  {
    if (status == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Proxy candidate = (Proxy) (iterator.next());
      if (candidate.getStatus().equals(status))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }
  
  private Set filterAssignmentsByGrantee
    (Set                all,
     PrivilegedSubject  grantee)
  {
    if (grantee == null)
    {
      return all;
    }
    
    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment)(iterator.next());
      if (candidate.getGrantee().equals(grantee))
      {
        subset.add(candidate);
      }
    }
    
    return subset;
  }
  
  private Set filterAssignmentsByGrantor
    (Set                all,
     PrivilegedSubject  grantor)
  {
    if (grantor == null)
    {
      return all;
    }
    
    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment)(iterator.next());
      if (candidate.getGrantor().equals(grantor))
      {
        subset.add(candidate);
      }
    }
    
    return subset;
  }
  
  private Set filterProxiesByGrantee
    (Set                all,
     PrivilegedSubject  grantee)
  {
    if (grantee == null)
    {
      return all;
    }
    
    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Proxy candidate = (Proxy)(iterator.next());
      if (candidate.getGrantee().equals(grantee))
      {
        subset.add(candidate);
      }
    }
    
    return subset;
  }
  
  private Set filterProxiesByGrantor
    (Set                all,
     PrivilegedSubject  grantor)
  {
    if (grantor == null)
    {
      return all;
    }
    
    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Proxy candidate = (Proxy)(iterator.next());
      if (candidate.getGrantor().equals(grantor))
      {
        subset.add(candidate);
      }
    }
    
    return subset;
  }

  private Set filterAssignments(Set all, Subsystem subsystem)
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

  private Set filterProxies(Set all, Subsystem subsystem)
  {
    if (subsystem == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Proxy candidate = (Proxy) (iterator.next());
      if (candidate.getSubsystem().equals(subsystem))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  private Set filterAssignments(Set all, Function function)
  {
    if (function == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment) (iterator.next());
      if (candidate.getFunction().equals(function))
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

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getSubject()
   */
  public Subject getSubject() {
    return this.subject;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getSubjectId()
   */
  public String getSubjectId()
  {
    String subjectId;
    
    if (this.subject == null)
    {
      subjectId = null;
    }
    else
    {
      subjectId = this.subject.getId();
    }
    
  	return subjectId;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getSubjectTypeId()
   */
  public String getSubjectTypeId()
  {
    if (this.subject == null)
    {
      return null;
    }

  	return this.subject.getType().getName();
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getName()
   */
  public String getName() {
    return this.subject.getName();
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getDescription()
   */
  public String getDescription() {
    return this.subject.getDescription();
  }
  
  public Assignment grant
    (PrivilegedSubject actingAs,
     PrivilegedSubject grantee,
     TreeNode          scope,
     Function          function,
     Set               limitValues,
     boolean           canUse,
     boolean           canGrant,
     Date              effectiveDate,
     Date              expirationDate)
  throws
    SignetAuthorityException
  {
    Assignment newAssignment
      = new AssignmentImpl
          (this.signet,
           this,
           actingAs,
           grantee,
           scope,
           function,
           limitValues,
           canUse,
           canGrant,
           effectiveDate,
           expirationDate);

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

    return new EqualsBuilder()
		.append(this.getSubjectId(), rhs.getSubjectId())
		.append(this.getSubjectTypeId(), rhs.getSubjectTypeId())
		.isEquals();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37)
		.append(this.getSubjectId())
		.append(this.getSubjectTypeId())
		.toHashCode();
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    String name = this.subject.getName();
    String typeId = this.subject.getType().getName();
    String id = this.subject.getId();

    return "[name='" + name + "',typeId ='" + typeId + "',id ='" + id + "']";
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    String thisName = null;
    String otherName = null;

    thisName = this.subject.getName();
    otherName = ((PrivilegedSubject) o).getSubject().getName();

    return thisName.compareToIgnoreCase(otherName);
  }

  /**
   * @param signet2
   */
  void setSignet(Signet signet)
  {
    this.signet = signet;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getGrantableChoices(edu.internet2.middleware.signet.Function, edu.internet2.middleware.signet.tree.TreeNode, edu.internet2.middleware.signet.Limit)
   */
  public Set getGrantableChoices
    (Function function,
     TreeNode scope,
     Limit    limit)
  {
    try
    {
      // First, check to see if the we are the Signet superSubject.
      // That Subject can grant any Choice in any Limit in any Function in any
      // scope to anyone.
      if (this.equals(this.signet.getSuperPrivilegedSubject()))
      {
          return
            UnmodifiableSet.decorate
              (limit.getChoiceSet().getChoices());
      }
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new SignetRuntimeException(onfe);
    }
    
    // We're not the SignetSuperSubject, so let's find out what Limit-values
    // we've been assigned in relation to this Function, scope, and Limit.
    
    Set receivedLimitChoices = new HashSet();

    Iterator assignmentsReceivedIterator;

    assignmentsReceivedIterator = this.getAssignmentsReceived
      (Status.ACTIVE, function.getSubsystem(), function)
       .iterator();
    
    while (assignmentsReceivedIterator.hasNext())
    {
      Assignment assignmentReceived
      	= (Assignment) (assignmentsReceivedIterator.next());
      
      if (assignmentReceived.getScope().equals(scope)
          || assignmentReceived.getScope().isAncestorOf(scope))
      {
        Set limitValuesReceived
          = assignmentReceived.getLimitValues();

        Iterator limitValuesReceivedIterator = limitValuesReceived.iterator();
        while (limitValuesReceivedIterator.hasNext())
        {
          LimitValue limitValue
            = (LimitValue)(limitValuesReceivedIterator.next());
          
          if (limitValue.getLimit().equals(limit))
          {
            try
            {
              receivedLimitChoices.add
                (limit.getChoiceSet()
                  .getChoiceByValue
                    (limitValue.getValue()));
            }
            catch (ChoiceNotFoundException cnfe)
            {
              throw new SignetRuntimeException(cnfe);
            }
          }
        }
      }
    }
    
    // Now that we've discovered which Limit-values we've been assigned, let's
    // use that information to discover the whole set of Limit-values that we
    // could possibly assign to others. In the case of a multiple-select, we
    // could grant any of the values that were granted to us. In the case of
    // a single-select, we could grant any of the values that were of equal
    // or lesser rank than the greatest value that was granted to us.
    
    Set grantableChoices = new HashSet();
    Set allChoices = limit.getChoiceSet().getChoices();
    
    Iterator allChoicesIterator = allChoices.iterator();
    while (allChoicesIterator.hasNext())
    {
      Choice candidate = (Choice)(allChoicesIterator.next());
      
      if (limit.getSelectionType().equals(SelectionType.SINGLE))
      {
        if (doesNotExceed(candidate, receivedLimitChoices))
        {
          grantableChoices.add(candidate);
        }
      }
      else if (limit.getSelectionType().equals(SelectionType.MULTIPLE))
      {
        if (receivedLimitChoices.contains(candidate))
        {
          grantableChoices.add(candidate);
        }
      }
      else
      {
        throw new SignetRuntimeException
          ("Unexpected selection-type '"
           + limit.getSelectionType()
           + "' encountered in PrivilegedSubject.getGrantableChoices().");
      }
    }

    return UnmodifiableSet.decorate(grantableChoices);
  }

  /**
   * @param choice The Choice to be evaluated.
   * @param choices The Set of Choices to compare the Choice against.
   * @return true if the rank of the specified Choice does not exceed the
   *         rank of the highest-ranking Choice in the Set, and false
   *         otherwise.
   */
  private boolean doesNotExceed(Choice choice, Set choices)
  {
    Iterator choicesIterator = choices.iterator();
    while (choicesIterator.hasNext())
    {
      Choice choiceInSet = (Choice)(choicesIterator.next());
      if (choice.getRank() > choiceInSet.getRank())
      {
        return false; // We've exceeded one of the Choices in the Set.
      }
    }
    
    // If we've gotten this far, then we must not have exceeded any of the
    // Choices in the Set.
    return true;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getPrivileges()
   */
  public Set getPrivileges()
  {
    Set privileges = new HashSet();
    
    Set assignments = this.getAssignmentsReceived(Status.ACTIVE, null, null);
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
      Set assignmentPrivileges = PrivilegeImpl.getPrivileges(assignment);
      privileges.addAll(assignmentPrivileges);
    }
    
    return privileges;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#grantProxy(edu.internet2.middleware.signet.PrivilegedSubject, edu.internet2.middleware.signet.Subsystem, java.util.Date, java.util.Date)
   */
  public Proxy grantProxy
    (PrivilegedSubject  actingAs,
     PrivilegedSubject  grantee,
     Subsystem          subsystem,
     boolean            canUse,
     boolean            canExtend,
     Date               effectiveDate,
     Date               expirationDate)
  throws SignetAuthorityException
  {
    if (grantee == null)
    {
      throw new IllegalArgumentException
        ("It's illegal to grant a Proxy to a NULL grantee.");
    }

    Proxy newProxy = null;

    newProxy
      = new ProxyImpl
          (this.signet,
           this,
           actingAs,
           grantee,
           subsystem,
           canUse,
           canExtend,
           effectiveDate,
           expirationDate);

    this.addProxyGranted(newProxy);
    ((PrivilegedSubjectImpl) grantee).addProxyReceived(newProxy);

    return newProxy;
  }
}