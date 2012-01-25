/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;

/**
 * 
 */
public interface PITAttributeAssignActionDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssignAction
   */
  public void saveOrUpdate(PITAttributeAssignAction pitAttributeAssignAction);
  
  /**
   * insert or update
   * @param pitAttributeAssignActions
   */
  public void saveOrUpdate(Set<PITAttributeAssignAction> pitAttributeAssignActions);
  
  /**
   * delete
   * @param pitAttributeAssignAction
   */
  public void delete(PITAttributeAssignAction pitAttributeAssignAction);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignAction
   */
  public PITAttributeAssignAction findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignAction
   */
  public PITAttributeAssignAction findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeAssignAction
   */
  public Set<PITAttributeAssignAction> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignAction
   */
  public PITAttributeAssignAction findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITAttributeAssignAction
   */
  public Set<PITAttributeAssignAction> findByAttributeDefId(String id);
  
  /**
   * @return active actions that are missing in point in time
   */
  public Set<AttributeAssignAction> findMissingActivePITAttributeAssignActions();
  
  /**
   * @return active point in time actions that should be inactive
   */
  public Set<PITAttributeAssignAction> findMissingInactivePITAttributeAssignActions();
}
