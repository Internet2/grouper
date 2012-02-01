/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Stem;
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
   * insert or update
   * @param pitStems
   */
  public void saveOrUpdate(Set<PITStem> pitStems);
  
  /**
   * delete
   * @param pitStem
   */
  public void delete(PITStem pitStem);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITStem
   */
  public PITStem findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITStem
   */
  public PITStem findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITStem
   */
  public Set<PITStem> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITStem
   */
  public PITStem findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
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
  
  /**
   * @return active stems that are missing in point in time
   */
  public Set<Stem> findMissingActivePITStems();
  
  /**
   * @return active point in time stems that should be inactive
   */
  public Set<PITStem> findMissingInactivePITStems();
}
