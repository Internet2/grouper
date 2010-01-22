/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/tree/Tree.java,v 1.5 2007-10-05 08:27:42 ddonn Exp $

Copyright 2007 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.tree;

import java.io.Serializable;
import java.util.Set;

/**
 * 
 * A Tree is a root {@link TreeNode}, followed by some set of child 
 * {@link TreeNode}s, each of which may have its own children.
 * 
 */
public interface Tree extends Serializable
{    
	/**
	 * @return Returns a short mnemonic ID which will appear in XML
	 * 		documents and other documents used by analysts. This ID
	 * 		uniquely identifies a single Tree.
	 */
	public String getId();
	
	/**
	 * 
	 * @return A printable String, containing the name of this Tree in
	 * 		some default displayable format. The exact details
	 * 		of the representation are unspecified and subject to change.
	 * @throws TreeNotFoundException
	 */
	public String getName()
	  throws TreeNotFoundException;
	
	/**
	 * Add an new top-level TreeNode to this Tree (Tree supports multiple roots)
	 * @param rootNode The TreeNode to add
	 */
	public void addRoot(TreeNode rootNode);
	
	/**
	 * Get the Set of top-level (no parents) TreeNodes for this Tree
	 * @return A Set of top-level TreeNodes for this Tree
	 */
	public Set getRoots();
	
	/**
	 * Get the unordered collection of all TreeNodes belonging to this Tree
	 * @return A Set of TreeNodes
	 */
	public Set getTreeNodes();
	
	/**
	 * Get the TreeNode that matches the nodeId
	 * @return the TreeNode matching the given nodeId
	 */
	public TreeNode getNode(String nodeId);
	
	/**
	 * Get the TreeAdapter class associated with this Tree
	 * @return The TreeAdapter class associated with this Tree
	 */
	public TreeAdapter getAdapter();
	
}
