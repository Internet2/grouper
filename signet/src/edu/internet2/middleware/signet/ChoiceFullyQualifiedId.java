/*
 * Created on Dec 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
public class ChoiceFullyQualifiedId
implements Serializable
{
  private String	choiceSetId;
  private String	choiceValue;

  /**
   * Hibernate requires that each persistable entity have a default
   * constructor.
   */
  public ChoiceFullyQualifiedId()
  {
      super();
  }
  
  public ChoiceFullyQualifiedId
  	(String	choiceSetId,
  	 String	choiceValue)
  {
    super();
    this.choiceSetId = choiceSetId;
    this.choiceValue = choiceValue;
  }
  
  public ChoiceFullyQualifiedId(TreeNode	node)
  throws TreeNotFoundException
  {
    super();
    this.choiceSetId = node.getTree().getId();
    this.choiceValue = node.getId();
  }
  
  public String getChoiceSetId()
  {
    return this.choiceSetId;
  }
  
  public String getChoiceValue()
  {
    return this.choiceValue;
  }
  
  private void setChoiceSetId(String id)
  {
    this.choiceSetId = id;
  }
  
  private void setChoiceValue(String id)
  {
    this.choiceValue = id;
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
    
    if (!(o instanceof ChoiceFullyQualifiedId))
    {
      return false;
    }
    
    final ChoiceFullyQualifiedId tnfqId = (ChoiceFullyQualifiedId)o;
    
    if (!choiceSetId.equals(tnfqId.getChoiceSetId()))
    {
      return false;
    }
    
    if (!choiceValue.equals(tnfqId.getChoiceValue()))
    {
      return false;
    }
    
    return true;
  }
  
  public int hashCode()
  {
    int hashCode = 0;
    
    if (choiceValue != null)
    {
    	hashCode = choiceValue.hashCode();
    }

    return hashCode;
  }
  
  public String toString()
  {
    return
    	"[choiceSetId='"
    	+ choiceSetId
    	+ "',choiceValue='"
    	+ choiceValue
    	+ "']";
  }
}
