/*--
  $Id: AssignmentImpl.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.SubjectNotFoundException;

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
implements Assignment, Comparable
{
  private Signet				  	signet;
  private Integer						id;
  private Status						status;
  private PrivilegedSubject	grantor;
  private PrivilegedSubject	grantee;
  private PrivilegedSubject revoker;
  private TreeNode					scope;
  private Subsystem					subsystem;
  private Function					function;
  private Housekeeping			housekeeping;
  private boolean						grantable;
  private boolean						grantOnly;
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public AssignmentImpl()
  {
    super();
    this.status = Status.PENDING;
    this.housekeeping = new Housekeeping();
  }
  
  public AssignmentImpl
  	(Signet							signet,
  	 PrivilegedSubject	grantor, 
  	 PrivilegedSubject 	grantee,
  	 TreeNode						scope,
  	 Function						function,
  	 boolean						canGrant,
  	 boolean						grantOnly)
  throws
  	SignetAuthorityException
  {
    super();
    
    if ((canGrant == false) && (grantOnly == true))
    {
      throw new IllegalArgumentException
      	("It is illegal to create a new Assignment with both its canGrant"
      	 + " and grantOnly attributes set true.");
    }

    this.signet = signet;
    this.grantor = grantor;
    this.grantee = grantee;
    this.subsystem = function.getSubsystem();
    this.scope = scope;
    this.function = function;
    this.grantable = canGrant;
    this.grantOnly = grantOnly;
    this.status = Status.ACTIVE;
    this.housekeeping = new Housekeeping();

    if (! this.grantor.canEdit(this))
    {
      throw new SignetAuthorityException
      	("The grantor '"
      	 + grantor.getId()
      	 + "' does not have the authority to assign the function '"
      	 + function.getId()
      	 + "' in the scope '"
      	 + scope.getId()
      	 + "'. "
      	 + this.grantor.editRefusalExplanation(this, "grantor"));
    }
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getGrantee()
   */
  public PrivilegedSubject getGrantee()
  {
    if (this.signet != null)
    {
      ((PrivilegedSubjectImpl)(this.grantee)).setSignet(this.signet);
    }
    
    return this.grantee;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getGrantor()
   */
  public PrivilegedSubject getGrantor()
  {
    if (this.signet != null)
    {
      ((PrivilegedSubjectImpl)(this.grantee)).setSignet(this.signet);
    }
    
    return this.grantor;
  }
  
  public PrivilegedSubject getRevoker()
  {
    if (this.signet != null)
    {
      ((PrivilegedSubjectImpl)(this.revoker)).setSignet(this.signet);
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
  
  void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = subsystem;
  }

  /**
   * @param function The function to set.
   */
  void setFunction(Function function)
  {
    this.function = function;
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
  }
  
  /**
   * @param grantor The grantor to set.
   */
  void setGrantor(PrivilegedSubject grantor)
  {
    this.grantor = grantor;
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
    return this.scope;
  }

  /**
   * @return
   */
  String getComment()
  {
    return this.housekeeping.getComment();
  }
  /**
   * @return
   */
  String getCreateContext()
  {
    return this.housekeeping.getCreateContext();
  }
  /**
   * @return
   */
  public Date getCreateDateTime()
  {
    return this.housekeeping.getCreateDateTime();
  }
  /**
   * @return
   */
  String getCreateDbAccount()
  {
    return this.housekeeping.getCreateDbAccount();
  }
  /**
   * @return
   */
  String getCreateUserID()
  {
    return this.housekeeping.getCreateUserID();
  }
  /**
   * @return
   */
  String getModifyContext()
  {
    return this.housekeeping.getModifyContext();
  }
  /**
   * @return
   */
  Date getModifyDateTime()
  {
    return this.housekeeping.getModifyDateTime();
  }
  /**
   * @return
   */
  String getModifyDbAccount()
  {
    return this.housekeeping.getModifyDbAccount();
  }
  /**
   * @return
   */
  String getModifyUserID()
  {
    return this.housekeeping.getModifyUserID();
  }
  /**
   * @param comment
   */
  void setComment(String comment)
  {
    this.housekeeping.setComment(comment);
  }
  /**
   * @param createContext
   */
  void setCreateContext(String createContext)
  {
    this.housekeeping.setCreateContext(createContext);
  }
  /**
   * @param createDateTime
   */
  void setCreateDateTime(Date createDateTime)
  {
    this.housekeeping.setCreateDateTime(createDateTime);
  }
  /**
   * @param createDbAccount
   */
  void setCreateDbAccount(String createDbAccount)
  {
    this.housekeeping.setCreateDbAccount(createDbAccount);
  }
  /**
   * @param userID
   */
  void setCreateUserID(String userID)
  {
    this.housekeeping.setCreateUserID(userID);
  }
  /**
   * @param modifyContext
   */
  void setModifyContext(String modifyContext)
  {
    this.housekeeping.setModifyContext(modifyContext);
  }
  /**
   * @param modifyDateTime
   */
  void setModifyDateTime(Date modifyDateTime)
  {
    this.housekeeping.setModifyDateTime(modifyDateTime);
  }
  /**
   * @param modifyDbAccount
   */
  void setModifyDbAccount(String modifyDbAccount)
  {
    this.housekeeping.setModifyDbAccount(modifyDbAccount);
  }
  /**
   * @param userID
   */
  void setModifyUserID(String userID)
  {
    this.housekeeping.setModifyUserID(userID);
  }
  /**
   * @return Returns the status.
   */
  public Status getStatus()
  {
    return this.status;
  }
  /**
   * @param id The id to set.
   */
  void setId(Integer id)
  {
    this.id = id;
  }
  /**
   * @param status The status to set.
   */
  void setStatus(Status status)
  {
    this.status = status;
  }
  
  /**
   * 
   * @return the unique identifier of this Assignment.
   */
  public Integer getId()
  {
    return this.id;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getFunction()
   */
  public Function getFunction()
  {
    return this.function;
  }
  
  public Subsystem getSubsystem()
  {
    return this.subsystem;
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
    			.append("createDateTime", getCreateDateTime())
    			.append("modifyDateTime", getModifyDateTime())
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
    
    Subsystem thisSubsystem = this.getSubsystem();
    Subsystem otherSubsystem = other.getSubsystem();
    comparisonResult = thisSubsystem.compareTo(otherSubsystem);
    
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
  
  void setSignet(Signet signet)
  {
    this.signet = signet;
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
      	 + revoker.getId()
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
}
