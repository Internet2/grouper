/*--
  $Id: TreeTypeAdapter.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.tree;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.Subsystem;

/**
 * This interface should be implemented by anyone who wants to use some
 * {@link Tree} implementation other than the default, database-persistent
 * one provided with Signet.
 * 
 */
public interface TreeTypeAdapter
{
  
	public Tree getTree(TreeType type, String id)
	  throws TreeNotFoundException;

	public void init()
		throws AdapterUnavailableException;
	
  boolean isModifiable();
  
  public Tree newTree
  	(TreeType 	type,
  	 String 		id,
  	 String 		name)
  	throws OperationNotSupportedException;
  
  /**
   * @param tree
   * @param id
   * @param name
   * @param type
   */
  TreeNode newTreeNode(Tree tree, String id, String name, String type);

	public void destroy();
}
