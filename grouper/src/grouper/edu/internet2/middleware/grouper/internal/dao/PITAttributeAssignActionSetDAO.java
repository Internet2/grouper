/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

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
}
