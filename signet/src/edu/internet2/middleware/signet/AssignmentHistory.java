/*--
$Id: AssignmentHistory.java,v 1.5 2005-08-29 18:29:30 acohen Exp $
$Date: 2005-08-29 18:29:30 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class AssignmentHistory extends History
{
  // AssignmentHistory is unusual among Signet entities in that it
  // (along with AssignmentImpl) has a numeric, not alphanumeric ID.
  protected Integer assignmentId;
  
  private TreeNode          scope;
  private Function          function;
  private Set               limitValues;
  private boolean           canGrant;
  private boolean           canUse;
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public AssignmentHistory()
  {
    super();
  }
  
  AssignmentHistory(AssignmentImpl assignment)
  {
    // Most information is just copied from the Assignment object to the
    // AssignmentHistory object.
    super(assignment);
    
    this.setAssignmentId(assignment.getId());
    this.setScope(assignment.getScope());
    this.setFunction(assignment.getFunction());
    this.setLimitValues(assignment.getLimitValues());
    this.setCanGrant(assignment.canGrant());
    this.setCanUse(assignment.canUse());
  }
  
  Integer getAssignmentId()
  {
    return this.assignmentId;
  }
  
  // This method is only for use by Hibernate.
  protected void setAssignmentId(Integer id)
  {
    this.assignmentId = id;
  }
  
  TreeNode getScope()
  {
    return this.scope;
  }
  
  void setScope(TreeNode scope)
  {
    this.scope = scope;
  }
  
  Function getFunction()
  {
    return this.function;
  }
  
  void setFunction(Function function)
  {
    this.function = function;
  }
  
  boolean canGrant()
  {
    return this.canGrant;
  }
  
  // This method is only for use by Hibernate.
  protected boolean getCanGrant()
  {
    return this.canGrant;
  }
  
  void setCanGrant(boolean canGrant)
  {
    this.canGrant = canGrant;
  }
  
  boolean canUse()
  {
    return this.canUse;
  }
  
  // This method is only for use by Hibernate.
  protected boolean getCanUse()
  {
    return this.canUse;
  }
  
  void setCanUse(boolean canUse)
  {
    this.canUse = canUse;
  }
  
  Set getLimitValues()
  {
    return this.limitValues;
  }
  
  void setLimitValues(Set limitValues)
  {
    // Let's make our own local copy of this Set, to remember its state right
    // now, when the history is being recorded.
    this.limitValues = new HashSet(limitValues);
  }
  
  public String toString()
  {
    return
      "[assignmentId="
      + this.getAssignmentId()
      + ", instanceNumber="
      + this.getInstanceNumber()
      + "]";
  }
}
