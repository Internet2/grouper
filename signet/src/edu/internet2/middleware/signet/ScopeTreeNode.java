/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/ScopeTreeNode.java,v 1.1 2007-07-18 17:24:39 ddonn Exp $

Copyright (c) 2007 Internet2, Stanford University

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

import java.util.Set;

/**
 * ScopeTreeNode - runtime wrapper for a TreeNodeRelationship / TreeNodeImpl pair
 * 
 * @version $Revision: 1.1 $
 * @author $Author: ddonn $
 */
public class ScopeTreeNode
{
	/** The real TreeNode */
	protected TreeNodeImpl	treeNode;
	/** The children of this TreeNode */
	protected Set			children;
	/** The parent of this TreeNode */
	protected TreeNodeImpl	parent;



	private ScopeTreeNode()
	{
	}

	public ScopeTreeNode(TreeNodeRelationship tnr)
	{
//		setTreeNode((TreeNodeImpl)tnr.childNode);
////		treeNode.getChildren();
	}

	/**
	 * @return the children
	 */
	public Set getChildren()
	{
		return (children);
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(Set children)
	{
		this.children = children;
	}

	/**
	 * @return the parent
	 */
	public TreeNodeImpl getParent()
	{
		return (parent);
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(TreeNodeImpl parent)
	{
		this.parent = parent;
	}

	/**
	 * @return the treeNode
	 */
	public TreeNodeImpl getTreeNode()
	{
		return (treeNode);
	}

	/**
	 * @param treeNode the treeNode to set
	 */
	public void setTreeNode(TreeNodeImpl treeNode)
	{
		this.treeNode = treeNode;
	}


}
