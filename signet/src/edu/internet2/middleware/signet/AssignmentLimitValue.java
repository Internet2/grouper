/*--
$Id: AssignmentLimitValue.java,v 1.3 2005-02-25 18:42:02 acohen Exp $
$Date: 2005-02-25 18:42:02 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AssignmentLimitValue
implements Serializable
{
  private int     assignmentId;
  private String  limitSubsystemId;
  private String 	limitId;
  private String 	value;
  
  AssignmentLimitValue
  	(int assignmentId,
  	 String	limitSubsystemId,
  	 String	limitId,
  	 String	value)
  {
    super();
    this.assignmentId = assignmentId;
    this.limitSubsystemId = limitSubsystemId;
    this.limitId = limitId;
    this.value = value;
  }
  
  AssignmentLimitValue()
  {
    super();
  }

  int getAssignmentId()
  {
    return this.assignmentId;
  }

  private void setAssignmentId(int assignmentId)
  {
    this.assignmentId = assignmentId;
  }

  String getLimitSubsystemId()
  {
    return this.limitSubsystemId;
  }

  private void setLimitSubsystemId(String limitSubsystemId)
  {
    this.limitSubsystemId = limitSubsystemId;
  }

  String getLimitId()
  {
    return this.limitId;
  }

  private void setLimitId(String limitId)
  {
    this.limitId = limitId;
  }

  String getLimitType()
  {
    /**
     * Someday, the "limitType" attribute of this class will indicate
     * whether this Limit has the shape of a Tree or a ChoiceSet.
     * Not yet, though.
     */
    return "reserved";
  }

  private void setLimitType(String limitId)
  {
    /**
     * Someday, the "limitType" attribute of this class will indicate
     * whether this Limit has the shape of a Tree or a ChoiceSet.
     * Not yet, though.
     */
  }
  
  String getValue()
  {
    return this.value;
  }
  
  private void setValue(String value)
  {
    this.value = value;
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if ( !(obj instanceof AssignmentLimitValue) )
    {
      return false;
    }
    
    AssignmentLimitValue rhs = (AssignmentLimitValue) obj;
    return new EqualsBuilder()
    	.append(this.assignmentId, rhs.assignmentId)
      .append(this.limitSubsystemId, rhs.limitSubsystemId)
      .append(this.limitId, rhs.limitId)
      .append(this.value, rhs.value)
      .isEquals();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37)
  		.append(this.assignmentId)
  		.append(this.limitSubsystemId)
  		.append(this.limitId)
  		.append(this.value)
      .toHashCode();
  }
}
