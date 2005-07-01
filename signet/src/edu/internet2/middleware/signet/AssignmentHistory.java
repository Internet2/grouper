/*--
$Id: AssignmentHistory.java,v 1.3 2005-07-01 01:51:33 acohen Exp $
$Date: 2005-07-01 01:51:33 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AssignmentHistory
{
  // AssignmentHistory is unusual among Signet entities in that it
  // (along with AssignmentImpl) has a numeric, not alphanumeric ID.
  protected Integer historyId;
  protected Integer assignmentId;
  private Date historyDatetime = new Date();

  private   String            grantorId;
  private   String            grantorTypeId;
  
  private   String            granteeId;
  private   String            granteeTypeId;
  
//  private PrivilegedSubject revoker;
  private String            revokerId;
  private String            revokerTypeId;
  
  private TreeNode          scope;
  private Function          function;
  private Set               limitValues;
  private boolean           grantable;
  private boolean           grantOnly;
  private Date              effectiveDate;
  private Date              expirationDate;
  private Status            status;
  private int               instanceNumber;
  
  /* The date and time this record was created. */
  private Date  modifyDatetime = new Date();
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public AssignmentHistory()
  {
    super();
  }
  
  AssignmentHistory(AssignmentImpl assignment)
  {
    // Most information is just copied from the Assignment object to the
    // AssignmentHistory object.
    this.setAssignmentId(assignment.getId());
    this.setGrantor(assignment.getGrantor());
    this.setGrantee(assignment.getGrantee());    
    this.setRevoker(assignment.getRevoker());
    this.setScope(assignment.getScope());
    this.setFunction(assignment.getFunction());
    this.setLimitValues(assignment.getLimitValues());
    this.setGrantable(assignment.isGrantable());
    this.setGrantOnly(assignment.isGrantOnly());
    this.setEffectiveDate(assignment.getEffectiveDate());
    this.setExpirationDate(assignment.getExpirationDate());
    this.setStatus(assignment.getStatus());
    this.setInstanceNumber(assignment.getInstanceNumber());
    
    this.historyDatetime = new Date();
  }
  
  // This method exists only for use by Hibernate.
  private Date getHistoryDatetime()
  {
    return this.historyDatetime;
  }
  
  // This method exists only for use by Hibernate.
  private void setHistoryDatetime(Date historyDatetime)
  {
    this.historyDatetime = historyDatetime;
  }
  
  /**
   * 
   * @return the unique identifier of this AssignmentHistory record.
   */
  Integer getHistoryId()
  {
    return this.historyId;
  }
  
  Integer getAssignmentId()
  {
    return this.assignmentId;
  }
  
  // This method is only for use by Hibernate.
  protected void setAssignmentId(Integer id)
  {
    this.assignmentId = id;
  }
  
  // This method is only for use by Hibernate.
  private void setHistoryId(Integer historyId)
  {
    this.historyId = historyId;
  }

  void setGrantee(PrivilegedSubject grantee)
  {
    this.granteeId = grantee.getSubjectId();
    this.granteeTypeId = grantee.getSubjectTypeId();
  }
  
  /**
   * @param grantor The grantor to set.
   */
  void setGrantor(PrivilegedSubject grantor)
  {
    this.grantorId = grantor.getSubjectId();
    this.grantorTypeId = grantor.getSubjectTypeId();
  }

  void setRevoker(PrivilegedSubject revoker)
  {
    if (revoker != null)
    {
      this.revokerId = revoker.getSubjectId();
      this.revokerTypeId = revoker.getSubjectTypeId();
    }
    else
    {
      this.revokerId = null;
      this.revokerTypeId = null;
    }
  }
  
  TreeNode getScope()
  {
    return this.scope;
  }
  
  void setScope(TreeNode scope)
  {
    this.scope = scope;
  }
  
  Date getEffectiveDate()
  {
    return this.effectiveDate;
  }
  
  void setEffectiveDate(Date effectiveDate)
  {
    this.effectiveDate = effectiveDate;
  }
  
  Date getExpirationDate()
  {
    return this.expirationDate;
  }
  
  void setExpirationDate(Date expirationDate)
  {
    this.expirationDate = expirationDate;
  }
  
  Function getFunction()
  {
    return this.function;
  }
  
  void setFunction(Function function)
  {
    this.function = function;
  }
  
  boolean isGrantable()
  {
    return this.grantable;
  }
  
  void setGrantable(boolean grantable)
  {
    this.grantable = grantable;
  }
  
  boolean isGrantOnly()
  {
    return this.grantOnly;
  }
  
  void setGrantOnly(boolean grantOnly)
  {
    this.grantOnly = grantOnly;
  }
  
  Set getLimitValues()
  {
    return this.limitValues;
  }
  
  void setLimitValues(Set limitValues)
  {
    // Let's make our own local copy of this Set, to remember its state right
    // now, when the history is being recorded.
    this.limitValues = new HashSet(limitValues);
  }
  
  Status getStatus()
  {
    return this.status;
  }
  
  void setStatus(Status status)
  {
    this.status = status;
  }
  
  int getInstanceNumber()
  {
    return this.instanceNumber;
  }
  
  void setInstanceNumber(int instanceNumber)
  {
    this.instanceNumber = instanceNumber;
  }
  
  String getGranteeId()
  {
    return this.granteeId;
  }
  
  void setGranteeId(String granteeId)
  {
    this.granteeId = granteeId;
  }
  
  String getGranteeTypeId()
  {
    return this.granteeTypeId;
  }
  
  void setGranteeTypeId(String granteeTypeId)
  {
    this.granteeTypeId = granteeTypeId;
  }
  
  String getGrantorId()
  {
    return this.grantorId;
  }

  void setGrantorId(String grantorId)
  {
    this.grantorId = grantorId;
  }
  
  String getGrantorTypeId()
  {
    return this.grantorTypeId;
  }
  
  void setGrantorTypeId(String grantorTypeId)
  {
    this.grantorTypeId = grantorTypeId;
  }
  
  String getRevokerId()
  {
    return this.revokerId;
  }

  void setRevokerId(String revokerId)
  {
    this.revokerId = revokerId;
  }
  
  String getRevokerTypeId()
  {
    return this.revokerTypeId;
  }
  
  void setRevokerTypeId(String revokerTypeId)
  {
    this.revokerTypeId = revokerTypeId;
  }
  
  public String toString()
  {
    return
      "[assignmentId="
      + this.getAssignmentId()
      + ", instanceNumber="
      + this.getInstanceNumber()
      + "]";
  }
  
  // This method is only for use by Hibernate.
  private Date getModifyDatetime()
  {
    return this.modifyDatetime;
  }
  
  // This method is only for use by Hibernate.
  private void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }
}
