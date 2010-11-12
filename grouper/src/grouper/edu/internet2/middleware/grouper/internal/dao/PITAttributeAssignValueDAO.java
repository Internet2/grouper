/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;

/**
 * 
 */
public interface PITAttributeAssignValueDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssignValue
   */
  public void saveOrUpdate(PITAttributeAssignValue pitAttributeAssignValue);
  
  /**
   * delete
   * @param pitAttributeAssignValue
   */
  public void delete(PITAttributeAssignValue pitAttributeAssignValue);
  
  /**
   * @param id
   * @return PITAttributeAssignValue
   */
  public PITAttributeAssignValue findById(String id);
}