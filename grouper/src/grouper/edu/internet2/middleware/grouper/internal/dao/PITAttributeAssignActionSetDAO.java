/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;

/**
 * 
 */
public interface PITAttributeAssignActionSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssignActionSet
   */
  public void saveOrUpdate(PITAttributeAssignActionSet pitAttributeAssignActionSet);
  
  /**
   * insert or update
   * @param pitAttributeAssignActionSets
   */
  public void saveOrUpdate(Set<PITAttributeAssignActionSet> pitAttributeAssignActionSets);
  
  /**
   * delete
   * @param pitAttributeAssignActionSet
   */
  public void delete(PITAttributeAssignActionSet pitAttributeAssignActionSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignActionSet
   */
  public PITAttributeAssignActionSet findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignActionSet
   */
  public PITAttributeAssignActionSet findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignActionSet
   */
  public PITAttributeAssignActionSet findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param pitAttributeAssignActionSet
   * @return pit action sets
   */
  public Set<PITAttributeAssignActionSet> findImmediateChildren(PITAttributeAssignActionSet pitAttributeAssignActionSet);
  
  /**
   * @param id
   * @return pit action sets
   */
  public Set<PITAttributeAssignActionSet> findAllSelfAttributeAssignActionSetsByAttributeAssignActionId(String id);
  
  /**
   * @param id
   */
  public void deleteSelfByAttributeAssignActionId(String id);
  
  /**
   * @param id
   * @return pit action sets
   */
  public Set<PITAttributeAssignActionSet> findByThenHasAttributeAssignActionId(String id);
  
  /**
   * @return active action sets that are missing in point in time
   */
  public Set<AttributeAssignActionSet> findMissingActivePITAttributeAssignActionSets();
  
  /**
   * @return active point in time action sets that should be inactive
   */
  public Set<PITAttributeAssignActionSet> findMissingInactivePITAttributeAssignActionSets();
}
