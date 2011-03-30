/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;

/**
 * 
 */
public interface PITAttributeAssignActionDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssignAction
   */
  public void saveOrUpdate(PITAttributeAssignAction pitAttributeAssignAction);
  
  /**
   * delete
   * @param pitAttributeAssignAction
   */
  public void delete(PITAttributeAssignAction pitAttributeAssignAction);
  
  /**
   * @param id
   * @return PITAttributeAssignAction
   */
  public PITAttributeAssignAction findById(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITAttributeAssignAction
   */
  public Set<PITAttributeAssignAction> findByAttributeDefId(String id);
}
