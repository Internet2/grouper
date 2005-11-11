/*--
 $Id: PrivilegedSubjectImpl.java,v 1.34 2005-11-11 00:24:01 acohen Exp $
 $Date: 2005-11-11 00:24:01 $
 
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

class PrivilegedSubjectImpl implements PrivilegedSubject
{
  private Signet     signet;
  
  private Integer    id;
  
  private Subject    subject;

  private String  subjectTypeId;
  private String  subjectId;
  private String  subjectName;
  private String  subjectDescription;

  private Set        assignmentsGranted;
  private Set        assignmentsReceived;
  private Set        proxiesGranted;
  private Set        proxiesReceived;
  
  private PrivilegedSubject actingAs = null;
  
  /* The date and time this entity was last modified. */
  private Date  modifyDatetime;

  /* Hibernate requires every persistent class to have a default
   * constructor.
   */
  public PrivilegedSubjectImpl()
  {
    this.assignmentsReceived = new HashSet();
    this.assignmentsGranted = new HashSet();
  }

  PrivilegedSubjectImpl(Signet signet, Subject subject)
  {
    this.signet = signet;
    this.subject = subject;
    
    this.subjectName = subject.getName();
    this.subjectDescription = subject.getDescription();
    this.subjectId = subject.getId();
    this.subjectTypeId = subject.getType().getName();
    
    this.assignmentsReceived = new HashSet();
    this.assignmentsGranted = new HashSet();
    this.proxiesReceived = new HashSet();
    this.proxiesGranted = new HashSet();
    
    // PrivilegedSubject is the only Signet entity which is automatically
    // persisted, because it's just a record of a query to the Subject adapter.
    signet.save(this);
  }
  
  Signet getSignet()
  {
    return this.signet;
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
  public Decision canEdit
    (Grantable grantableInstance)
  {
    PrivilegedSubject effectiveEditor = this.getEffectiveEditor();
    boolean sufficientScopeFound = false;
    
    // First, check to see if this editor and the grantee are the same.
    // No one, not even the Signet application subject, is allowed to grant
    // privileges to herself.
    if (effectiveEditor.equals(grantableInstance.getGrantee()))
    {
      return new DecisionImpl(false, Reason.SELF, null);
    }
    
    // The Signet application can only do one thing: Grant (or edit) a Proxy to
    // a System Administrator. The Signet Application can never directly
    // grant (or edit) any Assignment to anyone.
    if (this.equals(this.getSignet().getSignetSubject())
        && (grantableInstance instanceof Assignment))
    {
      return new DecisionImpl(false, Reason.CANNOT_USE, null);
    }
    
    if (grantableInstance instanceof Assignment)
    {
      Assignment anAssignment = (Assignment)grantableInstance;

      // If you're going to edit an Assignment while "acting as" someone
      // else, you must hold a "useable" Proxy from that other person.
      if (!(this.equals(effectiveEditor))
          && (!this.canUseProxy
                (effectiveEditor, anAssignment.getFunction().getSubsystem())))
      {
        return new DecisionImpl(false, Reason.CANNOT_USE, null);
      }
      
      // Next, let's see whether or not this Assignment is in a Scope that we can
      // grant this particular Function in, and if so, whether or not this
      // Assignment has any Limit-values that exceed the ones we're allowed to
      // work with in this particular combination of Scope and Function.

      Set grantableScopes
        = this.getGrantableScopes(anAssignment.getFunction());

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
            	= effectiveEditor.getGrantableChoices
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
    
    if (grantableInstance instanceof Proxy)
    {
      Proxy proxy = (Proxy)grantableInstance;
      
      // If you're going to edit a Proxy while "acting as" someone
      // else, you must hold an "extensible" Proxy from that other person.
      if (!(this.equals(effectiveEditor))
          && (!this.canExtendProxy
                (effectiveEditor, proxy.getSubsystem())))
      {
        return new DecisionImpl(false, Reason.CANNOT_EXTEND, null);
      }
      
      // If you're "acting as" no one but yourself, then you can't edit
      // any Proxy that you didn't grant.
      if (this.equals(effectiveEditor) && !(this.equals(proxy.getGrantor())))
      {
        return new DecisionImpl(false, Reason.SCOPE, null);
      }
    }

    // If we've gotten this far, the Grantable object must be editable by this
    // PrivilegedSubject.
    return new DecisionImpl(true, null, null);
  }

//  public String editRefusalExplanation
//  	(Assignment refusedAssignment,
//     String			actor) // 'grantor', 'revoker', etc.
//  {
//    // First, check to see if this subject and the grantee are the same.
//    // No one, not even the SignetSuperSubject, is allowed to grant
//    // privileges to herself.
//    if (this.equals(refusedAssignment.getGrantee()))
//    {
//      return
//      	("It is illegal to grant an assignment to oneself,"
//         + " or to modify or revoke"
//         + " an assignment which is granted to oneself.");
//    }
//
//    // Next, , check to see if the grantor is the Signet superSubject.
//    // That Subject can grant any privilege to anyone.
//
//    PrivilegedSubject SuperPSubject;
//
//    try
//    {
//      SuperPSubject = this.signet.getSuperPrivilegedSubject();
//    }
//    catch (ObjectNotFoundException onfe)
//    {
//      throw new SignetRuntimeException(onfe);
//    }
//
//    if (this.equals(SuperPSubject))
//    {
//      return "There is no reason to refuse this request.";
//    }
//
//    Set grantableScopes = getGrantableScopes(refusedAssignment.getFunction());
//    boolean scopeIsGrantable = false;
//    
//    Iterator grantableScopesIterator = grantableScopes.iterator();
//    while (grantableScopesIterator.hasNext())
//    {
//      TreeNode grantableScope = (TreeNode) (grantableScopesIterator.next());
//      if (grantableScope.equals(refusedAssignment.getScope())
//          || grantableScope.isAncestorOf(refusedAssignment.getScope()))
//      {
//        scopeIsGrantable = true;
//        
//        // This scope is indeed one that we can grant this Function in.
//        // Now, let's see whether or not we're allowed to work all of the
//        // Limit-values in this particular Assignment.
//        Set limitValues = refusedAssignment.getLimitValues();
//        Iterator limitValuesIterator = limitValues.iterator();
//        while (limitValuesIterator.hasNext())
//        {
//          LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
//          Limit limit = limitValue.getLimit();
//          Choice choice = null;
//          
//          try
//          {
//            choice
//            	= limit
//            			.getChoiceSet()
//            				.getChoiceByValue
//            					(limitValue.getValue());
//          }
//          catch (Exception e)
//          {
//            throw new SignetRuntimeException(e);
//          }
//          
//          Set grantableChoices
//          	= this.getGrantableChoices
//          			(refusedAssignment.getFunction(),
//          			 refusedAssignment.getScope(),
//          			 limit);
//          
//          if (grantableChoices.contains(choice) == false)
//          {
//            return
//            	"The "
//              + actor
//              + " has grantable privileges in regard to this function,"
//              + " and they do encompass the scope of this request, but"
//              + " this assignment includes the Limit-value '"
//              + choice.getDisplayValue()
//              + "' for the limit '"
//              + limit.getName()
//              + "', which this "
//              + actor
//              + " does not have sufficient privileges to work with.";
//          } 
//        }
//      }
//    }
//
//    if (grantableScopes.size() == 0)
//    {
//      return "The " + actor
//          + " has no grantable privileges in regard to this function.";
//    }
//    else if (scopeIsGrantable == false)
//    {
//      return "The " + actor
//          + " has grantable privileges in regard to this function,"
//          + " but they do not encompass the scope associated with this"
//          + " request.";
//    }
//    else
//    {
//      return "There is no reason to refuse this request.";
//    }
//  }

  private Collection getGrantableAssignments(Subsystem subsystem)
  {
    Collection assignments = new HashSet();

    Iterator iterator
      = filterAssignments
          (this.getEffectiveEditor().getAssignmentsReceived(), Status.ACTIVE)
                .iterator();
    
    while (iterator.hasNext())
    {
      Assignment assignment = (Assignment) (iterator.next());

      if (assignment.canGrant()
          && ((subsystem == null)
              || assignment.getFunction().getSubsystem().equals(subsystem)))
      {
        assignments.add(assignment);
      }
    }

    return assignments;
  }

  private Set getGrantableAssignments(Function function)
  {
    Set assignments = new HashSet();

    Iterator iterator
      = this
          .getEffectiveEditor()
            .getAssignmentsReceived()
              .iterator();
    
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
    // First, check to see if the we are the Signet superSubject.
    // That Subject can grant any function in any category to anyone.
    if (this.hasSuperSubjectPrivileges(category.getSubsystem()))
    {
      return category.getFunctions();
    }

    Set functions = new HashSet();

    Iterator iterator
      = filterAssignments
          (this.getEffectiveEditor().getAssignmentsReceived(),
           Status.ACTIVE)
         .iterator();
    
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
  
  // First, determine who the effectiveEditor is. That's the PrivilegedSubject
  // we're "acting for", if anyone, or ourself, if we're not "acting for"
  // another.
  //
  // Then, we have two possibilities to consider:
  //
  //  1) We are "acting for" ourself only.
  //
  //      In this case, we can grant a Proxy for any Subsystem, without regard
  //      to any Assignments or Proxies we currently hold.
  //
  //  2) We are "acting for" another.
  //
  //      In this case, we look through the extensible Proxies that we've
  //      received from that other PrivilegedSubject, plucking the Subsystem
  //      from each, keeping in mind that the NULL Subsystem indicates that
  //      we can extend a Proxy for any Subsystem.
  
  public Set getGrantableSubsystemsForProxy()
  {
    Set grantableSubsystems = new HashSet();
    
    if (this.getEffectiveEditor().equals(this))
    {
      grantableSubsystems = this.signet.getSubsystems();
    }
    else
    {
      Set proxiesReceived
        = filterProxies
            (this.getProxiesReceived(), Status.ACTIVE);
      proxiesReceived
        = filterProxiesByGrantor(proxiesReceived, this.getEffectiveEditor());
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();

      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy = (Proxy)(proxiesReceivedIterator.next());
      
        if (proxy.canExtend())
        {
          if (proxy.getSubsystem() == null)
          {
            grantableSubsystems = this.signet.getSubsystems();
          }
          else
          {
            grantableSubsystems.add(proxy.getSubsystem());
          }
        }
      }
    }
    
    return grantableSubsystems;
  }

  // First, determine who the effectiveEditor is. That's the PrivilegedSubject
  // we're "acting for", if anyone, or ourself, if we're not "acting for"
  // another.
  //
  // Then, we have three possibilities to consider:
  //
  //  1) We are "acting for" ourself only.
  //
  //      In this case, we just look through our grantable Assignments,
  //      plucking the Subsystem from each.
  //
  //  2) We are "acting for" the Signet subject.
  //
  //      In this case, we look through our usable Proxies that we've
  //      received from the Signet subject, plucking the Subsystem
  //      from each, keeping in mind that the NULL Subsystem indicates
  //      that we can grant in any Subsystem that's present in the system.
  //
  //  3) We are "acting for" some other, garden-variety subject.
  //
  //      In this case, we look through the usable Proxies that we've
  //      received from that other subject, plucking the Subsystem
  //      from each, keeping in mind that the NULL Subsystem indicates
  //      that we can grant in any Subsystem that our Proxy-grantor
  //      can grant in. This set of Subsystems is our "Proxied Subsystems".
  //
  //      Then, armed with that list of Subsystems, we look through the
  //      grantable Assignments held by our Proxy-grantor, examining the
  //      Subsystem of each. If a grantable Assignment's Subsystem is also
  //      found in our set of Proxyied Subsystems, then we add it to our list
  //      of grantable subsystems.
  //
  public Set getGrantableSubsystemsForAssignment()
  {
    Set grantableSubsystems = new HashSet();
    
    if (this.getEffectiveEditor().equals(this))
    {
      // We are acting for no one but ourselves.
      Collection grantableAssignments
        = this.getGrantableAssignments((Subsystem)null);
      
      Iterator grantableAssignmentsIterator = grantableAssignments.iterator();
      while (grantableAssignmentsIterator.hasNext())
      {
        Assignment grantableAssignment
          = (Assignment)(grantableAssignmentsIterator.next());
        
        grantableSubsystems.add
          (grantableAssignment.getFunction().getSubsystem());
      }
      
      return grantableSubsystems;
    }
    else if (this.getEffectiveEditor().equals(this.getSignet().getSignetSubject()))
    {
      // We are acting for the Signet subject.
      Set proxies = this.getProxiesReceived();
      proxies = filterProxies(proxies, Status.ACTIVE);
      proxies = filterProxiesByGrantor(proxies, this.getSignet().getSignetSubject());
      
      Iterator proxiesIterator = proxies.iterator();
      while (proxiesIterator.hasNext())
      {
        Proxy proxy = (Proxy)(proxiesIterator.next());
        
        if (proxy.canUse())
        {
          if (proxy.getSubsystem() == null)
          {
            Iterator candidates
              = this.signet.getSubsystems().iterator();
           
            while (candidates.hasNext())
            {
              Subsystem candidate = (Subsystem)(candidates.next());
              if (((SubsystemImpl)candidate).isPopulatedForGranting())
              {
                grantableSubsystems.add(candidate);
              }
            }
            grantableSubsystems.addAll(this.signet.getSubsystems());
          }
          else
          {
            if (((SubsystemImpl)(proxy.getSubsystem()))
                  .isPopulatedForGranting())
            {
              grantableSubsystems.add(proxy.getSubsystem());
            }
          }
        }
      }
      
      return grantableSubsystems;
    }
    else
    {
      // We are acting for some other subject who is not the Signet subject.
      Set proxies = this.getProxiesReceived();
      proxies = filterProxies(proxies, Status.ACTIVE);
      proxies = filterProxiesByGrantor(proxies, this.getEffectiveEditor());
      
      Set proxiedSubsystems = new HashSet();
    
      Iterator proxiesIterator = proxies.iterator();
      while (proxiesIterator.hasNext())
      {
        Proxy proxy = (Proxy)(proxiesIterator.next());
      
        if (proxy.canUse())
        {
          if (proxy.getSubsystem() == null)
          {
            // We can grant every subsystem that's grantable by the subject
            // we're "acting for".
            return this.getEffectiveEditor().getGrantableSubsystemsForAssignment();
          }

          proxiedSubsystems.add(proxy.getSubsystem());
        }
      }
      
      // Now that we have the set of Proxied Subsystems, let's get the set of
      // our Proxy-grantor's grantable subsystems, and return the intersection
      // of those sets.
      
      proxiedSubsystems.retainAll
        (this.getEffectiveEditor().getGrantableSubsystemsForAssignment());
      return proxiedSubsystems;
    }
  }

  public Set getGrantableScopes(Function aFunction)
  {
    Set grantableScopes = new HashSet();

    // First, check to see if the we are the Signet superSubject.
    // That Subject can grant any function at any scope to anyone.
    if (this.hasSuperSubjectPrivileges(aFunction.getSubsystem()))
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

    return UnmodifiableSet.decorate(grantableScopes);
  }
  
  private boolean hasSuperSubjectPrivileges(Subsystem subsystem)
  {
    // First, check to see if the we are the Signet superSubject.
    // That Subject can grant any privilege in any category to anyone.
    if (this.getEffectiveEditor().equals(this.getSignet().getSignetSubject()))
    {
      // We're either the SignetSuperSubject or we're "acting as" that
      // esteemed personage. If we're just "acting as", then we need to make
      // sure that our set of active Proxies actually includes this Subystem.
      
      Set proxies = this.getProxiesReceived();
      proxies = filterProxies(proxies, Status.ACTIVE);
      proxies = filterProxies(proxies, subsystem);
      proxies = filterProxiesByGrantor(proxies, this.getSignet().getSignetSubject());
      
      if ((this.equals(this.getSignet().getSignetSubject())) || (proxies.size() > 0))
      {
        return true;
      }
    }
      
    return false;
  }

  public Set getGrantableCategories(Subsystem subsystem)
  {
    if (hasSuperSubjectPrivileges(subsystem))
    {
      return subsystem.getCategories();
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
  public Set getAssignmentsGranted()
  {
    // Make sure that all of the members of this Set have Signet instances.
    Iterator iterator = this.assignmentsGranted.iterator();
    while (iterator.hasNext())
    {
      AssignmentImpl assignment = (AssignmentImpl)(iterator.next());
      assignment.setSignet(this.getSignet());
    }
    
    return this.assignmentsGranted;
  }
  
  public Set getProxiesGranted()
  {
    // Make sure that all of the members of this Set have Signet instances.
    Iterator iterator = this.proxiesGranted.iterator();
    while (iterator.hasNext())
    {
      ProxyImpl proxy = (ProxyImpl)(iterator.next());
      proxy.setSignet(this.getSignet());
    }
    
    return this.proxiesGranted;
  }

  /**
   * @param assignmentsGranted The assignmentsGranted to set.
   */
  void setAssignmentsGranted(Set assignmentsGranted)
  {
    this.assignmentsGranted = assignmentsGranted;
  }
  
  // This method is for use only by Hibernate.
  private void setProxiesGranted(Set proxies)
  {
    this.proxiesGranted = proxies;
  }
  
  // This method is for use only by Hibernate.
  private void setProxiesReceived(Set proxies)
  {
    this.proxiesReceived = proxies;
  }

  /**
   * @return Returns the assignmentsReceived.
   * @throws ObjectNotFoundException
   */
  public Set getAssignmentsReceived()
  {
    // Make sure that all of the members of this Set have Signet instances.
    Iterator iterator = this.assignmentsReceived.iterator();
    while (iterator.hasNext())
    {
      AssignmentImpl assignment = (AssignmentImpl)(iterator.next());
      assignment.setSignet(this.getSignet());
    }
    
    return this.assignmentsReceived;
  }

  public Set getProxiesReceived()
  {
    // Make sure that all of the members of this Set have Signet instances.
    Iterator iterator = this.proxiesReceived.iterator();
    while (iterator.hasNext())
    {
      ProxyImpl proxy = (ProxyImpl)(iterator.next());
      proxy.setSignet(this.getSignet());
    }
    
    return this.proxiesReceived;
  }
  
  static Set filterAssignments(Set all, Status status)
  {
    Set statusSet = new HashSet();
    statusSet.add(status);
    return filterAssignments(all, statusSet);
  }
  
  static Set filterProxies(Set all, Status status)
  {
    Set statusSet = new HashSet();
    statusSet.add(status);
    return filterProxies(all, statusSet);
  }

  static Set filterAssignments(Set all, Set statusSet)
  {
    if (statusSet == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Assignment candidate = (Assignment) (iterator.next());
      if (statusSet.contains(candidate.getStatus()))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  static Set filterProxies(Set all, Set statusSet)
  {
    if (statusSet == null)
    {
      return all;
    }

    Set subset = new HashSet();
    Iterator iterator = all.iterator();
    while (iterator.hasNext())
    {
      Proxy candidate = (Proxy) (iterator.next());
      if (statusSet.contains(candidate.getStatus()))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }
  
  static Set filterAssignmentsByGrantee
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
  
//  private Set filterAssignmentsByGrantor
//    (Set                all,
//     PrivilegedSubject  grantor)
//  {
//    if (grantor == null)
//    {
//      return all;
//    }
//    
//    Set subset = new HashSet();
//    Iterator iterator = all.iterator();
//    while (iterator.hasNext())
//    {
//      Assignment candidate = (Assignment)(iterator.next());
//      if (candidate.getGrantor().equals(grantor))
//      {
//        subset.add(candidate);
//      }
//    }
//    
//    return subset;
//  }
  
  static Set filterProxiesByGrantee
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
  
  static Set filterProxiesByGrantor
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

  static Set filterAssignments(Set all, Subsystem subsystem)
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

  static Set filterProxies(Set all, Subsystem subsystem)
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
      if ((candidate.getSubsystem() == null)
          || candidate.getSubsystem().equals(subsystem))
      {
        subset.add(candidate);
      }
    }

    return subset;
  }

  static Set filterAssignments(Set all, Function function)
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
  public Subject getSubject()
  throws ObjectNotFoundException
  {
    if (this.subject != null)
    {
      return this.subject;
    }
    
    return this.getSignet().getSubject(this.subjectTypeId, this.subjectId);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getSubjectId()
   */
  public String getSubjectId()
  {
  	return subjectId;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getSubjectTypeId()
   */
  public String getSubjectTypeId()
  {
    return subjectTypeId;
  }
  
  void setSubjectId(String subjectId)
  {
    this.subjectId = subjectId;
  }
  
  void setSubjectTypeId(String subjectTypeId)
  {
    this.subjectTypeId = subjectTypeId;
  }
  
  void setSubject(Subject subject)
  {
    this.subject = subject;
    this.subjectId = subject.getId();
    this.subjectTypeId = subject.getType().getName();
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getName()
   */
  public String getName()
  {
    return this.subjectName;
  }
  
  // This method is for use only by Hibernate.
  private void setName(String name)
  {
    this.subjectName = name;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.PrivilegedSubject#getDescription()
   */
  public String getDescription()
  {
    return this.subjectDescription;
  }
  
  // This method is for use only by Hibernate.
  private void setDescription(String description)
  {
    this.subjectDescription = description;
  }
  
  public Assignment grant
    (PrivilegedSubject grantee,
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
           grantee,
           scope,
           function,
           limitValues,
           canUse,
           canGrant,
           effectiveDate,
           expirationDate);


    ((PrivilegedSubjectImpl)(this.getEffectiveEditor()))
      .addAssignmentGranted(newAssignment);
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
    String name = this.getName();
    String typeId = this.getSubjectTypeId();
    String id = this.getSubjectId();

    return "[name='" + name + "',typeId ='" + typeId + "',id ='" + id + "']";
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    String thisName = null;
    String otherName = null;

    thisName = this.getName();
    otherName = ((PrivilegedSubjectImpl) o).getName();
    
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
    // First, check to see if the we are the Signet superSubject.
    // That Subject can grant any Choice in any Limit in any Function in any
    // scope to anyone.
    if (this.hasSuperSubjectPrivileges(function.getSubsystem()))
    {
        return
          UnmodifiableSet.decorate
            (limit.getChoiceSet().getChoices());
    }
    
    // We're not the SignetSuperSubject, so let's find out what Limit-values
    // we've been assigned in relation to this Function, scope, and Limit.
    
    Set receivedLimitChoices = new HashSet();

    Iterator assignmentsReceivedIterator;

    Set assignments = this.getAssignmentsReceived();
    assignments = filterAssignments(assignments, Status.ACTIVE);
    assignments = filterAssignments(assignments, function.getSubsystem());
    assignments = filterAssignments(assignments, function);
    assignmentsReceivedIterator = assignments.iterator();
    
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
    
    Set assignments = this.getAssignmentsReceived();
    assignments = filterAssignments(assignments, Status.ACTIVE);
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
    (PrivilegedSubject  grantee,
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
           grantee,
           subsystem,
           canUse,
           canExtend,
           effectiveDate,
           expirationDate);

    ((PrivilegedSubjectImpl)(this.getEffectiveEditor()))
      .addProxyGranted(newProxy);
    ((PrivilegedSubjectImpl) grantee).addProxyReceived(newProxy);

    return newProxy;
  }

  protected boolean canActAs
    (PrivilegedSubject  actingAs,
     Subsystem          subsystem)
  {
    Set proxies = this.getProxiesReceived();
    proxies = filterProxies(proxies, Status.ACTIVE);
    proxies = filterProxies(proxies, subsystem);
    proxies = filterProxiesByGrantor(proxies, actingAs);
    
    return (proxies.size() > 0);
  }
  
  boolean hasUsableProxy
    (PrivilegedSubject  fromGrantor,
     Subsystem          subsystem,
     Reason[]           returnReason)
  {
    Set candidates = this.getProxiesReceived();
    candidates = filterProxies(candidates, Status.ACTIVE);
    candidates = filterProxies(candidates, subsystem);
    candidates = filterProxiesByGrantor(candidates, fromGrantor);
    
    if (candidates.size() == 0)
    {
      returnReason[0] = Reason.NO_PROXY;
      return false;
    }
    
    Iterator candidatesIterator = candidates.iterator();
    while (candidatesIterator.hasNext())
    {
      Proxy candidate = (Proxy)(candidatesIterator.next());
      if (candidate.canUse())
      {
        return true;
      }
    }
    
    // If we've gotten this far, none of the Proxies we hold for this Subsystem
    // can be used directly.
    returnReason[0] = Reason.CANNOT_USE;
    return false;
  }
  
  boolean hasExtensibleProxy
    (PrivilegedSubject  pSubject,
     Subsystem          subsystem,
     Reason[]           returnReason)
  {
    Set candidates = this.getProxiesReceived();
    candidates = filterProxies(candidates, Status.ACTIVE);
    candidates = filterProxies(candidates, subsystem);
    candidates
      = filterProxiesByGrantor(candidates, pSubject.getEffectiveEditor());
    
    if (candidates.size() == 0)
    {
      returnReason[0] = Reason.NO_PROXY;
      return false;
    }
    
    Iterator candidatesIterator = candidates.iterator();
    while (candidatesIterator.hasNext())
    {
      Proxy candidate = (Proxy)(candidatesIterator.next());
      if (candidate.canExtend())
      {
        return true;
      }
    }
    
    // If we've gotten this far, none of the Proxies we hold for this Subsystem
    // can be extended.
    returnReason[0] = Reason.CANNOT_EXTEND;
    return false;
  }
  
  public void setActingAs(PrivilegedSubject actingAs)
  throws SignetAuthorityException
  {
    if (this.canActAs(actingAs, null))
    {
      this.actingAs = actingAs;
    }
    else
    {
      throw new SignetAuthorityException
        (new DecisionImpl(false, Reason.NO_PROXY, null));
    }
  }
  
  public PrivilegedSubject getEffectiveEditor()
  {
    return (actingAs == null ? this : actingAs);
  }
  
  /*
   * This method returns true if this PrivilegedSubject holds any "useable"
   * proxies from the specified grantor. A "useable" proxy is one that can be
   * be used(!) to grant Assignments. A proxy that is not "useable" can be
   * extended to another person, but may not be used to grant Assignments.
   */
  boolean canUseProxy
    (PrivilegedSubject proxyGrantor, Subsystem subsystem)
  {
    Set proxies = this.getProxiesReceived();
    proxies = filterProxies(proxies, Status.ACTIVE);
    proxies = filterProxies(proxies, subsystem);
    proxies = filterProxiesByGrantor(proxies, proxyGrantor);
    
    Iterator proxiesIterator = proxies.iterator();
    while (proxiesIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesIterator.next());
      if (proxy.canUse())
      {
        return true;
      }
    }
    
    // If we've gotten this far, it means we have no useable, active Proxies
    // from ProxyGrantor that are applicable to the specified Subsystem.
    return false;
  }
  
  /*
   * This method returns true if this PrivilegedSubject holds any "extensible"
   * proxies from the specified grantor. An "extensible" proxy is one that can
   * be used to grant Proxies. A proxy that is not "extensible" can be
   * used to grant Assignments, but may not be used to extend Proxies.
   */
  boolean canExtendProxy
    (PrivilegedSubject proxyGrantor, Subsystem subsystem)
  {
    Set proxies = this.getProxiesReceived();
    proxies = filterProxies(proxies, Status.ACTIVE);
    proxies = filterProxies(proxies, subsystem);
    proxies = filterProxiesByGrantor(proxies, proxyGrantor);
    
    Iterator proxiesIterator = proxies.iterator();
    while (proxiesIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesIterator.next());
      if (proxy.canExtend())
      {
        return true;
      }
    }
    
    // If we've gotten this far, it means we have no useable, active Proxies
    // from ProxyGrantor that are applicable to the specified Subsystem.
    return false;
  }
  
  public Integer getId()
  {
    return this.id;
  }
  
  void setId(Integer id)
  {
    this.id = id;
  }
  
  /**
   * @return Returns the date and time this entity was last modified.
   */
  public final Date getModifyDatetime()
  {
    return this.modifyDatetime;
  }
  
  /**
   * @param modifyDatetime The modifyDatetime to set.
   */
  final void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }
}