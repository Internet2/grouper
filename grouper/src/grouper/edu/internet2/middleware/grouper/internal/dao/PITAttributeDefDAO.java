/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITAttributeDef;

/**
 * 
 */
public interface PITAttributeDefDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeDef
   */
  public void saveOrUpdate(PITAttributeDef pitAttributeDef);
  
  /**
   * insert in batch
   * @param pitAttributeDefs
   */
  public void saveBatch(Set<PITAttributeDef> pitAttributeDefs);
  
  /**
   * delete
   * @param pitAttributeDef
   */
  public void delete(PITAttributeDef pitAttributeDef);
  
  /**
   * @param pitAttributeDefId
   * @return pit attribute def
   */
  public PITAttributeDef findById(String pitAttributeDefId);
}
