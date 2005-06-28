/*--
 $Id: AssignmentImpl.java,v 1.16 2005-06-28 19:41:57 acohen Exp $
 $Date: 2005-06-28 19:41:57 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;

class AssignmentImpl
extends EntityImpl
implements Assignment, Comparable
{
  // AssignmentImpl is unusual among Signet entities in that it
  // has a numeric, not alphanumeric ID.
  private Integer						id;
  
  private PrivilegedSubject	grantor;
  private   String						grantorId;
  private   String						grantorTypeId;
  
  private PrivilegedSubject	grantee;
  private   String						granteeId;
  private   String						granteeTypeId;
  
  private PrivilegedSubject revoker;
  private TreeNode					scope;
  private FunctionImpl			function;
  private Set								limitValues;
  private boolean						grantable;
  private boolean						grantOnly;
  private Date							effectiveDate;
  private Date              expirationDate      = null;
  private int               instanceNumber;
  
  private boolean limitValuesAlreadyFetched = false;
  boolean					hasUnsavedLimitValues = false;
  boolean         needsInitialHistoryRecord = false;
  Set             historyRecords = new HashSet();


  
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
     boolean						grantOnly,
     Date               effectiveDate,
     Date               expirationDate)
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

    this.effectiveDate = effectiveDate;
    this.expirationDate = expirationDate;
    
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
    this.setModifyDatetime(new Date());

    // The history-record for this new Assignment will be generated
    // after this Assignment gets its ID, which happens at save-time.
    this.needsInitialHistoryRecord = true;
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
    if ((this.getSignet() != null) && (this.revoker != null))
    {
      ((PrivilegedSubjectImpl)(this.revoker))
      	.setSignet(this.getSignet());
    }
    
    return this.revoker;
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
  public Integer getId()
  {
    return this.id;
  }
  
  // This method is only for use by Hibernate.
  private void setId(Integer id)
  {
    this.id = id;
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

  public void setGrantable(PrivilegedSubject actor, boolean grantable)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.grantable = grantable;
    this.setGrantor(actor);
    
    recordHistory();
  }
  
  private void recordHistory()
  {
    this.setModifyDatetime(new Date());
    this.instanceNumber++;
    
    AssignmentHistory assignmentHistory = new AssignmentHistory(this);
    this.historyRecords.add(assignmentHistory);
    // this.getSignet().save(assignmentHistory);
  }
  
  // This method is only for use by Hibernate.
  private void setGrantable(boolean grantable)
  {
    this.grantable = grantable;
  }

  public boolean isGrantOnly()
  {
    return this.grantOnly;
  }

  public void setGrantOnly(PrivilegedSubject actor, boolean grantOnly)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.grantOnly = grantOnly;
    this.grantor = actor;
    
    recordHistory();
  }


  private void setGrantOnly(boolean grantOnly)
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
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#revoke()
   */
  public void revoke(PrivilegedSubject revoker)
  throws SignetAuthorityException
  {
    if (!revoker.canEdit(this))
    {
      throw new SignetAuthorityException
      ("The Subject '"
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
    
    recordHistory();
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getEffectiveDate()
   */
  public Date getEffectiveDate()
  {
    return this.effectiveDate;
  }
  
  private void checkEditAuthority(PrivilegedSubject actor)
  throws SignetAuthorityException
  {
    if (!actor.canEdit(this))
    {
      throw new SignetAuthorityException
        ("The Subject '"
         + actor.getSubjectId()
         + "' does not have the authority to edit the function '"
         + function.getId()
         + "' in the scope '"
         + scope.getId()
         + "'. "
         + revoker.editRefusalExplanation(this, "actor"));
    }
  }
  
  public void setEffectiveDate(PrivilegedSubject actor, Date date)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.effectiveDate = date;
    this.setGrantor(actor);
    recordHistory();
  }


  private void setEffectiveDate(Date date)
  {
    this.effectiveDate = date;
  }
  
  /**
   * @return Returns the limitValues.
   */
  public Set getLimitValues()
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

  
  public void setLimitValues(PrivilegedSubject actor, Set limitValues)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.setLimitValues(limitValues);
    this.setGrantor(actor);
    
    recordHistory();
  }
  
  private void setLimitValues(Set limitValues)
  {
    this.limitValues = limitValues;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getExpirationDate()
   */
  public Date getExpirationDate()
  {
    return this.expirationDate;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#setExpirationDate(java.util.Date)
   */
  public void setExpirationDate(PrivilegedSubject actor, Date expirationDate)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.expirationDate = expirationDate;
    this.setGrantor(actor);
    this.setModifyDatetime(new Date());
    
    recordHistory();
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#findDuplicates()
   */
  public Set findDuplicates()
  {
    return this.getSignet().findDuplicates(this);
  }
  
  int getInstanceNumber()
  {
    return this.instanceNumber;
  }
  
  private void setInstanceNumber(int instanceNumber)
  {
    this.instanceNumber = instanceNumber;
  }


  private void setExpirationDate(Date expirationDate)
  {
    this.expirationDate = expirationDate;
  }
}
