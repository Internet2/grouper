/*--
$Id: TreeImpl.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeType;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeTypeAdapter;

//COLUMNS IN THE TreeNode TABLE:
//treeID
//nodeID
//nodeType
//status
//name
//createDatetime
//createDbAccount
//createUserID
//createContext
//modifyDatetime
//modifyDbAccount
//modifyUserID
//modifyContext
//comment

//COLUMNS IN THE TreeNodeRelationship TABLE:
//treeID
//nodeID
//parentNodeID

//COLUMNS IN THE Tree TABLE:
//treeID
//name
//adapterClass
//createDatetime
//createDbAccount
//createUserID
//createContext
//modifyDatetime
//modifyDbAccount
//modifyUserID
//modifyContext
//comment

class TreeImpl
extends EntityImpl
implements Tree
{
private	Signet					signet;
private Set							subsystems;
private Set							nodes;
private TreeTypeAdapter	adapter;
private String					adapterClassName;

public TreeImpl()
{
    super();
    this.nodes = new HashSet();
}

TreeImpl
	(Signet						signet,
	 TreeTypeAdapter	adapter,
	 String 					id,
	 String						name)
{
    super();
    this.signet = signet;
    this.setAdapter(adapter);
    this.setId(id);
    this.setName(name);
    this.nodes = new HashSet();
    this.subsystems = new HashSet();
}

void setSignet(Signet signet)
{
  this.signet = signet;
  
  if (this.adapter instanceof TreeTypeAdapterImpl)
  {
    ((TreeTypeAdapterImpl)(this.adapter)).setSignet(signet);
  }
}

/**
 * @return Returns the nodes.
 */
Set getNodes()
{
  return this.nodes;
}

/**
 * @param nodes The nodes to set.
 */
void setNodes(Set nodes)
{
  this.nodes = nodes;
}

/**
 * @return Returns the subsystems associated with this Tree.
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
public Set getSubsystems()
{
  return this.subsystems;
  // return UnmodifiableSet.decorate(this.subsystems);
}

/**
 * @param subsystem The subsystem to set.
 */
void setSubsystems(Set subsystems)
{
  this.subsystems = subsystems;
}

/* (non-Javadoc)
 * @see edu.internet2.middleware.signet.tree#getRoot()
 */
public Set getRoots()
{
  Set roots = new HashSet();
  
  Iterator nodesIterator = nodes.iterator();
  while (nodesIterator.hasNext())
  {
    TreeNodeImpl rootCandidate = (TreeNodeImpl)(nodesIterator.next());
    rootCandidate.setSignet(signet);
    if (rootCandidate.getParents().size() == 0)
    {
      roots.add(rootCandidate);
    }
  }
    
  return roots;
}

/**
 * @param nodeId
 * @param nodeName
 * @param nodeType
 * @return
 */
public void addRoot(TreeNode rootNode)
{
  if (this.getAdapter().isModifiable())
  {
    this.nodes.add(rootNode);
  }
  else
  {
    throw new IllegalArgumentException
    	("Only modifiable trees may have nodes added to them."
    	 + " The tree '" + this.getId() + "' is not modifiable.");
  }
}

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
public Set getTreeNodes()
{
  TreeNode[] treeNodesArray;
    
  if (this.nodes == null)
  {
    this.nodes = new HashSet();
  }
    
  return this.nodes;
  // return UnmodifiableSet.decorate(nodes);
}
  
/**
 * @return A brief description of this TreeImpl. The exact details
 * 		of the representation are unspecified and subject to change.
 */
public String toString()
{
  return 
  	new 
  		ToStringBuilder(this)
  			.append("id", getId())
  			.append("createDatetime", getCreateDatetime())
  			.append("modifyDatetime", getModifyDatetime())
  			.toString();
}

public TreeNode getNode(String nodeId)
{
  Iterator nodesIterator = nodes.iterator();
  while (nodesIterator.hasNext())
  {
    TreeNode candidate = (TreeNode)(nodesIterator.next());
    String candidateId = candidate.getId();
    if (candidateId.equals(nodeId))
    {
      return candidate;
    }
  }
    
  return null;
}

public boolean equals(Object obj)
{
  if ( !(obj instanceof TreeImpl) )
  {
    return false;
  }
  
  TreeImpl rhs = (TreeImpl) obj;
  return new EqualsBuilder()
                  .append(this.getId(), rhs.getId())
                  .isEquals();
}

public int hashCode()
{
  // you pick a hard-coded, randomly chosen, non-zero, odd number
  // ideally different for each class
  return new HashCodeBuilder(17, 37).   
     append(this.getId())
     .toHashCode();
}



public TreeTypeAdapter getAdapter()
{
  if ((this.adapter == null)
      && (this.adapterClassName != null))
  {
    this.adapter
    	= this.signet.getTreeTypeAdapter(this.adapterClassName);
  }
  return this.adapter;
}

void setAdapter(TreeTypeAdapter adapter)
{
  this.adapter = adapter;
  this.adapterClassName = adapter.getClass().getName();
  
  if (this.adapter instanceof TreeTypeAdapterImpl)
  {
    ((TreeTypeAdapterImpl)(this.adapter)).setSignet(signet);
  }
}

void setAdapterClassName(String name)
{
  this.adapterClassName = name;
  
  if (signet != null)
  {
    this.adapter = this.signet.getTreeTypeAdapter(name);
  }
}

String getAdapterClassName()
{
  return this.adapterClassName;
}
}
