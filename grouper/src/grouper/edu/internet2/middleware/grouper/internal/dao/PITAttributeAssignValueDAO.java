/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

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
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateAttributeAssignId(String oldId, String newId);
  
  /**
   * @param id
   * @return set of PITAttributeAssignValue
   */
  public Set<PITAttributeAssignValue> findActiveByAttributeAssignId(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * Find values by attribute assign id
   * @param attributeAssignId
   * @param queryOptions
   * @return set of values
   */
  public Set<PITAttributeAssignValue> findByAttributeAssignId(String attributeAssignId, QueryOptions queryOptions);
}