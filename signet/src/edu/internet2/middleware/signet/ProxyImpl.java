/*--
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/ProxyImpl.java,v 1.22 2008-05-17 20:54:09 ddonn Exp $
 
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
import edu.internet2.middleware.signet.resource.ResLoaderApp;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

public class ProxyImpl extends GrantableImpl implements Proxy
{  
  private SubsystemImpl subsystem;
  private boolean       canExtend;
  private boolean       canUse;
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public ProxyImpl()
  {
    super();
  }
  
  /** Constructor
	 * @param signet
	 * @param grantor
	 * @param grantee
	 * @param subsystem
	 * @param canUse
	 * @param canExtend
	 * @param effectiveDate
	 * @param expirationDate
	 * @throws SignetAuthorityException
	 */
  public ProxyImpl
  	(Signet                 signet,
     SignetSubject			grantor, 
     SignetSubject   	    grantee,
     Subsystem              subsystem,
     boolean                canUse,
     boolean                canExtend,
     Date                   effectiveDate,
     Date                   expirationDate)
  throws
  	SignetAuthorityException
  {  
    super
      (signet,
       grantor,
       grantee,
       effectiveDate,
       expirationDate);
    
    if (!grantor.equals(grantor.getEffectiveEditor()))
    {
      // At this point, we know the following things:
      //
      //   1) This grantor is "acting as" some other PrivilegedSubject.
      //
      //   2) This grantor does indeed hold at least one currently active Proxy
      //      from the PrivilegedSubject that he/she is "acting as". That was
      //      confirmed when PrivilegedSubject.setActingFor() was executed.
      //
      // We still need to confirm the following points:
      //
      //   3) At least one of those Proxies described in (2) above must have its
      //      "can extend" flag set, thereby allowing this grantor to use that
      //      Proxy to grant some new Proxy.
      //
      //   4) At least one of the Proxies described in (3) above must encompass
      //      the Subsystem of this Assignment.

      Reason[] reasonArray = new Reason[1];
      if (!grantor.hasExtensibleProxy
            (grantor.getEffectiveEditor(),
             subsystem,
             reasonArray))
      {
        Decision decision = new DecisionImpl(false, reasonArray[0], null);
        throw new SignetAuthorityException(decision);
      }
    }

    if ( !(canUse | canExtend)) // if can't use and can't extend (yes, I do mean '|' (arithmetic OR))
    {
      throw new IllegalArgumentException(
    		  ResLoaderApp.getString("ProxyImpl.ProxyImpl.CantUseOrExtend")); //$NON-NLS-1$
    }

    this.subsystem = (SubsystemImpl)subsystem;
    this.canUse = canUse;
    this.canExtend = canExtend;

    Decision decision = grantor.canEdit(this);

    if ( !decision.getAnswer())
    {
      throw new SignetAuthorityException(decision);
    }

	setInstanceNumber(MIN_INSTANCE_NUMBER - 1); // createHistory bumps instance number
	addHistoryRecord(createHistoryRecord());
  }

  public void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = (SubsystemImpl)subsystem;
  }

  public Subsystem getSubsystem()
  {
    if ((this.subsystem != null) && (this.getSignet() != null))
    {
      this.subsystem.setSignet(this.getSignet());
    }

    return this.subsystem;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    Proxy other = (Proxy)o;
    int comparisonResult;
    
    SignetSubject thisGrantee = this.getGrantee();
    SignetSubject otherGrantee = other.getGrantee();
    comparisonResult = thisGrantee.compareTo(otherGrantee);
    
    if (comparisonResult != 0)
    {
      return comparisonResult;
    }
    
    Subsystem thisSubsystem = this.getSubsystem();
    Subsystem otherSubsystem = other.getSubsystem();
    
    if (thisSubsystem == otherSubsystem)
    {
      comparisonResult = 0;
    }
    else if (thisSubsystem != null)
    {
      comparisonResult = thisSubsystem.compareTo(otherSubsystem);
    }
    else
    {
      comparisonResult = -1;
    }
    
    if (comparisonResult != 0)
    {
      return comparisonResult;
    }
    
    // This last clause is here to distinguish two Proxies which are 
    // otherwise identical twins:
    
    Integer thisId = null;
    Integer otherId = null;
    if (null == (thisId = this.getId()))
    	comparisonResult = -1;
    else if (null == (otherId = other.getId()))
    	comparisonResult = 1;

    return ((0 != comparisonResult) ? comparisonResult : thisId.compareTo(otherId));
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#findDuplicates()
   */
  public Set findDuplicates()
  {
    return (getSignet().getPersistentDB().findDuplicates(this));
  }


	/*
	 * (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Grantable#createHistoryRecord()
	 */
	public History createHistoryRecord()
	{
		incrementInstanceNumber();
		return (new ProxyHistoryImpl(this));
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Grantable#addHistoryRecord(edu.internet2.middleware.signet.History)
	 */
	public void addHistoryRecord(History histRecord)
	{
		if (null != histRecord)
			getHistory().add(histRecord);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Assignment#getActualStartDatetime()
	 */
  public Date getActualStartDatetime()
  {
    throw new UnsupportedOperationException(
    		ResLoaderApp.getString("general.method.not.implemented")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#getActualEndDatetime()
   */
  public Date getActualEndDatetime()
  {
    throw new UnsupportedOperationException(
    		ResLoaderApp.getString("general.method.not.implemented")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#inactivate()
   */
  public void inactivate()
  {
    throw new UnsupportedOperationException(
    		ResLoaderApp.getString("general.method.not.implemented")); //$NON-NLS-1$
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Proxy#canExtend()
   */
  public boolean canExtend()
  {
    return this.canExtend;
  }
  
  // This method is only for use by Hibernate.
  protected boolean getCanExtend()
  {
    return this.canExtend;
  }
  
  /* This method is for use only by Hibernate. */
  public void setCanExtend(boolean canExtend)
  {
    this.canExtend = canExtend;
  }

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Proxy#setCanExtend(edu.internet2.middleware.signet.PrivilegedSubject, boolean)
	 */
	public void setCanExtend(SignetSubject editor, boolean canExtend) throws SignetAuthorityException
	{
		checkEditAuthority(editor);

		this.canExtend = canExtend;

		setGrantor(editor);
	}

	/* (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Proxy#canUse()
	 */
  public boolean canUse()
  {
    return this.canUse;
  }
  
  // This method is only for use by Hibernate.
  protected boolean getCanUse()
  {
    return this.canUse;
  }
  
  /* This method is only for use by Hibernate. */
  public void setCanUse(boolean canUse)
  {
    this.canUse = canUse;
  }

	/*
	 * (non-Javadoc)
	 * @see edu.internet2.middleware.signet.Proxy#setCanUse(edu.internet2.middleware.signet.PrivilegedSubject, boolean)
	 */
	public void setCanUse(SignetSubject editor, boolean canUse) throws SignetAuthorityException
	{
		checkEditAuthority(editor);

		this.canUse = canUse;

		setGrantor(editor);
	}


	// ///////////////////////////////////
	// overrides Object
	/////////////////////////////////////

	/**
	 * @return Returns the contents of ProxyImpl including the values from
	 * it's super classes.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer(super.toString());
		buf.append(", subsystem=" + (null != subsystem ? subsystem.toString() : "null"));
		buf.append(", canExtend=" + canExtend);
		buf.append(", canUse=" + canUse);
		return (buf.toString());
	}
  

}
