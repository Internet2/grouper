/*--
 $Id: AssignmentImpl.java,v 1.27 2005-08-25 20:31:35 acohen Exp $
 $Date: 2005-08-25 20:31:35 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import edu.internet2.middleware.signet.tree.TreeNode;

class AssignmentImpl
extends GrantableImpl
implements Assignment
{
  private TreeNode					scope;
  private FunctionImpl			function;
  private Set								limitValues;
  private boolean						canGrant;
  private boolean						canUse;

  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public AssignmentImpl()
  {
    super();
    this.limitValues = new HashSet(0);
  }
  
  public AssignmentImpl
  	(Signet							signet,
     PrivilegedSubject	grantor, 
     PrivilegedSubject 	grantee,
     TreeNode						scope,
     Function						function,
     Set								limitValues,
     boolean            canUse,
     boolean						canGrant,
     Date               effectiveDate,
     Date               expirationDate)
  throws
  	SignetAuthorityException
  {
    super
      (signet,
       grantor, 
       grantee,
       effectiveDate,
       expirationDate);
    
    if (function == null)
    {
      throw new IllegalArgumentException
      	("It's illegal to grant an Assignment for a NULL Function.");
    }
    
    if ((canGrant == false) && (canUse == false))
    {
      throw new IllegalArgumentException
        ("It is illegal to create a new Assignment with both its canUse"
         + " and canGrant attributes set false.");
    }
    
    this.limitValues = limitValues;
    
    this.scope = scope;
    this.function = (FunctionImpl)function;
    this.canUse = canUse;
    this.canGrant = canGrant;
    
    this.checkAllLimitValues(function, limitValues);
    
    Decision decision = this.getGrantor().canEdit(this);
    
    if (decision.getAnswer() == false)
    {
      throw new SignetAuthorityException(decision);
    }
  }
  
  /**
   * Check to make sure that there's at least one LimitValue for every Limit
   * that's associated with the Function.
   * 
   * @param function
   * @param limitValues
   * @throws IllegalArgumentException
   */
  private void checkAllLimitValues
  	(Function	function,
  	 Set			limitValues)
  throws IllegalArgumentException
  {
    Set limits = function.getLimits();
    Iterator limitsIterator = limits.iterator();
    while (limitsIterator.hasNext())
    {
      Limit limit = (Limit)(limitsIterator.next());
      
      if (!(limitValueExists(limit, limitValues)))
      {
        throw new IllegalArgumentException
        	("An attempt to grant an Assignment for the Function '"
        	 + function.getId()
        	 + "' failed because the supplied set of limit-values did not"
        	 + " include a value for the required limit '"
        	 + limit.getId()
        	 + "'.");
      }
    }
  }
  
  /**
   * Check to make sure that there's at least one LimitValue for the specified
   * Limit.
   * 
   * @param limit
   * @param limitValues
   * @return
   */
  private boolean limitValueExists
  	(Limit	limit,
  	 Set		limitValues)
  {
    Iterator limitValuesIterator = limitValues.iterator();
    while (limitValuesIterator.hasNext())
    {
      LimitValue limitValue = (LimitValue)(limitValuesIterator.next());  
      if (limitValue.getLimit().equals(limit))
      {
        return true;
      }
    }
    
    // If we got this far, we found no match.
    return false;
  }
  
  /**
   * @param function The function to set.
   */
  void setFunction(Function function)
  {
    this.function = (FunctionImpl)function;
  }
  
  /**
   * @param scope The scope to set.
   */
  void setScope(TreeNode scope)
  {
    this.scope = scope;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getScope()
   */
  public TreeNode getScope()
  {
    ((TreeNodeImpl)this.scope).setSignet(this.getSignet());
    return this.scope;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getFunction()
   */
  public Function getFunction()
  {
    if (this.getSignet() != null)
    {
      this.function.setSignet(this.getSignet());
    }
    
    return this.function;
  }
  
  /**
   * @return A brief description of this AssignmentImpl. The exact details
   * 		of the representation are unspecified and subject to change.
   */
  public String toString()
  {
    return
      "[id=" + getId()
      + ",instance=" + getInstanceNumber()
      + ",scope=" + getScope() + "]";
  }

  public boolean canGrant()
  {
    return this.canGrant;
  }

  public void setCanGrant(PrivilegedSubject actor, boolean canGrant)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.canGrant = canGrant;
    this.setGrantor(actor);
  }
  
  // This method is only for use by Hibernate.
  protected void setCanGrant(boolean canGrant)
  {
    this.canGrant = canGrant;
  }
  
  // This method is only for use by Hibernate.
  protected boolean getCanGrant()
  {
    return this.canGrant;
  }

  public boolean canUse()
  {
    return this.canUse;
  }
  
  // This method is only for use by Hibernate.
  protected boolean getCanUse()
  {
    return this.canUse;
  }

  public void setCanUse(PrivilegedSubject actor, boolean canUse)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    super.setGrantor(actor);
    this.canUse = canUse;
  }


  // This method is for use only by Hibernate.
  protected void setCanUse(boolean canUse)
  {
    this.canUse = canUse;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    Assignment other = (Assignment)o;
    int comparisonResult;
    
    PrivilegedSubject thisGrantee = this.getGrantee();
    PrivilegedSubject otherGrantee = other.getGrantee();
    comparisonResult = thisGrantee.compareTo(otherGrantee);
    
    if (comparisonResult != 0)
    {
      return comparisonResult;
    }
    
    Subsystem thisSubsystem = this.getFunction().getSubsystem();
    Subsystem otherSubsystem = other.getFunction().getSubsystem();
    
    if (thisSubsystem == otherSubsystem)
    {
      comparisonResult = 0;
    }
    else if (thisSubsystem != null)
    {
      comparisonResult = thisSubsystem.compareTo(otherSubsystem);
    }
    else
    {
      comparisonResult = -1;
    }
    
    if (comparisonResult != 0)
    {
      return comparisonResult;
    }
    
    Category thisCategory = this.getFunction().getCategory();
    Category otherCategory = other.getFunction().getCategory();
    comparisonResult = thisCategory.compareTo(otherCategory);
    
    if (comparisonResult != 0)
    {
      return comparisonResult;
    }
    
    Function thisFunction = this.getFunction();
    Function otherFunction = other.getFunction();
    comparisonResult = thisFunction.compareTo(otherFunction);
    
    if (comparisonResult != 0)
    {
      return comparisonResult;
    }
    
    TreeNode thisScope = this.getScope();
    TreeNode otherScope = other.getScope();
    comparisonResult = thisScope.compareTo(otherScope);
    
    if (comparisonResult != 0)
    {
      return comparisonResult;
    }
    
    // This last clause is here to distinguish two Assignments which are 
    // otherwise identical twins:
    
    Integer thisId = this.getId();
    Integer otherId = other.getId();
    comparisonResult = thisId.compareTo(otherId);
    
    return comparisonResult;
  }
  
  /**
   * @return Returns the limitValues.
   */
  public Set getLimitValues()
  {
    // Let's make sure all of these LimitValues have their Signet members
    // set.
    Iterator limitValuesIterator = this.limitValues.iterator();
    while (limitValuesIterator.hasNext())
    {
      LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
      LimitImpl limit = (LimitImpl)(limitValue.getLimit());
      limit.setSignet(this.getSignet());
    }
    
    return this.limitValues;
  }

  
  public void setLimitValues(PrivilegedSubject actor, Set limitValues)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.setLimitValues(limitValues);
    this.setGrantor(actor);
  }
  
  private void setLimitValues(Set limitValues)
  {
    this.limitValues = limitValues;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#findDuplicates()
   */
  public Set findDuplicates()
  {
    return this.getSignet().findDuplicates(this);
  }
  
  void recordLimitValuesHistory
    (Session         session)
  throws HibernateException
  {
    Iterator limitValuesIterator
      = this.getLimitValues().iterator();
    while (limitValuesIterator.hasNext())
    {
      LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
      LimitValueHistory limitValueHistory
        = new LimitValueHistory(this, limitValue);
      
      if (session != null)
      {
        session.save(limitValueHistory);
      }
      else
      {
        this.getSignet().save(limitValueHistory);
      }
    }
  }
  
  public void save()
  {
    this.setModifyDatetime(new Date());
      
    if (this.getId() != null)
    {
      // This isn't the first time we've saved this Assignment.
      // We'll increment the instance-number accordingly, and save
      // its history-record right now (just after we save the Assignment
      // record itself, so as to avoid hitting any referential-integrity
      // problems in the database).
      this.incrementInstanceNumber();
        
      AssignmentHistory historyRecord
        = new AssignmentHistory(this);

      this.getSignet().save(this);
      this.getSignet().save(historyRecord);
        
      try
      {
        this.recordLimitValuesHistory(null);
      }
      catch (HibernateException he)
      {
        throw new SignetRuntimeException(he);
      }
    }
    else
    {
      // We can't construct the Assignment's initial history-record yet,
      // because we don't yet know the ID of the assignment. We'll set a
      // flag that will cause us to construct and save that history-record
      // later, in the postFlush() method of the Hibernate Interceptor.
      this.needsInitialHistoryRecord(true);
      this.getSignet().save(this);
    }
  }
}
