/*--
 $Id: GrantableImpl.java,v 1.1 2005-08-25 20:31:35 acohen Exp $
 $Date: 2005-08-25 20:31:35 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.subject.Subject;

abstract class GrantableImpl
extends EntityImpl
implements Grantable
{
  static final int MIN_INSTANCE_NUMBER = 1;

  // GrantableImpl is unusual among Signet entities in that it
  // has a numeric, not alphanumeric ID.
  private Integer						id;
  
  private PrivilegedSubject	grantor;
  private String						grantorId;
  private String						grantorTypeId;
  
  private PrivilegedSubject	grantee;
  private String						granteeId;
  private String						granteeTypeId;
  
  private PrivilegedSubject revoker;
  private String            revokerId;
  private String            revokerTypeId;
  
  private Date							effectiveDate;
  private Date              expirationDate = null;
  private int               instanceNumber = MIN_INSTANCE_NUMBER;
  
  boolean         needsInitialHistoryRecord = false;



  
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
  throws
  	SignetAuthorityException
  {    
    super(signet, null, null, null);

    this.setGrantor(grantor);
    this.setGrantee(grantee);
    
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

  // This method is for use only by Hibernate.
  protected void setGranteeId(String id)
  {
    this.granteeId = id;
  }

  // This method is for use only by Hibernate.
  protected String getGranteeId()
  {
    return this.granteeId;
  }

  // This method is for use only by Hibernate.
  protected String getGranteeTypeId()
  {
    return this.granteeTypeId;
  }

  // This method is for use only by Hibernate.
  protected void setGranteeTypeId(String typeId)
  {
    this.granteeTypeId = typeId;
  }

  // This method is for use only by Hibernate.
  protected void setGrantorId(String id)
  {
    this.grantorId = id;
  }

  // This method is for use only by Hibernate.
  protected String getGrantorId()
  {
    return this.grantorId;
  }

  // This method is for use only by Hibernate.
  protected String getGrantorTypeId()
  {
    return this.grantorTypeId;
  }

  // This method is for use only by Hibernate.
  protected void setGrantorTypeId(String typeId)
  {
    this.grantorTypeId = typeId;
  }

  // This method is for use only by Hibernate.
  protected void setRevokerId(String id)
  {
    this.revokerId = id;
  }

  // This method is for use only by Hibernate.
  protected String getRevokerId()
  {
    return this.revokerId;
  }

  // This method is for use only by Hibernate.
  protected String getRevokerTypeId()
  {
    return this.revokerTypeId;
  }

  // This method is for use only by Hibernate.
  protected void setRevokerTypeId(String typeId)
  {
    this.revokerTypeId = typeId;
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
    if ((this.revoker == null)
        && ((this.revokerTypeId != null) && (this.revokerId != null)))
    {
      Subject subject;
      
      try
      {
        subject
          = this.getSignet().getSubject
              (this.revokerTypeId, this.revokerId);
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
      
      this.revoker
      = new PrivilegedSubjectImpl(this.getSignet(), subject);
    }
    
    return this.revoker;
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
    this.revokerId = revoker.getSubjectId();
    this.revokerTypeId = revoker.getSubjectTypeId();
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
  protected void setId(Integer id)
  {
    this.id = id;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#revoke()
   */
  public void revoke(PrivilegedSubject revoker)
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
  
  protected void checkEditAuthority(PrivilegedSubject actor)
  throws SignetAuthorityException
  {
    Decision decision = actor.canEdit(this);
    if (decision.getAnswer() == false)
    {
      throw new SignetAuthorityException(decision);
    }
  }
  
  public void setEffectiveDate(PrivilegedSubject actor, Date date)
  throws SignetAuthorityException
  {
    checkEditAuthority(actor);
    
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
  public void setExpirationDate(PrivilegedSubject actor, Date expirationDate)
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
  
  boolean needsInitialHistoryRecord()
  {
    return this.needsInitialHistoryRecord;
  }
  
  void needsInitialHistoryRecord(boolean needsInitialHistoryRecord)
  {
    this.needsInitialHistoryRecord = needsInitialHistoryRecord;
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
   * @see edu.internet2.middleware.signet.Assignment#evaluate()
   */
  public Status evaluate()
  {
    Date now = new Date();
    Status newStatus;
    
    if (now.compareTo(this.effectiveDate) < 0)
    {
      // The effectiveDate has not yet arrived.
      newStatus = Status.PENDING;
    }
    else if ((this.expirationDate != null)
             && (now.compareTo(this.expirationDate) > 0))
    {
      // The expirationDate has already passed.
      newStatus = Status.INACTIVE;
    }
    else
    {
      newStatus = Status.ACTIVE;
    }
    
    this.setStatus(newStatus);
    
    return newStatus;
  }
}
