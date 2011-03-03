/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITAttributeDefName;

/**
 * 
 */
public interface PITAttributeDefNameDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeDefName
   */
  public void saveOrUpdate(PITAttributeDefName pitAttributeDefName);
  
  /**
   * delete
   * @param pitAttributeDefName
   */
  public void delete(PITAttributeDefName pitAttributeDefName);
  
  /**
   * @param id
   * @return PITAttributeDefName
   */
  public PITAttributeDefName findById(String id);
  
  /**
   * @param name
   * @param orderByStartTime
   * @return set of pit attribute def names
   */
  public Set<PITAttributeDefName> findByName(String name, boolean orderByStartTime);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
}
