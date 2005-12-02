/*--
$Id: AssignmentHistoryImpl.java,v 1.1 2005-12-02 18:36:53 acohen Exp $
$Date: 2005-12-02 18:36:53 $

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
class AssignmentHistoryImpl extends HistoryImpl implements AssignmentHistory
{
  private Assignment  assignment;
  private TreeNode    scope;
  private Function    function;
  private Set         limitValues;
  private boolean     canGrant;
  private boolean     canUse;
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public AssignmentHistoryImpl()
  {
    super();
  }
  
  AssignmentHistoryImpl(AssignmentImpl assignment)
  {
    // Most information is just copied from the Assignment object to the
    // AssignmentHistoryImpl object.
    super(assignment);
    
    this.setAssignment(assignment);
    this.setScope(assignment.getScope());
    this.setFunction(assignment.getFunction());
    this.setLimitValues(assignment.getLimitValues());
    this.setCanGrant(assignment.canGrant());
    this.setCanUse(assignment.canUse());
    
    // We need to make a fresh copy of these limit-values for history
    // purposes. Otherwise, Hibernate complains that it "Found shared
    // references to a collection".
    Set limitValuesCopy = new HashSet(assignment.getLimitValues().size());
    limitValuesCopy.addAll(assignment.getLimitValues());
    this.setLimitValues(limitValuesCopy);
  }
  
//  private Set buildLimitValueHistory(Assignment assignment)
//  {
//    Set limitValues = assignment.getLimitValues();
//    Set limitValueHistorySet = new HashSet(limitValues.size());
//    Iterator limitValuesIterator = limitValues.iterator();
//    while (limitValuesIterator.hasNext())
//    {
//      LimitValue limitValue = (LimitValue)(limitValuesIterator.next());
//      LimitValueHistory limitValueHistory = new LimitValueHistory(limitValue);
//      limitValueHistorySet.add(limitValueHistory);
//    }
//    
//    return limitValueHistorySet;
//  }
  
  public TreeNode getScope()
  {
    return this.scope;
  }
  
  void setScope(TreeNode scope)
  {
    this.scope = scope;
  }
  
  public Function getFunction()
  {
    return this.function;
  }
  
  void setFunction(Function function)
  {
    this.function = function;
  }
  
  public boolean canGrant()
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
  
  public boolean canUse()
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
  
  public Set getLimitValues()
  {
    return this.limitValues;
  }
  
  void setLimitValues(Set limitValues)
  {
    this.limitValues = limitValues;
  }
  
  public String toString()
  {
    return
      "[instanceNumber="
      + this.getInstanceNumber()
      + ", function="
      + this.getFunction()
      + ", scope="
      + this.getScope()
      + ", limitValues="
      + this.getLimitValues()
      + ", canUse="
      + this.canUse()
      + ", canGrant="
      + this.canGrant()
      + "]";
  }
  
  Assignment getAssignment()
  {
    return this.assignment;
  }
  
  void setAssignment(Assignment assignment)
  {
    this.assignment = assignment;
  }
}
