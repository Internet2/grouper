/*
 * Created on Dec 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
  
  void setLimitId(String functionId)
  {
    this.limitId = functionId;
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
