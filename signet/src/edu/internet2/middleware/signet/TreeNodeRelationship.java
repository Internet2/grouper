/*--
$Id: TreeNodeRelationship.java,v 1.7 2007-06-14 21:39:04 ddonn Exp $
$Date: 2007-06-14 21:39:04 $

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

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A class that establishes the relationship between a Tree and it's Nodes, and
 * the parent/child relationship between the Nodes.
 */
public class TreeNodeRelationship implements Serializable
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
  public String getChildNodeId()
	{
    return this.childNodeId;
	}

	/**
   * @param childNodeId The child to set.
	 */
  protected void setChildNodeId(String childNodeId)
	{
    this.childNodeId = childNodeId;
	}

	/**
   * @return Returns the parent.
	 */
  public String getParentNodeId()
	{
    return this.parentNodeId;
	}

	/**
   * @param parentNodeId The parent to set.
	 */
  protected void setParentNodeId(String parentNodeId)
	{
    this.parentNodeId = parentNodeId;
	}

  String getTreeId()
	{
    return this.treeId;
	}

  protected void setTreeId(String treeId)
	{
    this.treeId = treeId;
	}

	/////////////////////////////////////
	// overrides Object
	/////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if ( !(obj instanceof TreeNodeRelationship))
			return false;

		TreeNodeRelationship rhs = (TreeNodeRelationship)obj;

		EqualsBuilder eb = new EqualsBuilder();
		eb.append(treeId,		rhs.treeId);
		eb.append(childNodeId,	rhs.childNodeId);
		eb.append(parentNodeId,	rhs.parentNodeId);
		
		return (eb.isEquals());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		// you pick a hard-coded, randomly chosen, non-zero, odd number
		// ideally different for each class
		HashCodeBuilder hcb = new HashCodeBuilder(17, 37);
		hcb.append(treeId);
		hcb.append(childNodeId);
		hcb.append(parentNodeId);

		return (hcb.toHashCode());
	}
}
