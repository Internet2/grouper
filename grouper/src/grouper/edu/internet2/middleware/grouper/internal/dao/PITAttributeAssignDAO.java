/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITAttributeAssign;

/**
 * 
 */
public interface PITAttributeAssignDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssign
   */
  public void saveOrUpdate(PITAttributeAssign pitAttributeAssign);
  
  /**
   * delete
   * @param pitAttributeAssign
   */
  public void delete(PITAttributeAssign pitAttributeAssign);
  
  /**
   * @param id
   * @return PITAttributeAssign
   */
  public PITAttributeAssign findById(String id);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateOwnerAttributeAssignId(String oldId, String newId);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findActiveByOwnerAttributeAssignId(String id);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateOwnerMembershipId(String oldId, String newId);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findActiveByOwnerMembershipId(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
}
