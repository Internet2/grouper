/*--
  $Id: TreeKey.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeType;

class TreeKey
implements Serializable
{
  private Signet		signet;
  private String 		treeId;
  private TreeType	treeType;

  /**
   * 
   */
  TreeKey()
  {
    super();
    // TODO Auto-generated constructor stub
  }
  
  TreeKey(Signet signet, String treeId, TreeType treeType)
  {
    this.signet = signet;
    this.treeId = treeId;
    this.treeType = treeType;
  }
  
  TreeKey(Tree tree)
  {
    this.treeId = tree.getId();
    this.treeType = tree.getTreeType();
  }

  /**
   * @return Returns the id.
   */
  String getTreeId()
  {
    return this.treeId;
  }
  /**
   * @param id The id to set.
   */
  void setTreeId(String treeId)
  {
    this.treeId = treeId;
  }
  
  /**
   * @return Returns the treeType.
   */
  TreeType getTreeType()
  {
    return this.treeType;
  }
  
  /**
   * @param treeType The treeType to set.
   */
  void setTreeType(TreeType treeType)
  {
    this.treeType = treeType;
  }
  
  String getTreeTypeId()
  {
    return this.treeType.getId();
  }
  
  void setTreeTypeId(String treeTypeId)
  throws ObjectNotFoundException
  {
    this.treeType = this.signet.getTreeType(treeTypeId);
  }
  
  boolean isComplete()
  {
    if ((this.treeId != null) && (this.treeType != null))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if ( !(obj instanceof TreeKey) )
    {
      return false;
    }
    
    TreeKey rhs = (TreeKey) obj;
    return new EqualsBuilder()
                    .append(this.getTreeId(), rhs.getTreeId())
                    .append(this.getTreeType(), rhs.getTreeType())
                    .isEquals();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).   
       append(this.getTreeId())
       .append(this.getTreeType())
       .toHashCode();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    String outStr
    	= "id='"
    	  + this.treeId 
    	  + "', treeType='" 
    	  + this.treeType
    	  + "'";
    
    return outStr;
  }
}
