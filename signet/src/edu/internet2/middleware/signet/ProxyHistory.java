/*--
$Id: ProxyHistory.java,v 1.1 2005-08-25 20:31:35 acohen Exp $
$Date: 2005-08-25 20:31:35 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class ProxyHistory
{
  // ProxyHistory is unusual among Signet entities in that it
  // (along with ProxyImpl) has a numeric, not alphanumeric ID.
  protected Integer historyId;
  protected Integer proxyId;
  private Date historyDatetime = new Date();

  private   String            grantorId;
  private   String            grantorTypeId;
  
  private   String            granteeId;
  private   String            granteeTypeId;
  
//  private PrivilegedSubject revoker;
  private String            revokerId;
  private String            revokerTypeId;
  
  private Subsystem         subsystem;
  private boolean           canExtend;
  private boolean           canUse;
  private Date              effectiveDate;
  private Date              expirationDate;
  private Status            status;
  private int               instanceNumber;
  
  /* The date and time this record was created. */
  private Date  modifyDatetime = new Date();
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public ProxyHistory()
  {
    super();
  }
  
  ProxyHistory(ProxyImpl proxy)
  {
    // Most information is just copied from the Assignment object to the
    // AssignmentHistory object.
    this.setProxyId(proxy.getId());
    this.setGrantor(proxy.getGrantor());
    this.setGrantee(proxy.getGrantee());    
    this.setRevoker(proxy.getRevoker());
    this.setSubsystem(proxy.getSubsystem());
    this.setCanExtend(proxy.canExtend());
    this.setCanUse(proxy.canUse());
    this.setEffectiveDate(proxy.getEffectiveDate());
    this.setExpirationDate(proxy.getExpirationDate());
    this.setStatus(proxy.getStatus());
    this.setInstanceNumber(proxy.getInstanceNumber());
    
    this.historyDatetime = new Date();
  }
  
  // This method exists only for use by Hibernate.
  Date getHistoryDatetime()
  {
    return this.historyDatetime;
  }
  
  // This method exists only for use by Hibernate.
  void setHistoryDatetime(Date historyDatetime)
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
  
  Integer getProxyId()
  {
    return this.proxyId;
  }
  
  // This method is only for use by Hibernate.
  protected void setProxyId(Integer id)
  {
    this.proxyId = id;
  }
  
  // This method is only for use by Hibernate.
  protected void setHistoryId(Integer historyId)
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
  
  Subsystem getSubsystem()
  {
    return this.subsystem;
  }
  
  void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = subsystem;
  }
  
  boolean canExtend()
  {
    return this.canExtend;
  }
  
  void setCanExtend(boolean canExtend)
  {
    this.canExtend = canExtend;
  }
  
  boolean canUse()
  {
    return this.canUse;
  }
  
  void setCanUse(boolean canUse)
  {
    this.canUse = canUse;
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
      "[proxyId="
      + this.getProxyId()
      + ", instanceNumber="
      + this.getInstanceNumber()
      + "]";
  }
  
  // This method is only for use by Hibernate.
  protected Date getModifyDatetime()
  {
    return this.modifyDatetime;
  }
  
  // This method is only for use by Hibernate.
  protected void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }
}
