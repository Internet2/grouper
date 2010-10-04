/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

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
}
