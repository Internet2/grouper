/*--
  $Id: TreeNodeImpl.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.set.UnmodifiableSet;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;

class TreeNodeImpl 
extends EntityImpl
implements TreeNode, Comparable
{
  
  private TreeImpl		tree;
  private String 			type;
  private Set 				parents;
  private Set 				children;

  /**
   * 
   */
  public TreeNodeImpl()
  {
    super();
    this.parents = new HashSet();
    this.children = new HashSet();
    this.setStatus(Status.PENDING);
  }

  public TreeNodeImpl(Tree tree, String id, String name, String type)
  {
    super();
    this.tree = (TreeImpl)tree;
    this.setId(id);
    this.setName(name);
    this.type = type;
    this.parents = new HashSet();
    this.children = new HashSet();
    this.setStatus(Status.PENDING);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#getType()
   */
  public String getType()
  {
    return type;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#getParents()
   */
  /**
   * TODO - Hibernate requires that getters and setters for collections
   * return the EXACT SAME collection, not just an identical one. Failure
   * to do this makes Hibernate think that the collection has been modified,
   * and causes the entire collection to be re-persisted in the database.
   * 
   * I need to find some way to tell Hibernate to use a specific non-public
   * getter, so that the public getter can resume returning a non-modifiable
   * copy of the collection. 
   */
  public Set getParents()
  {
    return this.parents;
    // return UnmodifiableSet.decorate(this.parents);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#getChildren()
   */
  /**
   * TODO - Hibernate requires that getters and setters for collections
   * return the EXACT SAME collection, not just an identical one. Failure
   * to do this makes Hibernate think that the collection has been modified,
   * and causes the entire collection to be re-persisted in the database.
   * 
   * I need to find some way to tell Hibernate to use a specific non-public
   * getter, so that the public getter can resume returning a non-modifiable
   * copy of the collection. 
   */
  public Set getChildren()
  {
    return this.children;
    // return UnmodifiableSet.decorate(this.children);
  }

  /**
   * @return Returns the tree.
   */
  public Tree getTree()
  {
    return this.tree;
  }
  
  /**
   * @param tree The tree to set.
   */
  void setTree(Tree tree)
  {
    this.tree = (TreeImpl)tree;
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#setType(java.lang.String)
   */
  void setType(String type)
  {
    this.type = type;
  }
  
  /**
   * @param children The children to set.
   */
  void setChildren(Set children)
  {
    this.children = children;
  }
  
  /**
   * @param parents The parents to set.
   */
  void setParents(Set parents)
  {
    this.parents = parents;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#addChild(edu.internet2.middleware.signet.tree.TreeNode)
   */
  public void addChild(TreeNode childNode)
  {
    this.children.add(childNode);
    ((TreeNodeImpl)childNode).parents.add(this);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#isAncestorOf(edu.internet2.middleware.signet.tree.TreeNode)
   */
  public boolean isAncestorOf(TreeNode possibleDescendant)
  {
    // If we can find the possibleDescendant in this node's subtree, then
    // this node is an ancestor of that possibleDescendant.
    
    boolean foundDescendant = false;
    
    if (possibleDescendant == null)
    {
      // No one can be an ancestor of a NULL node.
      throw new IllegalArgumentException
      	("It is illegal to inquire whether a NULL TreeNode"
      	 + " is the descendant of some other node.");
    }
    
    if (this.children.contains(possibleDescendant))
    {
      foundDescendant = true;
    }
    else
    {
      Iterator childrenIterator = this.children.iterator();
      
      while (childrenIterator.hasNext() && (foundDescendant == false))
      {
        foundDescendant
        	= ((TreeNodeImpl)(childrenIterator.next()))
        			.isAncestorOf(possibleDescendant);
      }
    }
    
    return foundDescendant;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#isDescendantOf(edu.internet2.middleware.signet.tree.TreeNode)
   */
  public boolean isDescendantOf(TreeNode possibleAncestor)
  {
    return possibleAncestor.isAncestorOf(this);
  }
	
  public boolean equals(Object o)
  {
    if ( !(o instanceof TreeNodeImpl) )
    {
      return false;
    }
    
    TreeNodeImpl rhs = (TreeNodeImpl) o;
    return new EqualsBuilder()
                    .append(this.getTree(), rhs.getTree())
                    .append(this.getId(), rhs.getId())
                    .isEquals();
  }

  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */   
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new
    	HashCodeBuilder(17, 37)
    	  .append(this.getTree())
    		.append(this.getId())
        .toHashCode();
   }
	
	public String toString()
	{
	  return
	  	this.getTree().getTreeType().getId()
	  	+ Signet.SCOPE_PART_DELIMITER
	  	+ this.getTree().getId()
	  	+ Signet.SCOPE_PART_DELIMITER
	  	+ this.getId();
	}

  /**
   * @param allScopes
   * @return
   */
  public boolean isAncestorOfAll(Set treeNodes)
  {
    Iterator treeNodesIterator = treeNodes.iterator();
    while (treeNodesIterator.hasNext())
    {
      TreeNode otherTreeNode = (TreeNode)(treeNodesIterator.next());
      if ((otherTreeNode.getTree().equals(this.getTree()))
          && (otherTreeNode.isAncestorOf(this)))
      {
        return false;
      }
    }
    
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    String thisName = null;
    String otherName = null;

    thisName = this.getName();
    otherName = ((TreeNode)o).getName();
    
    return thisName.compareToIgnoreCase(otherName);
  }
}
