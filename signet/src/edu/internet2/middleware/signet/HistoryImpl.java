/*--
$Id: HistoryImpl.java,v 1.4 2006-10-25 00:08:28 ddonn Exp $
$Date: 2006-10-25 00:08:28 $

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
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

/**
 * @author Andy Cohen
 *
 */
abstract class HistoryImpl implements History
{
  protected Integer historyId;
  private Date historyDatetime = new Date();

  private SignetSubject  grantor;
  private SignetSubject  proxySubject;  
  private SignetSubject  grantee;
  private SignetSubject  revoker;
  
  private Date              effectiveDate;
  private Date              expirationDate;
  private Status            status;
  private int               instanceNumber;
  
  /* The date and time this record was created. */
  private Date  modifyDatetime = new Date();
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public HistoryImpl()
  {
    super();
  }

  HistoryImpl(GrantableImpl grantableInstance)
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
   * @return the unique identifier of this HistoryImpl record.
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

  protected void setGrantee(SignetSubject grantee)
  {
    this.grantee = grantee;
  }
  
  /**
   * @param grantor The grantor to set.
   */
  protected void setGrantor(SignetSubject grantor)
  {
    this.grantor = grantor;
  }

  protected void setRevoker(SignetSubject revoker)
  {
    this.revoker = revoker;
  }

  protected void setProxySubject(SignetSubject proxySubject)
  {
    this.proxySubject = proxySubject;
  }
  
  public SignetSubject getGrantor()
  {
    return this.grantor;
  }
  
  public SignetSubject getGrantee()
  {
    return this.grantee;
  }
  
  public SignetSubject getRevoker()
  {
    return this.revoker;
  }
  
  public SignetSubject getProxySubject()
  {
    return this.proxySubject;
  }
  
  
  public Date getEffectiveDate()
  {
    return this.effectiveDate;
  }
  
  void setEffectiveDate(Date effectiveDate)
  {
    this.effectiveDate = effectiveDate;
  }
  
  public Date getExpirationDate()
  {
    return this.expirationDate;
  }
  
  void setExpirationDate(Date expirationDate)
  {
    this.expirationDate = expirationDate;
  }
  
  public Status getStatus()
  {
    return this.status;
  }
  
  void setStatus(Status status)
  {
    this.status = status;
  }
  
  public int getInstanceNumber()
  {
    return this.instanceNumber;
  }
  
  void setInstanceNumber(int instanceNumber)
  {
    this.instanceNumber = instanceNumber;
  }
  
  public Date getModifyDatetime()
  {
    return this.modifyDatetime;
  }
  
  // This method is only for use by Hibernate.
  protected void setModifyDatetime(Date modifyDatetime)
  {
    this.modifyDatetime = modifyDatetime;
  }
  
  public Date getDate()
  {
    return this.getHistoryDatetime();
  }
}
