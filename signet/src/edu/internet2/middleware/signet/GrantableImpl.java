/*--
$Id: GrantableImpl.java,v 1.14 2006-02-09 10:20:29 lmcrae Exp $
$Date: 2006-02-09 10:20:29 $
 
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
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

abstract class GrantableImpl
extends EntityImpl
implements Grantable
{
  static final int MIN_INSTANCE_NUMBER = 1;

  // GrantableImpl is unusual among Signet entities in that it
  // has a numeric, not alphanumeric ID.
  private Integer						id;
  
  // If this Grantable instance was granted directly by a PrivilegedSubject,
  // then this is that PrivilegedSubject.
  //
  // If this Grantable instance was granted via a Proxy, then this is the
  // PrivilegedSubject who originally granted that Proxy.
  private PrivilegedSubjectImpl	grantor;
  
  // If this Grantable instance was granted directly by a PrivilegedSubject,
  // then this is null.
  //
  // If this Grantable instance was granted via a Proxy, then this is the
  // PrivilegedSubject who acting on behalf of the PrivilegedSubject who
  // originally granted that Proxy.
  private PrivilegedSubjectImpl proxy;
  
  private PrivilegedSubjectImpl	grantee;
  
  private PrivilegedSubjectImpl revoker;
  
  private Date                  effectiveDate;
  private Date                  expirationDate = null;
  private int                   instanceNumber = MIN_INSTANCE_NUMBER;
  private Set                   history = null;



  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public GrantableImpl()
  {
    super();
  }
  
  protected Status determineStatus
    (Date effectiveDate,
     Date expirationDate)
  {
    Date today = new Date();
    Status status;
    
    if ((effectiveDate != null) && (today.compareTo(effectiveDate) < 0))
    {
      // effectiveDate has not yet arrived.
      status = Status.PENDING;
    }
    else if ((expirationDate != null) && (today.compareTo(expirationDate) > 0))
    {
      // expirationDate has already passed.
      status = Status.INACTIVE;
    }
    else
    {
      status = Status.ACTIVE;
    }
    
    return status;
  }
  
  protected boolean datesInWrongOrder
    (Date effectiveDate,
     Date expirationDate)
  {
    boolean result = false;
    
    if ((effectiveDate != null) && (expirationDate != null))
    {
      if (effectiveDate.compareTo(expirationDate) >= 0)
      {
        return true;
      }
    }
    
    return result;
  }
  
  public GrantableImpl
  	(Signet							signet,
     PrivilegedSubject	grantor,
     PrivilegedSubject 	grantee,
     Date               effectiveDate,
     Date               expirationDate)
  {    
    super(signet, null, null, null);
    
    this.setGrantor((PrivilegedSubjectImpl)grantor);
    this.setGrantee(grantee);
    
    if (effectiveDate == null)
    {
      throw new IllegalArgumentException
        ("An effective-date may not be NULL.");
    }
    
    if (datesInWrongOrder(effectiveDate, expirationDate))
    {
      throw new IllegalArgumentException
        ("An expiration-date must be NULL or later than its"
         + " effective-date. The requested expiration-date '"
         + expirationDate
         + "' is neither NULL nor later than the requested effective-date '"
         + effectiveDate
         + "'.");
    }

    this.effectiveDate = effectiveDate;
    this.expirationDate = expirationDate;
    
    this.setStatus(determineStatus(effectiveDate, expirationDate));
    this.setModifyDatetime(new Date());
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getGrantee()
   */
  public PrivilegedSubject getGrantee()
  {
    this.grantee.setSignet(this.getSignet());
    return this.grantee;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getGrantor()
   */
  public PrivilegedSubject getGrantor()
  {
    this.grantor.setSignet(this.getSignet());
    return this.grantor;
  }
  
  public PrivilegedSubject getRevoker()
  {
    if (this.revoker != null)
    {
      this.revoker.setSignet(this.getSignet());
    }
    
    return this.revoker;
  }
  
  public PrivilegedSubject getProxy()
  {
    if (this.proxy != null)
    {
      this.proxy.setSignet(this.getSignet());
    }
    
    return this.proxy;
  }
  
  // This method is only for use by Hibernate.
  void setProxy(PrivilegedSubject proxy)
  {
    this.proxy = (PrivilegedSubjectImpl)proxy;
  }
  
  /**
   * @param grantee The grantee to set.
   */
  void setGrantee(PrivilegedSubject grantee)
  {
    this.grantee = (PrivilegedSubjectImpl)grantee;
  }
  
  /**
   * @param grantor The grantor to set.
   */
  void setGrantor(PrivilegedSubjectImpl grantor)
  {
    this.grantor = (PrivilegedSubjectImpl)(grantor.getEffectiveEditor());
    
    if (!grantor.equals(grantor.getEffectiveEditor()))
    {
      this.proxy = grantor;
    }
  }
  
  void setRevoker(PrivilegedSubjectImpl revoker)
  {
    if (revoker != null)
    {
      this.revoker = (PrivilegedSubjectImpl)(revoker.getEffectiveEditor());
    
      if (!revoker.equals(revoker.getEffectiveEditor()))
      {      
        this.proxy = revoker;
      }
    }
  }
  
//  void setProxy(PrivilegedSubject proxy)
//  {
//    this.proxy = proxy;
//    this.proxyId = proxy.getSubjectId();
//    this.proxyTypeId = proxy.getSubjectTypeId();
//  }

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
  protected void setId(Integer id)
  {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#revoke()
   */
  public void revoke
    (PrivilegedSubject revoker)
  throws SignetAuthorityException
  {
    Decision decision = revoker.canEdit(this);
    
    if (decision.getAnswer() == false)
    {
      throw new SignetAuthorityException(decision);
    }

    this.setRevoker((PrivilegedSubjectImpl)revoker);
    this.setStatus(Status.INACTIVE);
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getEffectiveDate()
   */
  public Date getEffectiveDate()
  {
    return this.effectiveDate;
  }
  
  protected void checkEditAuthority
    (PrivilegedSubject actor)
  throws SignetAuthorityException
  {
    Decision decision = actor.canEdit(this);
    if (decision.getAnswer() == false)
    {
      throw new SignetAuthorityException(decision);
    }
  }
  
  public void setEffectiveDate
    (PrivilegedSubject  actor,
     Date               date)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    if (date == null)
    {
      throw new IllegalArgumentException
        ("effectiveDate must have a non-NULL value.");
    }
    
    this.effectiveDate = date;
    this.setGrantor((PrivilegedSubjectImpl)actor);
  }


  // This method is for use only by Hibernate.
  protected void setEffectiveDate(Date date)
  {
    this.effectiveDate = date;
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
  public void setExpirationDate
    (PrivilegedSubject  actor,
     Date               expirationDate)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.expirationDate = expirationDate;
    this.setGrantor((PrivilegedSubjectImpl)actor);
    this.setModifyDatetime(new Date());
  }
  
  int getInstanceNumber()
  {
    return this.instanceNumber;
  }
  
  // This method is for use only by Hibernate.
  protected void setInstanceNumber(int instanceNumber)
  {
    this.instanceNumber = instanceNumber;
  }


  // This method is for use only by Hibernate.
  protected void setExpirationDate(Date expirationDate)
  {
    this.expirationDate = expirationDate;
  }
  
  void incrementInstanceNumber()
  {
    this.instanceNumber++;
  }

  public boolean equals(Object obj)
  {
    if ( !(obj instanceof GrantableImpl) )
    {
      return false;
    }
    
    GrantableImpl rhs = (GrantableImpl) obj;
    return new EqualsBuilder()
      .append(this.id, rhs.id)
      .isEquals();
  }
  
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37)
      .append(this.id)
      .toHashCode();
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getActualStartDatetime()
   */
  public Date getActualStartDatetime()
  {
    throw new UnsupportedOperationException
      ("This method is not yet implemented.");
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getActualEndDatetime()
   */
  public Date getActualEndDatetime()
  {
    throw new UnsupportedOperationException
      ("This method is not yet implemented.");
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#inactivate()
   */
  public void inactivate()
  {
    throw new UnsupportedOperationException
      ("This method is not yet implemented");
  }
  
  public boolean evaluate()
  {
    Date now = new Date();
    return this.evaluate(now);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#evaluate()
   */
  public boolean evaluate(Date date)
  {
    Status  newStatus;
    boolean statusChanged = false;
    
    if (date.compareTo(this.effectiveDate) < 0)
    {
      // The effectiveDate has not yet arrived.
      newStatus = Status.PENDING;
    }
    else if ((this.expirationDate != null)
             && (date.compareTo(this.expirationDate) > 0))
    {
      // The expirationDate has already passed.
      newStatus = Status.INACTIVE;
    }
    else
    {
      newStatus = Status.ACTIVE;
    }
    
    if (!newStatus.equals(this.getStatus()))
    {
      this.setStatus(newStatus);
      statusChanged = true;
    }
    
    return statusChanged;
  }
  
  protected void save(PrivilegedSubject pSubject)
  {
    if ((pSubject != null) && (pSubject.getId() == null))
    {
      ((PrivilegedSubjectImpl)pSubject).save();
    }
  }
  
  public Set getHistory()
  {
    return this.history;
  }
  
  void setHistory(Set history)
  {
    this.history = history;
  }
}
