/*--
$Id: ChoiceSetFullyQualifiedId.java,v 1.2 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet;

import java.io.Serializable;

/**
 *	ChoiceSet IDs are composite in nature, naming both the ChoiceSet
 *	and its enclosing Subsystem. Both parts are required to uniquely
 *	identify a ChoiceSet.
 *
 *	This code was copied from example code in "Hibernate in Action", page
 *  334.
 */
class ChoiceSetFullyQualifiedId implements Serializable
{
  private String	subsystemId;
  private String	choiceSetId;

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public ChoiceSetFullyQualifiedId()
  {
      super();
  }
  
  public ChoiceSetFullyQualifiedId
  	(String	subsystemId,
  	 String	choiceSetId)
  {
    this.subsystemId = subsystemId;
    this.choiceSetId = choiceSetId;
  }
  
  public String getSubsystemId()
  {
    return this.subsystemId;
  }
  
  void setSubsystemId(String subsystemId)
  {
    this.subsystemId = subsystemId;
  }
  
  public String getChoiceSetId()
  {
    return this.choiceSetId;
  }
  
  void setChoiceSetId(String choiceSetId)
  {
    this.choiceSetId = choiceSetId;
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
    
    if (!(o instanceof ChoiceSetFullyQualifiedId))
    {
      return false;
    }
    
    final ChoiceSetFullyQualifiedId csfqId = (ChoiceSetFullyQualifiedId)o;
    
    if (!subsystemId.equals(csfqId.getSubsystemId()))
    {
      return false;
    }
    
    if (!choiceSetId.equals(csfqId.getChoiceSetId()))
    {
      return false;
    }
    
    return true;
  }
  
  public int hashCode()
  {
    int hashCode = 0;
    
    if (choiceSetId != null)
    {
    	hashCode = choiceSetId.hashCode();
    }

    return hashCode;
  }
  
  public String toString()
  {
    return
    	"[subsystemId='"
    	+ subsystemId
    	+ "',choiceSetId='"
    	+ choiceSetId
    	+ "']";
  }
}
