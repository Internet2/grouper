/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.flat.FlatAttributeDef;


/**
 * 
 */
public interface FlatAttributeDefDAO extends GrouperDAO {

  /**
   * insert or update a flat attribute def object
   * @param flatAttributeDef
   */
  public void saveOrUpdate(FlatAttributeDef flatAttributeDef);

  /**
   * insert a batch of flat attr def objects
   * @param flatAttributeDefs
   */
  public void saveBatch(Set<FlatAttributeDef> flatAttributeDefs);
  
  /**
   * delete a flat attribute def object
   * @param flatAttributeDef
   */
  public void delete(FlatAttributeDef flatAttributeDef);
  
  /**
   * @param flatAttributeDefId
   * @return flat attribute def
   */
  public FlatAttributeDef findById(String flatAttributeDefId);
  
  /**
   * @param flatAttributeDefId
   */
  public void removeAttributeDefForeignKey(String flatAttributeDefId);
  
  /**
   * find missing flat attr defs
   * @return set of attr defs that need flat attr defs
   */
  public Set<AttributeDef> findMissingFlatAttributeDefs();
  
  /**
   * remove bad flat attr defs
   * @return set of flat attr defs that should be removed
   */
  public Set<FlatAttributeDef> findBadFlatAttributeDefs();
}
