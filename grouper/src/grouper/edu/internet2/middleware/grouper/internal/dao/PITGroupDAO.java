/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITGroup;

/**
 * 
 */
public interface PITGroupDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitGroup
   */
  public void saveOrUpdate(PITGroup pitGroup);
  
  /**
   * insert in batch
   * @param pitGroups
   */
  public void saveBatch(Set<PITGroup> pitGroups);
  
  /**
   * delete
   * @param pitGroup
   */
  public void delete(PITGroup pitGroup);
  
  /**
   * @param pitGroupId
   * @return pit group
   */
  public PITGroup findById(String pitGroupId);
}
