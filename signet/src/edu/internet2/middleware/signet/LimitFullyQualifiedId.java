/*--
$Id: LimitFullyQualifiedId.java,v 1.3 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.io.Serializable;

/**
 *	Limit IDs are composite in nature, naming both the Limit
 *	and its enclosing Subsystem. Both parts are required to uniquely
 *	identify a Limit.
 *
 *	This code was copied from example code in "Hibernate in Action", page
 *  334.
 */
class LimitFullyQualifiedId implements Serializable
{
  private String	subsystemId;
  private String	limitId;

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public LimitFullyQualifiedId()
  {
      super();
  }
  
  public LimitFullyQualifiedId
  	(String	subsystemId,
  	 String	limitId)
  {
    this.subsystemId = subsystemId;
    this.limitId = limitId;
  }
  
  public String getSubsystemId()
  {
    return this.subsystemId;
  }
  
  void setSubsystemId(String subsystemId)
  {
    this.subsystemId = subsystemId;
  }
  
  public String getLimitId()
  {
    return this.limitId;
  }
  
  void setLimitId(String limitId)
  {
    this.limitId = limitId;
  }
  
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    
    if (o == null)
    {
      return false;
    }
    
    if (!(o instanceof LimitFullyQualifiedId))
    {
      return false;
    }
    
    final LimitFullyQualifiedId lfqId = (LimitFullyQualifiedId)o;
    
    if (!subsystemId.equals(lfqId.getSubsystemId()))
    {
      return false;
    }
    
    if (!limitId.equals(lfqId.getLimitId()))
    {
      return false;
    }
    
    return true;
  }
  
  public int hashCode()
  {
    int hashCode = 0;
    
    if (limitId != null)
    {
    	hashCode = limitId.hashCode();
    }

    return hashCode;
  }
  
  public String toString()
  {
    return
    	"[subsystemId='"
    	+ subsystemId
    	+ "',limitId='"
    	+ limitId
    	+ "']";
  }
}
