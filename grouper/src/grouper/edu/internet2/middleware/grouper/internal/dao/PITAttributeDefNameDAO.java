/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

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
}
