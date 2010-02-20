/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.flat.FlatGroup;


/**
 * 
 */
public interface FlatGroupDAO extends GrouperDAO {

  /**
   * insert a flat group object
   * @param flatGroup
   */
  public void save(FlatGroup flatGroup);
  
  /**
   * delete a flat group object
   * @param flatGroup
   */
  public void delete(FlatGroup flatGroup);
  
  /**
   * @param flatGroupId
   * @return flat group
   */
  public FlatGroup findById(String flatGroupId);
  
  /**
   * @param flatGroupId
   */
  public void removeGroupForeignKey(String flatGroupId);
}
