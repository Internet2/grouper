/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.flat.FlatAttributeDef;


/**
 * 
 */
public interface FlatAttributeDefDAO extends GrouperDAO {

  /**
   * insert a flat attribute def object
   * @param flatAttributeDef
   */
  public void save(FlatAttributeDef flatAttributeDef);
  
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
}
