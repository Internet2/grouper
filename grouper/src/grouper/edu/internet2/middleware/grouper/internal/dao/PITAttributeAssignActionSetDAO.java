/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;

/**
 * 
 */
public interface PITAttributeAssignActionSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssignActionSet
   */
  public void saveOrUpdate(PITAttributeAssignActionSet pitAttributeAssignActionSet);
  
  /**
   * delete
   * @param pitAttributeAssignActionSet
   */
  public void delete(PITAttributeAssignActionSet pitAttributeAssignActionSet);
  
  /**
   * @param id
   * @return PITAttributeAssignActionSet
   */
  public PITAttributeAssignActionSet findById(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
}
