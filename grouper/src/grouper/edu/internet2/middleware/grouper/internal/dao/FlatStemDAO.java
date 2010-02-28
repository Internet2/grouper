/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.Stem;
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
   * insert a batch of flat stems
   * @param flatStems
   */
  public void saveBatch(Set<FlatStem> flatStems);
  
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
  
  /**
   * @param flatStemId
   */
  public void removeStemForeignKey(String flatStemId);
  
  /**
   * find missing flat stems
   * @return set of stems that need flat stems
   */
  public Set<Stem> findMissingFlatStems();

  /**
   * remove bad flat stems
   * @return set of flat stems that should be removed
   */
  public Set<FlatStem> findBadFlatStems();
}
