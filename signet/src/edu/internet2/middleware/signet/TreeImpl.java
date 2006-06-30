/*--
$Id: TreeImpl.java,v 1.9 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $
 
Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package edu.internet2.middleware.signet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeAdapter;
import edu.internet2.middleware.signet.tree.TreeNode;

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

public class TreeImpl extends EntityImpl implements Tree
{
  private Set             subsystems;

  private Set             nodes;

  private TreeAdapter adapter;

  private String          adapterClassName;

  public TreeImpl()
  {
    super();
    this.nodes = new HashSet();
  }

  TreeImpl(Signet signet, TreeAdapter adapter, String id, String name)
  {
    super(signet, id, name, Status.ACTIVE);
    this.setAdapter(adapter);
    this.nodes = new HashSet();
    this.subsystems = new HashSet();
  }

  public void setSignet(Signet signet)
  {
    super.setSignet(signet);

    if (this.adapter instanceof TreeAdapterImpl)
    {
      ((TreeAdapterImpl) (this.adapter)).setSignet(signet);
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
      TreeNodeImpl rootCandidate = (TreeNodeImpl) (nodesIterator.next());
      rootCandidate.setSignet(this.getSignet());
      if (rootCandidate.getParents().size() == 0)
      {
        rootCandidate.setSignet(this.getSignet());
        roots.add(rootCandidate);
      }
    }

    return roots;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.Tree#addRoot(edu.internet2.middleware.signet.tree.TreeNode)
   */
  public void addRoot(TreeNode rootNode)
  {
    if (this.getAdapter().isModifiable())
    {
      this.nodes.add(rootNode);
    }
    else
    {
      throw new IllegalArgumentException(
          "Only modifiable trees may have nodes added to them." + " The tree '"
              + this.getId() + "' is not modifiable.");
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
    if (null == nodes)
      nodes = new HashSet();
    
    for (Iterator nodesIterator = nodes.iterator(); nodesIterator.hasNext();)
      ((TreeNodeImpl)(nodesIterator.next())).setSignet(this.getSignet());

    return (nodes);
    // return UnmodifiableSet.decorate(nodes);
  }

  /**
   * @return A brief description of this TreeImpl. The exact details
   * 		of the representation are unspecified and subject to change.
   */
  public String toString()
  {
    return new ToStringBuilder(this)
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
      TreeNode candidate = (TreeNode) (nodesIterator.next());
      String candidateId = candidate.getId();
      if (candidateId.equals(nodeId))
      {
        ((TreeNodeImpl)candidate).setSignet(this.getSignet());
        return candidate;
      }
    }

    return null;
  }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof TreeImpl))
    {
      return false;
    }

    TreeImpl rhs = (TreeImpl) obj;
    return new EqualsBuilder().append(this.getId(), rhs.getId()).isEquals();
  }

  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).append(this.getId()).toHashCode();
  }

  public TreeAdapter getAdapter()
  {
    if ((this.adapter == null) && (this.adapterClassName != null))
    {
      this.adapter = this.getSignet().getTreeAdapter(this.adapterClassName);
    }
    return this.adapter;
  }

  public void setAdapter(TreeAdapter adapter)
  {
    this.adapter = adapter;
    this.adapterClassName = adapter.getClass().getName();

    if (this.adapter instanceof TreeAdapterImpl)
    {
      ((TreeAdapterImpl) (this.adapter)).setSignet(this.getSignet());
    }
  }

  void setAdapterClassName(String name)
  {
    this.adapterClassName = name;

    if (this.getSignet() != null)
    {
      this.adapter = this.getSignet().getTreeAdapter(name);
    }
  }

  String getAdapterClassName()
  {
    return this.adapterClassName;
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

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#inactivate()
   */
  public void inactivate()
  {
    throw new UnsupportedOperationException
      ("This method is not yet implemented");
  }
}
