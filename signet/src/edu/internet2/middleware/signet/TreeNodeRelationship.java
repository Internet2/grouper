/*--
$Id: TreeNodeRelationship.java,v 1.2 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

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
class TreeNodeRelationship
implements Serializable
{
  private String treeId;
  private String childNodeId;
  private String parentNodeId;
  
  TreeNodeRelationship
  	(String treeId,
  	 String childNodeId,
  	 String parentNodeId)
  {
    super();
    this.treeId = treeId;
    this.childNodeId = childNodeId;
    this.parentNodeId = parentNodeId;
  }
  
  TreeNodeRelationship()
  {
    super();
  }
  
  /**
   * @return Returns the child.
   */
  String getChildNodeId()
  {
    return this.childNodeId;
  }
  /**
   * @param child The child to set.
   */
  private void setChildNodeId(String childNodeId)
  {
    this.childNodeId = childNodeId;
  }
  /**
   * @return Returns the parent.
   */
  String getParentNodeId()
  {
    return this.parentNodeId;
  }
  /**
   * @param parent The parent to set.
   */
  private void setParentNodeId(String parentNodeId)
  {
    this.parentNodeId = parentNodeId;
  }
  
  String getTreeId()
  {
    return this.treeId;
  }
  
  private void setTreeId(String treeId)
  {
    this.treeId = treeId;
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if ( !(obj instanceof TreeNodeRelationship) )
    {
      return false;
    }
    
    TreeNodeRelationship rhs = (TreeNodeRelationship) obj;
    return new EqualsBuilder()
    	.append(this.treeId, rhs.treeId)
      .append(this.childNodeId, rhs.childNodeId)
      .append(this.parentNodeId, rhs.parentNodeId)
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
  		.append(this.treeId)
  		.append(this.childNodeId)
  		.append(this.parentNodeId)
      .toHashCode();
  }
}
