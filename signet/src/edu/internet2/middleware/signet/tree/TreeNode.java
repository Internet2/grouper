/*--
  $Id: TreeNode.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.tree;

import java.util.Set;

/**
 * A TreeNode has a type, 0 or more parents, and 0 or more children.
 * 
 */
public interface TreeNode extends Comparable
{
    /**
     * @return An analyst-defined string, used as a tag that is
     * appear in XML documents, in case a tree has a mix of parts
     * and you want to declare the kind of thing a specific node is.
     * 
     * For example, in an org tree one might say school vs
     * department vs office...and we had our project vs task
     * vs award example for the financial tree.
     */
    public String	getType();
    public String	getId();
    public String	getName();
    public Tree		getTree();
    public Set		getParents();
    public Set		getChildren();
    /**
     * @param treeNode
     */
    public void addChild(TreeNode treeNode);
    
    public boolean isAncestorOf(TreeNode treeNode);
    public boolean isAncestorOfAll(Set treeNodes);
    
    public boolean isDescendantOf(TreeNode treeNode);
}
