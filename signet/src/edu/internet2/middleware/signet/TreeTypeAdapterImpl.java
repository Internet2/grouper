/*--
  $Id: TreeTypeAdapterImpl.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.tree.AbstractTreeTypeAdapter;
import edu.internet2.middleware.signet.tree.AdapterUnavailableException;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.signet.tree.TreeType;
import edu.internet2.middleware.signet.tree.TreeTypeAdapter;
import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * This implementation of TreeTypeAdapter provides Signet's own native,
 * database-persisted trees.
 */
class TreeTypeAdapterImpl
  extends AbstractTreeTypeAdapter
  implements TreeTypeAdapter
{
  private Signet signet;
  
  public TreeTypeAdapterImpl()
  {
    super();
  }
  
  public TreeTypeAdapterImpl(Signet signet)
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
	(TreeType	treeType,
	 String 	id)
  throws TreeNotFoundException
  {
    Tree tree = null;
    
    try
    {
      tree = this.signet.getNativeSignetTree(treeType, id);
    }
    catch (ObjectNotFoundException onfe)
    {
      throw new TreeNotFoundException(onfe);
    }
    
    if (tree == null)
    {
      throw new TreeNotFoundException
    	  ("The native Signet TreeTypeAdapter was unable to find the"
    	   + " Tree with ID='"
    	   + id
    	   + "'.");
    }
  
    return tree;
  }
  
  public void init() throws AdapterUnavailableException
  {
    // This TreeTypeAdapter has no initialization to perform.
  }
  
  public void destroy()
  {
    // This TreTypeAdapter has no destroy-time actions to perform.
  }
  
  public Tree newTree
	(TreeType		type,
	 String 		id, 
	 String 		name)
  throws OperationNotSupportedException
  {
    if (this.isModifiable() == false)
    {
      throw new OperationNotSupportedException
    	  					("The TreeTypeAdapter '" 
    		  				 + this.getClass().getName() 
    			  			 + "' is read-only. The attempt to create the new"
    				  		 + " Tree '"
    					  	 + id
    						   + "' failed.");
    }
  
    TreeImpl treeImpl = new TreeImpl(type, id, name);
  
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
   * @see edu.internet2.middleware.signet.tree.TreeTypeAdapter#newTreeNode(edu.internet2.middleware.signet.tree.Tree, java.lang.String, java.lang.String, java.lang.String)
   */
  public TreeNode newTreeNode
  	(Tree tree, String id, String name, String type)
  {
    TreeNodeImpl newTreeNode = new TreeNodeImpl(tree, id, name, type);
    ((TreeImpl)tree).getNodes().add(newTreeNode);
    
    return newTreeNode;
  }
}
