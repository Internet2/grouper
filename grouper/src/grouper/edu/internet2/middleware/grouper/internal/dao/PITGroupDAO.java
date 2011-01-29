/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
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
  
  /**
   * @param groupName
   * @param orderByStartTime
   * @return set of pit groups
   */
  public Set<PITGroup> findByName(String groupName, boolean orderByStartTime);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * Get all the groups that a member is a member of.
   * @param pitMemberId 
   * @param pitFieldId 
   * @param scope 
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param queryOptions 
   * @return set of pit groups
   */
  public Set<PITGroup> getAllGroupsMembershipSecure(String pitMemberId, String pitFieldId, String scope,
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions);
}
