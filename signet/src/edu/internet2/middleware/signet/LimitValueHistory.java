/*--
$Id: LimitValueHistory.java,v 1.1 2005-06-28 19:41:57 acohen Exp $
$Date: 2005-06-28 19:41:57 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class LimitValueHistory
{
  // LimitValueHistory is unusual among Signet entities in that it
  // (along with AssignmentImpl and AssignmentImplHistory) has a numeric, not
  // alphanumeric ID.
  private Integer   historyId;
  private Integer   assignmentId;
  private int       instanceNumber;
  private Subsystem subsystem;
//  private Limit     limit;
  private String    choiceSetId;
  private String    value;
  
  private final String      limitType="reserved";
  
  /**
   * Hibernate requires the presence of a default constructor.
   */
  public LimitValueHistory()
  {
    super();
  }
  
  LimitValueHistory
    (AssignmentHistory  assignmentHistory,
     LimitValue         limitValue)
  {
    // Most information is just copied from the Assignment object to the
    // AssignmentHistory object.
//    this.setHistoryId(assignmentHistory.getHistoryId());
    this.setAssignmentId(assignmentHistory.getAssignmentId());
    this.setInstanceNumber(assignmentHistory.getInstanceNumber());
    this.setSubsystem(assignmentHistory.getFunction().getSubsystem());
//    this.limit = limitValue.getLimit();
    this.choiceSetId = limitValue.getLimit().getChoiceSet().getId();
    this.value = limitValue.getValue();
  }
  
  Subsystem getSubsystem()
  {
    return this.subsystem;
  }
  
  private void setSubsystem(Subsystem subsystem)
  {
    this.subsystem = subsystem;
  }
  
  Integer getHistoryId()
  {
    return this.historyId;
  }
  
  Integer getAssignmentId()
  {
    return this.assignmentId;
  }
  
  private void setAssignmentId(Integer id)
  {
    this.assignmentId = id;
  }
  
  private void setHistoryId(Integer historyId)
  {
    this.historyId = historyId;
  }
  
  int getInstanceNumber()
  {
    return this.instanceNumber;
  }
  
  private void setInstanceNumber(int instanceNumber)
  {
    this.instanceNumber = instanceNumber;
  }
  
  /**
   * @return Returns the limitType.
   */
  String getLimitType()
  {
    return this.limitType;
  }
  
  /**
   * @param limitType The limitType to set.
   */
  void setLimitType(String limitType)
  {
    // This method does nothing, and is just a place-holder.
  }
  
  String getChoiceSetId()
  {
    return this.choiceSetId;
  }
  
  // This method is for use only by Hibernate.
  private void setChoiceSetId(String choiceSetId)
  {
    this.choiceSetId = choiceSetId;
  }
  
  // This method is for use only by Hibernate.
  private String getValue()
  {
    return this.value;
  }
  
  // This method is for use only by Hibernate.
  private void setValue(String value)
  {
    this.value = value;
  }
}
