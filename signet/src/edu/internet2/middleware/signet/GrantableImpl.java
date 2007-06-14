/*--
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/GrantableImpl.java,v 1.22 2007-06-14 21:39:04 ddonn Exp $
 
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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

/**
 * The abstract base class for all grantable entities.
 * NOTE: This class and all the other subclasses of EntityImpl should be 
 * rearchitected to better handle the notion of "id". GrantableImpl and it's 
 * subclasses use an integer-based id whereas EntityImpl uses a String-based id. 
 */
public abstract class GrantableImpl extends EntityImpl implements Grantable
{
	/** Starting number for instance numbering */
	public static final int		MIN_INSTANCE_NUMBER = 1;

	/** Database primary key. GrantableImpl is unusual among Signet entities in
	 * that it has a numeric, not alphanumeric ID.
	 * Note!! Overrides/masks super.id (a dangerous practice) */
	private Integer			id;

	/** If this Grantable instance was granted directly by a PrivilegedSubject,
	 * then this is that PrivilegedSubject. If this Grantable instance was
	 * granted by an "acting as" Subject, then this is the logged-in PrivilegedSubject. */
	protected SignetSubject	grantor;

	/** If this Grantable instance was granted/revoked directly by a PrivilegedSubject,
	 * then this is null. If this Grantable instance was granted/revoked by an
	 * "acting as" Subject, then this is the "acting as" PrivilegedSubject. */
	protected SignetSubject	proxy;

	/** The recipient of this grant */
	protected SignetSubject	grantee;

	/** The revoker of this grant */
	protected SignetSubject	revoker;

	protected Date			effectiveDate;
	protected Date			expirationDate;
	protected int			instanceNumber;
	protected Set			history;


	/**
	 * Hibernate requires the presence of a default constructor.
	 */
	public GrantableImpl()
	{
		super();
		instanceNumber = MIN_INSTANCE_NUMBER;
		setHistory(new HashSet());
	}
  
	/**
	 * Constructor
	 * @param signet
	 * @param grantor
	 * @param grantee
	 * @param effectiveDate
	 * @param expirationDate
	 */
	public GrantableImpl(Signet signet, SignetSubject grantor,
			SignetSubject grantee, Date effectiveDate, Date expirationDate)
	{
		super(signet, null, null, null);

		instanceNumber = MIN_INSTANCE_NUMBER;
		setHistory(new HashSet());

		setGrantor(grantor);

		this.setGrantee(grantee);

		if (effectiveDate == null)
		{
			throw new IllegalArgumentException("An effective-date may not be NULL.");
		}
		if (datesInWrongOrder(effectiveDate, expirationDate))
		{
			throw new IllegalArgumentException("An expiration-date must be NULL or later than its"
					+ " effective-date. The requested expiration-date '" + expirationDate
					+ "' is neither NULL nor later than the requested effective-date '" + effectiveDate + "'.");
		}
		this.effectiveDate = effectiveDate;
		this.expirationDate = expirationDate;
		setStatus(determineStatus(effectiveDate, expirationDate));
		setModifyDatetime(new Date());
	}

	////////////////////////////////////
	// Grantor methods
	////////////////////////////////////

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Grantable#getGrantor()
	 */
	public SignetSubject getGrantor()
	{
		return (grantor);
	}

	/**
	 * Set the grantorId field and attempt to set proxyId for grantor's "acting as".
	 * @param grantor
	 */
	public void setGrantor(SignetSubject grantor)
	{
		this.grantor = grantor.getEffectiveEditor();
		if ( !(grantor.equals(this.grantor)))
			setProxy(grantor);
		else
			setProxy(null);
	}


	////////////////////////////////////
	// Proxy methods
	////////////////////////////////////

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Grantable#getProxy()
	 */
	public SignetSubject getProxy()
	{
		return (proxy);
	}
  
	/**
	 * Set the proxyId to that primary key of the incoming proxy.
	 * @param proxy
	 */
	public void setProxy(SignetSubject proxy)
	{
		this.proxy = proxy;
	}


	////////////////////////////////////
	// Grantee methods
	////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Assignment#getGrantee()
	 */
	public SignetSubject getGrantee()
	{
		return (grantee);
	}

	/**
	 * @param grantee The grantee to set.
	 */
	public void setGrantee(SignetSubject grantee)
	{
		this.grantee = grantee;
	}
 

	////////////////////////////////////
	// Revoker methods
	////////////////////////////////////

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Grantable#getRevoker()
	 */
	public SignetSubject getRevoker()
	{
		return (revoker);
	}

