/*--
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/tree/TreeAdapter.java,v 1.4 2007-06-14 21:39:04 ddonn Exp $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.tree;

import java.io.Serializable;
import javax.naming.OperationNotSupportedException;
import edu.internet2.middleware.signet.AdapterUnavailableException;

/**
* This interface should be implemented by anyone who wants to use some
* {@link Tree} implementation other than the default, database-persistent
* one provided with Signet.
* 
*/
public interface TreeAdapter extends Serializable
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
