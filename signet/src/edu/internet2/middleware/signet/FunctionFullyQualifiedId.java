/*
 * Created on Dec 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet;

import java.io.Serializable;

/**
 *	Function IDs are composite in nature, naming both the Function
 *	and its enclosing Subsystem. Both parts are required to uniquely
 *	identify a Function.
 *
 *	This code was copied from example code in "Hibernate in Action", page
 *  334.
 */
public class FunctionFullyQualifiedId implements Serializable
{
  private String	subsystemId;
  private String	functionId;

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public FunctionFullyQualifiedId()
  {
      super();
  }
  
  public FunctionFullyQualifiedId
  	(String	subsystemId,
  	 String	functionId)
  {
    this.subsystemId = subsystemId;
    this.functionId = functionId;
  }
  
  public String getSubsystemId()
  {
    return this.subsystemId;
  }
  
  void setSubsystemId(String subsystemId)
  {
    this.subsystemId = subsystemId;
  }
  
  public String getFunctionId()
  {
    return this.functionId;
  }
  
  void setFunctionId(String functionId)
  {
    this.functionId = functionId;
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
    
    if (!(o instanceof FunctionFullyQualifiedId))
    {
      return false;
    }
    
    final FunctionFullyQualifiedId ffqId = (FunctionFullyQualifiedId)o;
    
    if (!subsystemId.equals(ffqId.getSubsystemId()))
    {
      return false;
    }
    
    if (!functionId.equals(ffqId.getFunctionId()))
    {
      return false;
    }
    
    return true;
  }
  
  public int hashCode()
  {
    int hashCode = 0;
    
    if (functionId != null)
    {
    	hashCode = functionId.hashCode();
    }

    return hashCode;
  }
  
  public String toString()
  {
    return
    	"[subsystemId='"
    	+ subsystemId
    	+ "',functionId='"
    	+ functionId
    	+ "']";
  }
}
