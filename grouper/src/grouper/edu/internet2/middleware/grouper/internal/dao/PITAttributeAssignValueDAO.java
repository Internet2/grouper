/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
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
   * insert or update
   * @param pitAttributeAssignValues
   */
  public void saveOrUpdate(Set<PITAttributeAssignValue> pitAttributeAssignValues);
  
  /**
   * delete
   * @param pitAttributeAssignValue
   */
  public void delete(PITAttributeAssignValue pitAttributeAssignValue);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignValue
   */
  public PITAttributeAssignValue findBySourceIdActive(String id, boolean exceptionIfNotFound);

  /**  
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignValue
   */
  public PITAttributeAssignValue findById(String id, boolean exceptionIfNotFound);

  /**  
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignValue
   */
  public PITAttributeAssignValue findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
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
  
  /**
   * @return active attribute assign values that are missing in point in time
   */
  public Set<AttributeAssignValue> findMissingActivePITAttributeAssignValues();
  
  /**
   * @return active point in time attribute assign values that should be inactive
   */
  public Set<PITAttributeAssignValue> findMissingInactivePITAttributeAssignValues();
}