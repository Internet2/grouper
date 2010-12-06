/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;

/**
 * 
 */
public interface PITAttributeDefNameSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeDefNameSet
   */
  public void saveOrUpdate(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * delete
   * @param pitAttributeDefNameSet
   */
  public void delete(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * @param id
   * @return PITAttributeDefNameSet
   */
  public PITAttributeDefNameSet findById(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
}
