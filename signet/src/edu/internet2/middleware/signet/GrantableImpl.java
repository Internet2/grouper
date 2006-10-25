/*--
$Id: GrantableImpl.java,v 1.15 2006-10-25 00:08:28 ddonn Exp $
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
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

public abstract class GrantableImpl
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
  private SignetSubject	grantor;
  
  // If this Grantable instance was granted directly by a PrivilegedSubject,
  // then this is null.
  //
  // If this Grantable instance was granted via a Proxy, then this is the
  // PrivilegedSubject who acting on behalf of the PrivilegedSubject who
  // originally granted that Proxy.
  private SignetSubject proxy;
  
  private SignetSubject	grantee;
  
  private SignetSubject revoker;
  
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
     SignetSubject	grantor,
     SignetSubject 	grantee,
     Date               effectiveDate,
     Date               expirationDate)
  {    
    super(signet, null, null, null);
    
    this.setGrantor(grantor);
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
  public SignetSubject getGrantee()
  {
//    this.grantee.setSignet(this.getSignet());
    return this.grantee;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getGrantor()
   */
  public SignetSubject getGrantor()
  {
//    this.grantor.setSignet(this.getSignet());
    return this.grantor;
  }
  
  public SignetSubject getRevoker()
  {
//    if (this.revoker != null)
//    {
//      this.revoker.setSignet(this.getSignet());
//    }
    
    return this.revoker;
  }
  
  public SignetSubject getProxy()
  {
//    if (this.proxy != null)
//    {
//      this.proxy.setSignet(this.getSignet());
//    }
    
    return this.proxy;
  }
  
  // This method is only for use by Hibernate.
  void setProxy(SignetSubject proxy)
  {
    this.proxy = proxy;
  }
  
  /**
   * @param grantee The grantee to set.
   */
  void setGrantee(SignetSubject grantee)
  {
    this.grantee = grantee;
  }
  
  /**
   * @param grantor The grantor to set.
   */
  void setGrantor(SignetSubject grantor)
  {
    this.grantor = grantor.getEffectiveEditor();
    
    if (!grantor.equals(grantor.getEffectiveEditor()))
    {
      this.proxy = grantor;
    }
  }
  
  void setRevoker(SignetSubject revoker)
  {
    if (revoker != null)
    {
      this.revoker = revoker.getEffectiveEditor();
    
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
    (SignetSubject revoker)
  throws SignetAuthorityException
  {
    Decision decision = revoker.canEdit(this);
    
    if (decision.getAnswer() == false)
    {
      throw new SignetAuthorityException(decision);
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
  
  protected void checkEditAuthority
    (SignetSubject actor)
  throws SignetAuthorityException
  {
    Decision decision = actor.canEdit(this);
    if (decision.getAnswer() == false)
    {
      throw new SignetAuthorityException(decision);
    }
  }
  
  public void setEffectiveDate
    (SignetSubject  actor,
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
    this.setGrantor(actor);
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
    (SignetSubject  actor,
     Date               expirationDate)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
    this.expirationDate = expirationDate;
    this.setGrantor(actor);
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
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Grantable#evaluate()
   */
  public boolean evaluate()
  {
    return (evaluate(new Date()));
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Grantable#evaluate(java.util.Date)
   */
  public boolean evaluate(Date date)
  {
    Status newStatus;

     // The effectiveDate has not yet arrived.
    if (date.compareTo(effectiveDate) < 0)
      newStatus = Status.PENDING;

    // The expirationDate has already passed.
    else if ((expirationDate != null) && (date.compareTo(expirationDate) > 0))
      newStatus = Status.INACTIVE;

    // we're between the effectiveDate and expirationDate
    else
      newStatus = Status.ACTIVE;
    
    return (setStatus(newStatus));
  }
  
//  protected void save(SignetSubject pSubject)
//  {
////TODO Should use (0 == pSubject.getSubjectKey()) instead???
//    if ((pSubject != null) && (pSubject.getId() == null))
//    {
//      pSubject.save();
//    }
//  }

  	public void save()
	{
		this.setModifyDatetime(new Date());
		SignetSubject subj;
		if (null != (subj = getGrantor()))
			subj.save();
		if (null != (subj = getGrantee()))
			subj.save();
		if (null != (subj = getRevoker()))
			subj.save();
		if (null != (subj = getProxy()))
			subj.save();
//		getSignet().getPersistentDB().save(this);
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
