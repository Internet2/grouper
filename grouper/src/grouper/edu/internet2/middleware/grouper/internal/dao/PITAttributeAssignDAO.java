/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.pit.PITAttributeAssign;

/**
 * 
 */
public interface PITAttributeAssignDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssign
   */
  public void saveOrUpdate(PITAttributeAssign pitAttributeAssign);
  
  /**
   * delete
   * @param pitAttributeAssign
   */
  public void delete(PITAttributeAssign pitAttributeAssign);
  
  /**
   * @param id
   * @return PITAttributeAssign
   */
  public PITAttributeAssign findById(String id);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateId(String oldId, String newId);
}
