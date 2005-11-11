/*--
$Id: History.java,v 1.3 2005-11-11 00:24:01 acohen Exp $
$Date: 2005-11-11 00:24:01 $

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
abstract class History
{
  protected Integer historyId;
  private Date historyDatetime = new Date();

  private PrivilegedSubject  grantor;
  private PrivilegedSubject  proxySubject;  
  private PrivilegedSubject  grantee;
  private PrivilegedSubject  revoker;
  
  private Date              effectiveDate;
  private Date              expirationDate;
  private Status            status;
  private int               instanceNumber;
  
  /* The date and time this record was created. */
  private Date  modifyDatetime = new Date();
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public History()
  {
    super();
  }

  History(GrantableImpl grantableInstance)
  {
    this.setGrantor(grantableInstance.getGrantor());
    this.setProxySubject(grantableInstance.getProxy());
    this.setGrantee(grantableInstance.getGrantee());    
    this.setRevoker(grantableInstance.getRevoker());
    this.setEffectiveDate(grantableInstance.getEffectiveDate());
    this.setExpirationDate(grantableInstance.getExpirationDate());
    this.setStatus(grantableInstance.getStatus());
    this.setInstanceNumber(grantableInstance.getInstanceNumber());
    
    this.historyDatetime = new Date();
  }
  
  // This method exists only for use by Hibernate.
  protected Date getHistoryDatetime()
  {
    return this.historyDatetime;
  }
  
  // This method exists only for use by Hibernate.
  protected void setHistoryDatetime(Date historyDatetime)
  {
    this.historyDatetime = historyDatetime;
  }
  
  /**
   * 
   * @return the unique identifier of this History record.
   */
  Integer getHistoryId()
  {
    return this.historyId;
  }
  
  // This method is only for use by Hibernate.
  protected void setHistoryId(Integer historyId)
  {
    this.historyId = historyId;
  }

  protected void setGrantee(PrivilegedSubject grantee)
  {
    this.grantee = grantee;
  }
  
  /**
   * @param grantor The grantor to set.
   */
  protected void setGrantor(PrivilegedSubject grantor)
  {
    this.grantor = grantor;
  }

  protected void setRevoker(PrivilegedSubject revoker)
  {
    this.revoker = revoker;
  }

  protected void setProxySubject(PrivilegedSubject proxySubject)
  {
    this.proxySubject = proxySubject;
  }
  
  protected PrivilegedSubject getGrantor()
  {
    return this.grantor;
  }
  
  protected PrivilegedSubject getGrantee()
  {
    return this.grantee;
  }
  
  protected PrivilegedSubject getRevoker()
  {
    return this.revoker;
  }
  
  protected PrivilegedSubject getProxySubject()
  {
    return this.proxySubject;
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
