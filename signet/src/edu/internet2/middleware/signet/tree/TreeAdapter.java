/*--
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/tree/TreeAdapter.java,v 1.3 2006-12-16 01:08:53 ddonn Exp $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.tree;

import javax.naming.OperationNotSupportedException;
import edu.internet2.middleware.signet.AdapterUnavailableException;

/**
* This interface should be implemented by anyone who wants to use some
* {@link Tree} implementation other than the default, database-persistent
* one provided with Signet.
* 
*/
public interface TreeAdapter
{

	public Tree getTree(String id)
		throws TreeNotFoundException;

	public void init()
		throws AdapterUnavailableException;

	boolean isModifiable();

	public Tree newTree
		(String 		id,
		 String 		name)
		throws OperationNotSupportedException;

	/**
	 * @param tree
	 * @param id
	 * @param name
	 */
	TreeNode newTreeNode(Tree tree, String id, String name);

	public void destroy();
}
