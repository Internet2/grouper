/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

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
}
