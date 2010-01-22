/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/ScopeTreeModel.java,v 1.1 2007-07-18 17:24:39 ddonn Exp $

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

import java.util.HashSet;
import java.util.Set;

/**
 * ScopeTreeModel - A hierarchical structure of a TreeNodeRelationships
 * 
 * @version $Revision: 1.1 $
 * @author $Author: ddonn $
 */
public class ScopeTreeModel
{
	protected Set roots;

	public ScopeTreeModel(TreeImpl tree)
	{
//		Set nodeRelations;
//
//		if ((null == tree) || (null == (nodeRelations = tree.getNodeRelations())))
//			roots = new HashSet();
//		else
//			roots = findRoots(nodeRelations);
	}


	protected Set findRoots(Set nodeRelations)
	{
		HashSet retval = new HashSet();

//		for (Iterator iter = nodeRelations.iterator(); iter.hasNext(); )
//		{
//			TreeNodeRelationship node = (TreeNodeRelationship)iter.next();
//			if (null == node.parentNode)
//				retval.add(node);
//		}

		return (retval);
	}


	public Set getRoots()
	{
		return (roots);
	}


//	public TreeNode getTreeNode(String treeNodeId)
//	{
//		TreeNode retval = null;
//
//		if ((null == treeNodeId) || (0 >= treeNodeId.length()))
//			return (retval);
//		for (Iterator iter = roots;
//	}
//
//	public TreeNode getTreeNode(Set nodeRelations)
//	{
//	}

}
