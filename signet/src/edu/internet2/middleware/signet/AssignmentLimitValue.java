/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
  private String 	limitId;
  private String 	value;
  
  AssignmentLimitValue
  	(int assignmentId,
  	 String  limitId,
  	 String  value)
  {
    super();
    this.assignmentId = assignmentId;
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

  String getLimitId()
  {
    return this.limitId;
  }

  private void setLimitId(String limitId)
  {
    this.limitId = limitId;
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
  		.append(this.limitId)
  		.append(this.value)
      .toHashCode();
  }
}
