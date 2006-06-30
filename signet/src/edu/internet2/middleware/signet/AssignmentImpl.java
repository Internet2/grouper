/*--
$Id: AssignmentImpl.java,v 1.39 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $
 
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.tree.TreeNode;

public class AssignmentImpl extends GrantableImpl implements Assignment
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
  	(Signet							    signet,
     PrivilegedSubjectImpl	grantor,
     PrivilegedSubject 	    grantee,
     TreeNode               scope,
     Function               function,
     Set                    limitValues,
     boolean                canUse,
     boolean                canGrant,
     Date                   effectiveDate,
     Date                   expirationDate)
  throws
  	SignetAuthorityException
  {
    super
      (signet,
       grantor,
       grantee,
       effectiveDate,
       expirationDate);
    
    // The Signet application can only do one thing: Grant a Proxy to a
    // System Administrator. The Signet Application can never directly
    // grant any Assignment to anyone.
    if (grantor == getSignet().getSignetSubject())
    {
      Decision decision = new DecisionImpl(false, Reason.CANNOT_USE, null);
      throw new SignetAuthorityException(decision);
    }
    
    if (function == null)
    {
      throw new IllegalArgumentException
        ("It's illegal to grant an Assignment for a NULL Function.");
    }
    
    if (!grantor.equals(grantor.getEffectiveEditor()))
    {
      // At this point, we know the following things:
      //
      //   1) This grantor is "acting as" some other PrivilegedSubject.
      //
      //   2) This grantor does indeed hold at least one currently active Proxy
      //      from the PrivilegedSubject that he/she is "acting as". That was
      //      confirmed when PrivilegedSubject.setActingFor() was executed.
      //
      // We still need to confirm the following points:
      //
      //   3) At least one of those Proxies described in (2) above must have its
      //      "can use" flag set, thereby allowing this grantor to use that
      //      proxy to grant some Assignment.
      //
      //   4) At least one of the Proxies described in (3) above must encompass
      //      the Subsystem of this Assignment.

      Reason[] reasonArray = new Reason[1];
      if (!grantor.hasUsableProxy
            (grantor.getEffectiveEditor(),
             function.getSubsystem(),
             reasonArray))
      {
        Decision decision = new DecisionImpl(false, reasonArray[0], null);
        throw new SignetAuthorityException(decision);
      }
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
    
    Decision decision = grantor.canEdit(this);
    
    if (decision.getAnswer() == false)
    {
      throw new SignetAuthorityException(decision);
    }

    AssignmentHistory historyRecord
      = new AssignmentHistoryImpl(this);
  
    Set historySet = new HashSet(1);
    historySet.add(historyRecord);
    this.setHistory(historySet);
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
  	(Limit limit,
  	 Set   limitValues)
  {
    if (limitValues == null)
    {
      return false;
    }
    
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
    ((TreeNodeImpl)this.scope).setSignet(getSignet());
    return this.scope;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getFunction()
   */
  public Function getFunction()
  {
    if (getSignet() != null)
    {
      this.function.setSignet(getSignet());
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

  public void setCanGrant
    (PrivilegedSubject  actor,
     boolean            canGrant)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.canGrant = canGrant;
    this.setGrantor((PrivilegedSubjectImpl)actor);
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

  public void setCanUse
    (PrivilegedSubject  actor,
     boolean            canUse)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    super.setGrantor((PrivilegedSubjectImpl)actor);
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
      limit.setSignet(getSignet());
    }
    
    return this.limitValues;
  }

  
  public void setLimitValues
    (PrivilegedSubject  actor,
     Set                limitValues)
  throws SignetAuthorityException
  {
    checkLimitValues(limitValues);
    checkEditAuthority(actor);
    
    this.setLimitValues(limitValues);
    this.setGrantor((PrivilegedSubjectImpl)actor);
  }
  
  private void checkLimitValues(Set limitValues)
  throws IllegalArgumentException
  {
    Iterator limitValuesIterator = limitValues.iterator();
    while (limitValuesIterator.hasNext())
    {
      LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
      
      // Let's build up a set of all the legal Choice-values for this Limit.
      Set legalChoiceValues = new HashSet();
      Set choices = limitValue.getLimit().getChoiceSet().getChoices();
      Iterator choicesIterator = choices.iterator();
      while (choicesIterator.hasNext())
      {
        Choice choice = (Choice)(choicesIterator.next());
        legalChoiceValues.add(choice.getValue());
      }
      
      if (!legalChoiceValues.contains(limitValue.getValue()))
      {
        throw new IllegalArgumentException
          ("'"
           + limitValue.getValue()
           + "' is not a legal value for the Limit"
           + " with ID '"
           + limitValue.getLimit().getId()
           + "' and name '"
           + limitValue.getLimit().getName()
           + "'. Legal values for this Limit are: "
           + legalChoiceValues);
      }
    }
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
    return getSignet().getPersistentDB().findDuplicates(this);
  }

  
  public void save()
  {
    this.setModifyDatetime(new Date());

    save(this.getGrantor());
    save(this.getGrantee());
    save(this.getRevoker());
    save(this.getProxy());
      
    if (this.getId() != null)
    {
      // This isn't the first time we've saved this Assignment.
      // We'll increment the instance-number accordingly, and save
      // its history-record right now (just after we save the Assignment
      // record itself, so as to avoid hitting any referential-integrity
      // problems in the database).
      this.incrementInstanceNumber();
        
      AssignmentHistory historyRecord
        = new AssignmentHistoryImpl(this);
      Set historySet = this.getHistory();
      historySet.add(historyRecord);
      this.setHistory(historySet);
      
      getSignet().getPersistentDB().save(this);
    }
    else
    {
      // We can't construct the Assignment's initial history-record yet,
      // because we don't yet know the ID of the assignment. We'll detect this
      // case, construct and save that history-record later, in the postFlush()
      // method of the Hibernate Interceptor.
      getSignet().getPersistentDB().save(this);
    }
  }
}
