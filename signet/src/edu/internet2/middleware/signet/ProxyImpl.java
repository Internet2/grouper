/*--
$Id: ProxyImpl.java,v 1.13 2006-05-15 21:22:11 ddonn Exp $
$Date: 2006-05-15 21:22:11 $
 
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
import java.util.HashSet;
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
  	(Signet                 signet,
     PrivilegedSubjectImpl  grantor, 
     PrivilegedSubject 	    grantee,
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

    ProxyHistory historyRecord = new ProxyHistoryImpl(this);
  
    Set historySet = new HashSet(1);
    historySet.add(historyRecord);
    this.setHistory(historySet);
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
    return this.getSignet().findDuplicates(this);
  }
  
  public void save()
  {
    this.setModifyDatetime(new Date());

    save(this.getGrantor());
    save(this.getGrantee());
    save(this.getRevoker());
    save(this.getProxy());
      
    if (this.getId() != null)
    {
      // This isn't the first time we've saved this Proxy.
      // We'll increment the instance-number accordingly, and save
      // its history-record right now (just after we save the Proxy
      // record itself, so as to avoid hitting any referential-integrity
      // problems in the database).
      this.incrementInstanceNumber();
        
      ProxyHistory historyRecord
        = new ProxyHistoryImpl(this);
      Set historySet = this.getHistory();
      historySet.add(historyRecord);
      this.setHistory(historySet);
      
      this.getSignet().save(this);
      // this.getSignet().save(historyRecord);
    }
    else
    {
      // We can't construct the Assignment's initial history-record yet,
      // because we don't yet know the ID of the assignment. We'll detect that
      // condition, and construct and save that history-record
      // later, in the postFlush() method of the Hibernate Interceptor.
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
     boolean                canExtend)
  throws SignetAuthorityException
  {
    checkEditAuthority(editor);
    
    this.canExtend = canExtend;
    this.setGrantor((PrivilegedSubjectImpl)editor);
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
    
    super.setGrantor((PrivilegedSubjectImpl)editor);
    this.canUse = canUse;
  }
}
