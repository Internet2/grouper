/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.pit.PITGroupSet;

/**
 * 
 */
public interface PITGroupSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitGroupSet
   */
  public void saveOrUpdate(PITGroupSet pitGroupSet);
  
  /**
   * insert or update
   * @param pitGroupSets
   */
  public void saveOrUpdate(Set<PITGroupSet> pitGroupSets);
  
  /**
   * delete
   * @param pitGroupSet
   */
  public void delete(PITGroupSet pitGroupSet);
  
  /**
   * @param pitGroupSetId
   * @return pit group set
   */
  public PITGroupSet findById(String pitGroupSetId);
  
  /**
   * @param ownerId
   * @param startTime 
   * @param contextId 
   * @param checkIfAlreadyExists 
   */
  public void insertSelfGroupSetsByOwner(String ownerId, Long startTime, String contextId, boolean checkIfAlreadyExists);

  /**
   * @param fieldId
   * @param startTime 
   * @param contextId 
   */
  public void insertSelfGroupSetsByField(String fieldId, Long startTime, String contextId);
  
  /**
   * @param ownerId
   * @param endTime
   * @param contextId
   */
  public void updateEndTimeByOwner(String ownerId, Long endTime, String contextId);
  
  /**
   * @param fieldId
   * @param endTime
   * @param contextId
   */
  public void updateEndTimeByField(String fieldId, Long endTime, String contextId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @param endTime
   * @param contextId
   */
  public void updateEndTimeByOwnerAndField(String ownerId, String fieldId, Long endTime, String contextId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @param activeOnly 
   * @return pit group set
   */
  public PITGroupSet findSelfGroupSet(String ownerId, String fieldId, boolean activeOnly);
  
  /**
   * @param ownerId
   * @param memberId
   * @param fieldId
   * @return pit group set
   */
  public PITGroupSet findActiveImmediateByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId);
  
  /**
   * @param groupId
   * @param field
   * @return pit group sets
   */
  public Set<PITGroupSet> findAllActiveByGroupOwnerAndField(String groupId, Field field);
  
  /**
   * @param groupId
   * @return pit group sets
   */
  public Set<PITGroupSet> findAllActiveByMemberGroup(String groupId);
  
  /**
   * @param groupId
   * @return pit group sets
   */
  public Set<PITGroupSet> findAllByMemberGroup(String groupId);
  
  /**
   * @param pitGroupSet
   * @return all nested children of the pit group set
   */
  public Set<PITGroupSet> findAllActiveChildren(PITGroupSet pitGroupSet);
  
  /**
   * @param parentPITGroupSet
   * @param memberGroupId
   * @return pit group set
   */
  public PITGroupSet findActiveImmediateChildByParentAndMemberGroup(PITGroupSet parentPITGroupSet, String memberGroupId);
  
  /**
   * @param pitGroupSet
   * @return pit group sets
   */
  public Set<PITGroupSet> findImmediateChildren(PITGroupSet pitGroupSet);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return pit group sets
   */
  public Set<PITGroupSet> findAllSelfGroupSetsByOwnerId(String id);
  
  /**
   * @param id
   */
  public void deleteSelfByOwnerId(String id);
  
  /**
   * @return active group sets that are missing in point in time
   */
  public Set<GroupSet> findMissingActivePITGroupSets();
  
  /**
   * @return active point in time group sets that should be inactive
   */
  public Set<PITGroupSet> findMissingInactivePITGroupSets();
}
