/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

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
   * insert in batch
   * @param pitFields
   */
  public void saveBatch(Set<PITField> pitFields);
  
  /**
   * delete
   * @param pitField
   */
  public void delete(PITField pitField);
  
  /**
   * @param pitFieldId
   * @return pit field
   */
  public PITField findById(String pitFieldId);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
}
