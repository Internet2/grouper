/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
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
   * insert a batch of flat group objects
   * @param flatGroups
   */
  public void saveBatch(Set<FlatGroup> flatGroups);
  
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
  
  /**
   * find missing flat groups
   * @return set of groups that need flat groups
   */
  public Set<Group> findMissingFlatGroups();
  
  /**
   * remove bad flat groups
   * @return set of flat groups that should be removed
   */
  public Set<FlatGroup> findBadFlatGroups();
}
