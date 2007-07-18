/*--
$Id: HistoryImpl.java,v 1.5 2007-07-18 17:24:39 ddonn Exp $
$Date: 2007-07-18 17:24:39 $

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

  private SignetSubject  grantor;
  private SignetSubject  proxySubject;  
  private SignetSubject  grantee;
  private SignetSubject  revoker;
  
  private Date              effectiveDate;
  private Date              expirationDate;
  private Status            status;
  private int               instanceNumber;
  
  private Date				historyDatetime;

  /* The date and time this record was created. */
  private Date				modifyDatetime;
  
	/**
	 * Hibernate requires the presence of a default constructor.
	 */
	public HistoryImpl()
	{
		Date now = new Date();
		modifyDatetime = now;
		historyDatetime = now;
	}

	/**
	 * Create a History record from the provided Grantable
	 * @param grantableInstance The Proxy or Assignment 
	 */
	public HistoryImpl(GrantableImpl grantableInstance)
	{
		this();
		setGrantor(grantableInstance.getGrantor());
		setProxySubject(grantableInstance.getProxy());
		setGrantee(grantableInstance.getGrantee());    
		setRevoker(grantableInstance.getRevoker());
		setEffectiveDate(grantableInstance.getEffectiveDate());
		setExpirationDate(grantableInstance.getExpirationDate());
		setStatus(grantableInstance.getStatus());
		setInstanceNumber(grantableInstance.getInstanceNumber());
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


  ///////////////////////////////
  // overrides Object
  ///////////////////////////////

	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("[HistoryImpl: "); //$NON-NLS-1$
		buf.append("historyId=" + historyId); //$NON-NLS-1$
		buf.append(", historyDatetime=" + historyDatetime.toString()); //$NON-NLS-1$
		buf.append(", instanceNumber=" + instanceNumber); //$NON-NLS-1$
		buf.append(", status=" + status.toString()); //$NON-NLS-1$
		buf.append(", grantorId=" + grantor.getId()); //$NON-NLS-1$
		buf.append(", granteeId=" + grantee.getId()); //$NON-NLS-1$
		buf.append(", proxyId=" + ((null != proxySubject) ? proxySubject.getId() : "<null>")); //$NON-NLS-1$ $NON-NLS-2$
		buf.append(", revokerId=" + ((null != revoker) ? revoker.getId() : "<null>")); //$NON-NLS-1$ $NON-NLS-2$
		buf.append(", effectiveDate=" + ((null != effectiveDate) ? effectiveDate.toString() : "<null>")); //$NON-NLS-1$
		buf.append(", expirationDate=" + ((null != expirationDate) ? expirationDate.toString() : "<null>")); //$NON-NLS-1$
		buf.append(", modifyDatetime=" +((null != modifyDatetime) ?  modifyDatetime.toString() : "<null>")); //$NON-NLS-1$
		buf.append("]"); //$NON-NLS-1$

		return (buf.toString());
	}

}
