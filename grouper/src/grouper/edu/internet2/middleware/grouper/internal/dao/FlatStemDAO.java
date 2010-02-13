/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.flat.FlatStem;


/**
 * 
 */
public interface FlatStemDAO extends GrouperDAO {

  /**
   * insert a flat stem object
   * @param flatStem
   */
  public void save(FlatStem flatStem);
  
  /**
   * delete a flat stem object
   * @param flatStem
   */
  public void delete(FlatStem flatStem);
  
  /**
   * @param flatStemId
   * @return flat stem
   */
  public FlatStem findById(String flatStemId);
  
}
