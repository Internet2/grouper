/*--
$Id: LimitValueHistory.java,v 1.3 2005-11-24 00:02:53 acohen Exp $
$Date: 2005-11-24 00:02:53 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

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
  private Limit     limit;
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
    (AssignmentImpl  assignmentImpl,
     LimitValue      limitValue)
  {
    this.setAssignmentId(assignmentImpl.getId());
    this.setInstanceNumber(assignmentImpl.getInstanceNumber());
    this.limit = limitValue.getLimit();
    this.value = limitValue.getValue();
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
  
  /**
   * Gets the <code>Limit</code> associated with this historical record.
   * 
   * @return the <code>Limit</code> associated with this historical record.
   */
  Limit getLimit()
  {
    return this.limit;
  }
  
  // This method is for use only by Hibernate.
  private void setLimit(Limit limit)
  {
    this.limit = limit;
  }
}