	/**
	 * @param revoker
	 */
	void setRevoker(SignetSubject revoker)
	{
		if (null != revoker)
		{
			this.revoker = revoker.getEffectiveEditor();
			if ( !revoker.equals(this.revoker))
				setProxy(revoker);
		}
		else
			this.revoker = revoker;
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

	/**
	 * @param actor
	 * @throws NullPointerException, SignetAuthorityException
	 */
	public void checkEditAuthority(SignetSubject actor)
		throws NullPointerException, SignetAuthorityException
	{
		if (null == actor)
			throw new NullPointerException("No SignetSubject specified");

		Decision decision = actor.canEdit(this);
		if (decision.getAnswer() == false)
		{
			throw new SignetAuthorityException(decision);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Grantable#setEffectiveDate(edu.internet2.middleware.signet.subjsrc.SignetSubject,
	 * java.util.Date)
	 */
	public void setEffectiveDate(SignetSubject actor, Date date, boolean checkAuth)
			throws SignetAuthorityException
	{
		if (checkAuth)
			checkEditAuthority(actor);

		if (date == null)
		{
			throw new IllegalArgumentException("effectiveDate must have a non-NULL value.");
		}

		this.effectiveDate = date;

		setGrantor(actor);
//		setGrantorId(actor.getSubject_PK());
//		setProxyForEffectiveEditor(actor);
		// this.setGrantor(actor);
  }



	  /** This method is for use only by Hibernate.
	 * @param date
	 */
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

	/*
	 * (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Assignment#setExpirationDate(java.util.Date)
	 */
	public void setExpirationDate(SignetSubject actor, Date expirationDate, boolean checkAuth)
			throws SignetAuthorityException
	{
		if (checkAuth)
			checkEditAuthority(actor);

		this.expirationDate = expirationDate;

		setGrantor(actor);
//		setGrantorId(actor.getSubject_PK());
//		setProxyForEffectiveEditor(actor);
		// this.setGrantor(actor);
	}


	/**
	 * Returns a status based upon now, effectiveDate, and expirationDate
	 * @param effectiveDate
	 * @param expirationDate
	 * @return Status.PENDING, Status.INACTIVE, or Status.ACTIVE
	 */
	protected Status determineStatus(Date effectiveDate, Date expirationDate)
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

	/**
	 * @param effectiveDate
	 * @param expirationDate
	 * @return True if expirationDate precedes effectiveDate
	 */
	protected boolean datesInWrongOrder(Date effectiveDate, Date expirationDate)
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


	  /**
	 * @return The instance number of this Grantable
	 */
  int getInstanceNumber()
  {
    return this.instanceNumber;
  }

	  /** This method is for use only by Hibernate.
	 * @param instanceNumber
	 */
  protected void setInstanceNumber(int instanceNumber)
  {
    this.instanceNumber = instanceNumber;
  }


	/** This method is for use only by Hibernate.
	 * @param expirationDate
	 */
  protected void setExpirationDate(Date expirationDate)
  {
    this.expirationDate = expirationDate;
  }

	/**
	 * Increment the instance number
	 */
  protected void incrementInstanceNumber()
  {
    this.instanceNumber++;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getActualStartDatetime()
   */
  public Date getActualStartDatetime()
  {
    throw new UnsupportedOperationException
      (ResLoaderApp.getString("general.method.not.implemented")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getActualEndDatetime()
   */
  public Date getActualEndDatetime()
  {
    throw new UnsupportedOperationException
      (ResLoaderApp.getString("general.method.not.implemented")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#inactivate()
   */
  public void inactivate()
  {
    throw new UnsupportedOperationException
      (ResLoaderApp.getString("general.method.not.implemented")); //$NON-NLS-1$
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


	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Grantable#getHistory()
	 */
  public Set getHistory()
  {
    return this.history;
  }

	/** Set the History records for this Grantable
	 * @param history
	 */
  protected void setHistory(Set history)
  {
    this.history = history;
  }


	/////////////////////////////////////////
	// overrides Object
	/////////////////////////////////////////

	public String toString()
	{
		StringBuffer buf = new StringBuffer(super.toString());
		buf.append(", id(GrantableImpl)=" + id.toString()); //$NON-NLS-1$
		buf.append(", grantorId=" + grantor.getId()); //$NON-NLS-1$
		buf.append(", granteeId=" + grantee.getId()); //$NON-NLS-1$
		buf.append(", proxyId=" + (proxy != null ? proxy.getId() : "none")); //$NON-NLS-1$ $NON-NLS-2$
		buf.append(", revokerID=" + (revoker != null ? revoker.getId() : "none")); //$NON-NLS-1$ $NON-NLS-2$
//		buf.append(", grantorId=" + grantorId); //$NON-NLS-1$
//		buf.append(", granteeId=" + granteeId); //$NON-NLS-1$
//		buf.append(", proxyId=" + (proxyId != null ? proxyId.toString() : "null")); //$NON-NLS-1$ $NON-NLS-2$
//		buf.append(", revokerID=" + (revokerId != null ? revokerId.toString() : "null")); //$NON-NLS-1$ $NON-NLS-2$
		DateFormat df = DateFormat.getDateInstance();
		buf.append(", effectiveDate=" + (null != effectiveDate ? df.format(effectiveDate) : "null")); //$NON-NLS-1$ $NON-NLS-2$
		buf.append(", expirationDate=" + (null != expirationDate ? df.format(expirationDate) : "null")); //$NON-NLS-1$ $NON-NLS-2$
		buf.append(", instanceNumber=" + instanceNumber); //$NON-NLS-1$
		if (null != history)
		{
			buf.append(", historyInstanceNumbers=["); //$NON-NLS-1$
			for (Iterator hists = history.iterator(); hists.hasNext(); )
			{
				HistoryImpl hist = (HistoryImpl)hists.next();
				buf.append(hist.getInstanceNumber());
				if (hists.hasNext())
					buf.append(", ");
			}
			buf.append("]"); //$NON-NLS-1$
		}
		return (buf.toString());
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof GrantableImpl))
		{
			return false;
		}
		GrantableImpl rhs = (GrantableImpl)obj;
		return new EqualsBuilder().append(this.id, rhs.id).isEquals();
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		// you pick a hard-coded, randomly chosen, non-zero, odd number
		// ideally different for each class
		return new HashCodeBuilder(17, 37).append(this.id).toHashCode();
	}

}
