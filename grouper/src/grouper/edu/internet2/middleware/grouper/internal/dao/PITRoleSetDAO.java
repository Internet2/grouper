/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.pit.PITRoleSet;

/**
 * 
 */
public interface PITRoleSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitRoleSet
   */
  public void saveOrUpdate(PITRoleSet pitRoleSet);
  
  /**
   * delete
   * @param pitRoleSet
   */
  public void delete(PITRoleSet pitRoleSet);
  
  /**
   * @param id
   * @return PITRoleSet
   */
  public PITRoleSet findById(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
}
