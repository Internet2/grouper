/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITStem;

/**
 * 
 */
public interface PITStemDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitStem
   */
  public void saveOrUpdate(PITStem pitStem);
  
  /**
   * insert in batch
   * @param pitStems
   */
  public void saveBatch(Set<PITStem> pitStems);
  
  /**
   * delete
   * @param pitStem
   */
  public void delete(PITStem pitStem);
  
  /**
   * @param pitStemId
   * @return pit stem
   */
  public PITStem findById(String pitStemId);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITStem
   */
  public Set<PITStem> findByParentStemId(String id);
  
  /**
   * @param stemName
   * @param orderByStartTime
   * @return set of pit stems
   */
  public Set<PITStem> findByName(String stemName, boolean orderByStartTime);
}
