/*--
$Id: TreeNodeFullyQualifiedId.java,v 1.4 2006-02-09 10:26:18 lmcrae Exp $
$Date: 2006-02-09 10:26:18 $

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

import java.io.Serializable;

import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class TreeNodeFullyQualifiedId
implements Serializable
{
  private String	treeId;
  private String	treeNodeId;

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public TreeNodeFullyQualifiedId()
  {
      super();
  }
  
  public TreeNodeFullyQualifiedId
  	(String	treeId,
  	 String	treeNodeId)
  {
    super();
    this.treeId = treeId;
    this.treeNodeId = treeNodeId;
  }
  
  public TreeNodeFullyQualifiedId(TreeNode	node)
  throws TreeNotFoundException
  {
    super();
    this.treeId = node.getTree().getId();
    this.treeNodeId = node.getId();
  }
  
  public String getTreeId()
  {
    return this.treeId;
  }
  
  public String getTreeNodeId()
  {
    return this.treeNodeId;
  }
  
  private void setTreeId(String id)
  {
    this.treeId = id;
  }
  
  private void setTreeNodeId(String id)
  {
    this.treeNodeId = id;
  }
  
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    
    if (o == null)
    {
      return false;
    }
    
    if (!(o instanceof TreeNodeFullyQualifiedId))
    {
      return false;
    }
    
    final TreeNodeFullyQualifiedId tnfqId = (TreeNodeFullyQualifiedId)o;
    
    if (!treeId.equals(tnfqId.getTreeId()))
    {
      return false;
    }
    
    if (!treeNodeId.equals(tnfqId.getTreeNodeId()))
    {
      return false;
    }
    
    return true;
  }
  
  public int hashCode()
  {
    int hashCode = 0;
    
    if (treeNodeId != null)
    {
    	hashCode = treeNodeId.hashCode();
    }

    return hashCode;
  }
  
  public String toString()
  {
    return
    	"[treeId='"
    	+ treeId
    	+ "',treeNodeId='"
    	+ treeNodeId
    	+ "']";
  }
}
