/*--
$Id: TreeAdapterImpl.java,v 1.2 2005-01-12 23:47:28 mnguyen Exp $
$Date: 2005-01-12 23:47:28 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.AdapterUnavailableException;
import edu.internet2.middleware.signet.tree.AbstractTreeAdapter;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.signet.tree.TreeAdapter;
import edu.internet2.middleware.signet.tree.TreeNode;

/**
* This implementation of TreeAdapter provides Signet's own native,
* database-persisted trees.
*/
class TreeAdapterImpl
	extends AbstractTreeAdapter
	implements TreeAdapter
{
	private Signet signet;

public TreeAdapterImpl()
{
  super();
}

public TreeAdapterImpl(Signet signet)
{
  super();
  this.signet = signet;
}

void setSignet(Signet signet)
{
  this.signet = signet;
}

public boolean isModifiable()
{
  // Native Signet trees are always modifiable.
  return true;
}

public Tree getTree
(String 	id)
throws TreeNotFoundException
{
  Tree tree = null;
  
  try
  {
    tree = this.signet.getNativeSignetTree(id);
  }
  catch (ObjectNotFoundException onfe)
  {
    throw new TreeNotFoundException(onfe);
  }
  
  if (tree == null)
  {
    throw new TreeNotFoundException
  	  ("The native Signet TreeAdapter was unable to find the"
  	   + " Tree with ID='"
  	   + id
  	   + "'.");
  }

  return tree;
}

public void init() throws AdapterUnavailableException
{
  // This TreeAdapter has no initialization to perform.
}

public void destroy()
{
  // This TreTypeAdapter has no destroy-time actions to perform.
}

public Tree newTree
(String 		id, 
 String 		name)
throws OperationNotSupportedException
{
  if (this.isModifiable() == false)
  {
    throw new OperationNotSupportedException
  	  					("The TreeAdapter '" 
  		  				 + this.getClass().getName() 
  			  			 + "' is read-only. The attempt to create the new"
  				  		 + " Tree '"
  					  	 + id
  						   + "' failed.");
  }

  TreeImpl treeImpl = new TreeImpl(signet, this, id, name);

  // Signet application programs will never explicitly persist a new
  // Tree, because they won't know where that Tree actually
  // resides. That's why we're doing it here, for a Tree that we know
  // resides in the SQL database. Signet transactiona always nest, so this
  // operation will either be part of some larger transaction that's
  // already in progress, or will commit as its own small transaction.
  this.signet.beginTransaction();
  this.signet.save(treeImpl);
  this.signet.commit();

  return treeImpl;
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.signet.tree.TreeAdapter#newTreeNode(edu.internet2.middleware.signet.tree.Tree, java.lang.String, java.lang.String, java.lang.String)
 */
public TreeNode newTreeNode
	(Tree tree, String id, String name)
{
  TreeNodeImpl newTreeNode
  	= new TreeNodeImpl(this.signet, tree, id, name);
  ((TreeImpl)tree).getNodes().add(newTreeNode);
  
  return newTreeNode;
}
}
