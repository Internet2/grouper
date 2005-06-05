/*--
 $Id: AssignmentImpl.java,v 1.11 2005-06-05 05:43:48 mnguyen Exp $
 $Date: 2005-06-05 05:43:48 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.subject.Subject;

/* These are the columns in the Assignment database table:
 * 
 * assignmentID
 * status
 * subsystemID
 * functionID
 * grantorTypeID
 * grantorID
 * granteeTypeID
 * granteeID
 * proxyTypeID
 * proxyID
 * scopeNodeID
 * grantOnly
 * canGrant
 * effectiveDate
 * revokerTypeID
 * revokerID
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
class AssignmentImpl
extends EntityImpl
implements Assignment, Comparable
{
  // AssignmentImpl is unusual among Signet entities in that it
  // has a numeric, not alphanumeric ID.
  private Integer						id;
  
  private PrivilegedSubject	grantor;
  private String						grantorId;
  private String						grantorTypeId;
  
  private PrivilegedSubject	grantee;
  private String						granteeId;
  private String						granteeTypeId;
  
  private PrivilegedSubject revoker;
  private TreeNode					scope;
  private FunctionImpl			function;
  private Set								limitValues;
  private boolean						grantable;
  private boolean						grantOnly;
  private Date							effectiveDate;
  
  private boolean						limitValuesAlreadyFetched = false;
  boolean										hasUnsavedLimitValues = false;
  
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
     boolean						canGrant,
     boolean						grantOnly)
  throws
  	SignetAuthorityException
  {
    super(signet, null, null, Status.ACTIVE);
    
    this.hasUnsavedLimitValues = true;
    
    if (function == null)
    {
      throw new IllegalArgumentException
      	("It's illegal to grant an Assignment for a NULL Function.");
    }
    
    if ((canGrant == false) && (grantOnly == true))
    {
      throw new IllegalArgumentException
      ("It is illegal to create a new Assignment with both its canGrant"
          + " and grantOnly attributes set true.");
    }
    
    this.limitValues = limitValues;

    this.setGrantor(grantor);
    this.setGrantee(grantee);
    
    this.scope = scope;
    this.function = (FunctionImpl)function;
    this.grantable = canGrant;
    this.grantOnly = grantOnly;
    
    // By default, this assignment is active, starting now.
    this.effectiveDate = new Date();
    
    if (! this.grantor.canEdit(this))
    {
      throw new SignetAuthorityException
      ("The grantor '"
       + grantor.getSubjectId()
       + "' does not have the authority to assign the function '"
       + function.getId()
       + "' in the scope '"
       + scope.getId()
       + "'. "
       + this.grantor.editRefusalExplanation(this, "grantor"));
    }
    
    this.checkAllLimitValues(function, limitValues);
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
    Limit limits[] = function.getLimitsArray();
    for (int i = 0; i < limits.length; i++)
    {
      Limit limit = limits[i];
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
  
  public LimitValue[] getLimitValuesArray()
  {
    LimitValue limitValuesArray[] = new LimitValue[0];
    return (LimitValue[])(this.getLimitValues().toArray(limitValuesArray));
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getGrantee()
   */
  public PrivilegedSubject getGrantee()
  {
    if (this.grantee == null)
    {
      Subject subject;
      
      try
      {
        subject = this.getSignet().getSubject
        (this.granteeTypeId, this.granteeId);
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
      
      this.grantee
      = new PrivilegedSubjectImpl(this.getSignet(), subject);
    }
    
    return this.grantee;
  }
  
  private void setGranteeId(String id)
  {
    this.granteeId = id;
  }
  
  private String getGranteeId()
  {
    return this.granteeId;
  }
  
  private String getGranteeTypeId()
  {
    return this.granteeTypeId;
  }
  
  private void setGranteeTypeId(String typeId)
  {
    this.granteeTypeId = typeId;
  }
  
  private void setGrantorId(String id)
  {
    this.grantorId = id;
  }
  
  private String getGrantorId()
  {
    return this.grantorId;
  }
  
  private String getGrantorTypeId()
  {
    return this.grantorTypeId;
  }
  
  private void setGrantorTypeId(String typeId)
  {
    this.grantorTypeId = typeId;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getGrantor()
   */
  public PrivilegedSubject getGrantor()
  {
    if (this.grantor == null)
    {
      Subject subject;
      
      try
      {
        subject = this.getSignet().getSubject
        (this.grantorTypeId, this.grantorId);
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
      
      this.grantor
      = new PrivilegedSubjectImpl(this.getSignet(), subject);
    }
    
    return this.grantor;
  }
  
  public PrivilegedSubject getRevoker()
  {
    if (this.getSignet() != null)
    {
      ((PrivilegedSubjectImpl)(this.revoker))
      	.setSignet(this.getSignet());
    }
    
    return this.revoker;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getProxy()
   */
  public PrivilegedSubject getProxy()
  {
    // TODO Auto-generated method stub
    return null;
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
  
  /**
   * @param grantee The grantee to set.
   */
  void setGrantee(PrivilegedSubject grantee)
  {
    this.grantee = grantee;
    this.granteeId = grantee.getSubjectId();
    this.granteeTypeId = grantee.getSubjectTypeId();
  }
  
  /**
   * @param grantor The grantor to set.
   */
  void setGrantor(PrivilegedSubject grantor)
  {
    this.grantor = grantor;
    this.grantorId = grantor.getSubjectId();
    this.grantorTypeId = grantor.getSubjectTypeId();
  }
  
  void setRevoker(PrivilegedSubject revoker)
  {
    this.revoker = revoker;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getScope()
   */
  public TreeNode getScope()
  {
    ((TreeNodeImpl)this.scope).setSignet(this.getSignet());
    return this.scope;
  }

  /**
   * @param id The id to set.
   */
  void setNumericId(Integer id)
  {
    this.id = id;
  }
  
  /**
   * 
   * @return the unique identifier of this Assignment.
   */
  public Integer getNumericId()
  {
    return this.id;
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
    new 
    ToStringBuilder(this)
    .append("id", getId())
    .append("status", getStatus())
    .append("createDatetime", getCreateDatetime())
    .append("modifyDatetime", getModifyDatetime())
    .toString();
  }
  /**
   * @return Returns the grantable.
   */
  public boolean isGrantable()
  {
    return this.grantable;
  }
  /**
   * @param grantable The grantable to set.
   */
  public void setGrantable(boolean grantable)
  {
    this.grantable = grantable;
  }
  /**
   * @return Returns the grantOnly.
   */
  public boolean isGrantOnly()
  {
    return this.grantOnly;
  }
  /**
   * @param grantOnly The grantOnly to set.
   */
  public void setGrantOnly(boolean grantOnly)
  {
    this.grantOnly = grantOnly;
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
    
    Subsystem thisSubsystem = null;
    Subsystem otherSubsystem = null;
    try
    {
      thisSubsystem = this.getFunction().getSubsystem();
    }
    catch (ObjectNotFoundException onfe)
    {
      // thisSubsystem retains its initial NULL value.
    }
    
    try
    {
      otherSubsystem = other.getFunction().getSubsystem();
    }
    catch (ObjectNotFoundException onfe)
    {
      // otherSubsystem retains its initial NULL value.
    }
    
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
    
    Integer thisId = this.getNumericId();
    Integer otherId = other.getNumericId();
    comparisonResult = thisId.compareTo(otherId);
    
    return comparisonResult;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#revoke()
   */
  public void revoke(PrivilegedSubject revoker)
  throws SignetAuthorityException
  {
    if (!revoker.canEdit(this))
    {
      throw new SignetAuthorityException
      ("The revoker '"
          + revoker.getSubjectId()
          + "' does not have the authority to revoke the function '"
          + function.getId()
          + "' in the scope '"
          + scope.getId()
          + "'. "
          + revoker.editRefusalExplanation(this, "revoker"));
    }
    
    this.setRevoker(revoker);
    this.setStatus(Status.INACTIVE);
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getEffectiveDate()
   */
  public Date getEffectiveDate()
  {
    return this.effectiveDate;
  }
  
  void setEffectiveDate(Date date)
  {
    this.effectiveDate = date;
  }
  
  /**
   * @return Returns the limitValues.
   */
  Set getLimitValues()
  {
    Set unsavedLimitValues;
    
    if (limitValuesAlreadyFetched == false)
    {
      // Let's make sure we don't throw out any associated but not-yet-saved
      // LimitValues.
      unsavedLimitValues = this.limitValues;
      this.limitValues = this.getSignet().getLimitValues(this);

      limitValuesAlreadyFetched = true;

      this.limitValues.addAll(unsavedLimitValues);
    }
    
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

  /**
   * @param limitValues The limitValues to set.
   */
  void setLimitValues(Set limitValues)
  {
    this.limitValues = limitValues;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getLimitValuesInDisplayOrder()
   */
  public LimitValue[] getLimitValuesInDisplayOrder()
  {
    LimitValue[] limitValues = this.getLimitValuesArray();
    Arrays.sort(limitValues, new LimitValueDisplayOrder());
    return limitValues;
  }
}
