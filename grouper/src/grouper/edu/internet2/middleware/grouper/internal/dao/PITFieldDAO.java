/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.pit.PITField;

/**
 * 
 */
public interface PITFieldDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitField
   */
  public void saveOrUpdate(PITField pitField);
  
  /**
   * insert or update
   * @param pitFields
   */
  public void saveOrUpdate(Set<PITField> pitFields);
  
  /**
   * delete
   * @param pitField
   */
  public void delete(PITField pitField);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITField
   */
  public PITField findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITField
   */
  public PITField findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITField
   */
  public PITField findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITField
   */
  public Set<PITField> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @return active fields that are missing in point in time
   */
  public Set<Field> findMissingActivePITFields();
  
  /**
   * @return active point in time fields that should be inactive
   */
  public Set<PITField> findMissingInactivePITFields();
}
