/*--
 $Id: ProxyImpl.java,v 1.5 2005-09-13 22:25:36 acohen Exp $
 $Date: 2005-09-13 22:25:36 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class ProxyImpl
extends GrantableImpl
implements Proxy
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
  
  public ProxyImpl
  	(Signet							signet,
     PrivilegedSubject	grantor, 
     PrivilegedSubject  actingAs,
     PrivilegedSubject 	grantee,
     Subsystem          subsystem,
     boolean            canUse,
     boolean            canExtend,
     Date               effectiveDate,
     Date               expirationDate)
  throws
  	SignetAuthorityException
  {  
    super(signet, grantor, actingAs, grantee, effectiveDate, expirationDate);
    
    Set proxyCandidates = new HashSet();
    
    if (actingAs != null)
    {
      proxyCandidates = signet.getExtensibleProxies(actingAs, grantor);
      if (proxyCandidates.size() == 0)
      {
        Decision decision = new DecisionImpl(false, Reason.CANNOT_EXTEND, null);
        throw new SignetAuthorityException(decision);
      }
      
      if (!encompassesSubsystem(proxyCandidates, subsystem))
      {
        throw new IllegalArgumentException
          ("When extending a Proxy to a third party,"
           + " the grantor must hold a Proxy with a NULL Subsystem, or a"
           + " Subsystem which matches the Subsystem"
           + " associated with the new Proxy.");
      }
    }
    
    if ((canUse == false) && (canExtend == false))
    {
      throw new IllegalArgumentException
        ("It is illegal to create a new Proxy with both its canUse"
         + " and canExtend attributes set false.");
    }
    
    this.subsystem = (SubsystemImpl)subsystem;
    this.canUse = canUse;
    this.canExtend = canExtend;
    
    Decision decision = this.getGrantor().canEdit(this);
    
    if (decision.getAnswer() == false)
    {
      throw new SignetAuthorityException(decision);
    }
  }
  
  void setSubsystem(Subsystem subsystem)
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
  
  /**
   * @return A brief description of this AssignmentImpl. The exact details
   * 		of the representation are unspecified and subject to change.
   */
  public String toString()
  {
    return
      "[id=" + getId()
      + ",instance=" + getInstanceNumber()
      + ",subsystem=" + getSubsystem() + "]";
  }
  
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    Proxy other = (Proxy)o;
    int comparisonResult;
    
    PrivilegedSubject thisGrantee = this.getGrantee();
    PrivilegedSubject otherGrantee = other.getGrantee();
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
    
    Integer thisId = this.getId();
    Integer otherId = other.getId();
    comparisonResult = thisId.compareTo(otherId);
    
    return comparisonResult;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Assignment#findDuplicates()
   */
  public Set findDuplicates()
  {
    return this.getSignet().findDuplicates(this);
  }
  
  public void save()
  {
    this.setModifyDatetime(new Date());
      
    if (this.getId() != null)
    {
      // This isn't the first time we've saved this Proxy.
      // We'll increment the instance-number accordingly, and save
      // its history-record right now (just after we save the Proxy
      // record itself, so as to avoid hitting any referential-integrity
      // problems in the database).
      this.incrementInstanceNumber();
        
      ProxyHistory historyRecord
        = new ProxyHistory(this);

      this.getSignet().save(this);
      this.getSignet().save(historyRecord);
    }
    else
    {
      // We can't construct the Assignment's initial history-record yet,
      // because we don't yet know the ID of the assignment. We'll set a
      // flag that will cause us to construct and save that history-record
      // later, in the postFlush() method of the Hibernate Interceptor.
      this.needsInitialHistoryRecord(true);
      this.getSignet().save(this);
    }
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
  protected void setCanExtend(boolean canExtend)
  {
    this.canExtend = canExtend;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Proxy#setCanExtend(edu.internet2.middleware.signet.PrivilegedSubject, boolean)
   */
  public void setCanExtend
    (PrivilegedSubject  editor,
     boolean            canExtend)
  throws SignetAuthorityException
  {
    checkEditAuthority(editor);
    
    this.canExtend = canExtend;
    this.setGrantor(editor);
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
  protected void setCanUse(boolean canUse)
  {
    this.canUse = canUse;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Proxy#setCanUse(edu.internet2.middleware.signet.PrivilegedSubject, boolean)
   */
  public void setCanUse
    (PrivilegedSubject  editor,
     boolean            canUse)
  throws SignetAuthorityException
  {
    checkEditAuthority(editor);
    
    super.setGrantor(editor);
    this.canUse = canUse;
  }
}
