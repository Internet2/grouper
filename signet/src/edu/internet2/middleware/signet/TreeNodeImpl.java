/*--
 $Id: TreeNodeImpl.java,v 1.6 2005-06-17 23:24:28 acohen Exp $
 $Date: 2005-06-17 23:24:28 $
 
 Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
 Licensed under the Signet License, Version 1,
 see doc/license.txt in this distribution.
 */
package edu.internet2.middleware.signet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;

class TreeNodeImpl
extends
	EntityImpl
implements
	TreeNode,
	Comparable
{
  private TreeImpl tree;
  private String   treeId;
  private Set      parents;
  private Set      children;
  private boolean  parentsAlreadyFetched  = false;
  private boolean  childrenAlreadyFetched = false;

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

  public TreeNodeImpl(Signet signet, Tree tree, String id, String name)
  {
    super(signet, id, name, Status.ACTIVE);
    this.tree = (TreeImpl) tree;
    this.treeId = this.tree.getId();
    this.parents = new HashSet();
    this.children = new HashSet();
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
    if (parentsAlreadyFetched == false)
    {
      try
      {
        this.parents = this.getSignet().getParents(this);
      }
      catch (TreeNotFoundException tnfe)
      {
        throw new SignetRuntimeException(tnfe);
      }
      parentsAlreadyFetched = true;
    }

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
    if (childrenAlreadyFetched == false)
    {
      try
      {
        this.children = this.getSignet().getChildren(this);
      }
      catch (TreeNotFoundException tnfe)
      {
        throw new SignetRuntimeException(tnfe);
      }
      childrenAlreadyFetched = true;
    }

    return this.children;
    // return UnmodifiableSet.decorate(this.children);
  }

  /**
   * @return Returns the tree.
   */
  public Tree getTree()
  {
    if ((this.tree == null) && (this.treeId != null)
        && (this.getSignet() != null))
    {
      try
      {
        this.tree
        	= (TreeImpl)(this.getSignet().getTree(this.treeId));
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
    }

    return this.tree;
  }

  String getTreeId()
  {
    return this.treeId;
  }

  /**
   * @param tree The tree to set.
   */
  void setTree(Tree tree)
  {
    this.tree = (TreeImpl) tree;
    this.treeId = tree.getId();
  }

  void setTreeId(String treeId) throws ObjectNotFoundException
  {
    this.treeId = treeId;

    if (this.getSignet() != null)
    {
      this.tree = (TreeImpl) (this.getSignet().getTree(treeId));
    }
  }

  /**
   * @param children The children to set.
   */
  void setChildren(Set children)
  {
    this.children = children;

    Iterator childrenIterator = children.iterator();
    while (childrenIterator.hasNext())
    {
      TreeNode child = (TreeNode) (childrenIterator.next());
      // Now, save the parent-child relationship info in the db.
      this.addChild(child);
    }
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
    ((TreeNodeImpl) childNode).parents.add(this);

    try
    {
      saveRelationship(childNode, this);
    }
    catch (TreeNotFoundException tnfe)
    {
      // I decided to throw a runtime exception here instead of a
      // an ObjectNotFoundException, because the caller is not
      // knowingly trying to fetch anything, and has no reasonable
      // way to handle this error.
      throw new SignetRuntimeException(tnfe);
    }
  }

  private void saveRelationship(TreeNode childNode, TreeNode parentNode)
      throws TreeNotFoundException
  {
    TreeNodeRelationship tnr = new TreeNodeRelationship(childNode.getTree()
        .getId(), childNode.getId(), parentNode.getId());

    this.getSignet().save(tnr);
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
      throw new IllegalArgumentException(
          "It is illegal to inquire whether a NULL TreeNode"
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
        foundDescendant = ((TreeNodeImpl) (childrenIterator.next()))
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
    if (!(o instanceof TreeNodeImpl))
    {
      return false;
    }

    TreeNodeImpl rhs = (TreeNodeImpl) o;
    return new EqualsBuilder().append(this.getTreeId(), rhs.getTreeId())
        .append(this.getId(), rhs.getId()).isEquals();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).append(this.getTreeId()).append(
        this.getId()).toHashCode();
  }

  public String toString()
  {
    return this.getTree().getAdapter().getClass().getName()
           + Signet.SCOPE_PART_DELIMITER + this.getTreeId()
           + Signet.SCOPE_PART_DELIMITER + this.getId();
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
      TreeNodeImpl otherTreeNode = (TreeNodeImpl) (treeNodesIterator.next());
      if ((otherTreeNode.getTreeId().equals(this.getTreeId()))
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
    otherName = ((TreeNode) o).getName();

    return thisName.compareToIgnoreCase(otherName);
  }

  /* This method exists only for use by Hibernate.
   */
  public TreeNodeFullyQualifiedId getFullyQualifiedId()
  {
    return new TreeNodeFullyQualifiedId(this.getTreeId(), this.getId());
  }

  /*
   * This method exists only for use by Hibernate.
   */
  void setFullyQualifiedId(TreeNodeFullyQualifiedId tnfqId)
      throws ObjectNotFoundException
  {
    this.treeId = tnfqId.getTreeId();
    this.setId(tnfqId.getTreeNodeId());

    if (this.getSignet() != null)
    {
      this.tree = (TreeImpl) (this.getSignet().getTree(tnfqId.getTreeId()));
    }
  }
  
  public String getId()
  {
    return super.getStringId();
  }
  
  // This method is only for use by Hibernate.
  private void setId(String id)
  {
    super.setStringId(id);
  }
}