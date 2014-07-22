/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.pit.PITField;
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
   * insert a batch of pit group set objects
   * @param pitGroupSets
   */
  public void saveBatch(Set<PITGroupSet> pitGroupSets);
  
  /**
   * delete
   * @param pitGroupSet
   */
  public void delete(PITGroupSet pitGroupSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGroupSet
   */
  public PITGroupSet findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGroupSet
   */
  public PITGroupSet findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGroupSet
   */
  public PITGroupSet findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param ownerId
   * @param startTime 
   * @param contextId 
   * @param checkIfAlreadyExists 
   */
  public void insertSelfPITGroupSetsByOwner(String ownerId, Long startTime, String contextId, boolean checkIfAlreadyExists);

  /**
   * @param fieldId
   * @param startTime 
   * @param contextId 
   */
  public void insertSelfPITGroupSetsByField(String fieldId, Long startTime, String contextId);
  
  /**
   * @param ownerId
   * @param endTime
   * @param contextId
   */
  public void updateEndTimeByPITOwner(String ownerId, Long endTime, String contextId);
  
  /**
   * @param fieldId
   * @param endTime
   * @param contextId
   */
  public void updateEndTimeByPITField(String fieldId, Long endTime, String contextId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @param endTime
   * @param contextId
   */
  public void updateEndTimeByPITOwnerAndPITField(String ownerId, String fieldId, Long endTime, String contextId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @param activeOnly 
   * @return pit group set
   */
  public PITGroupSet findSelfPITGroupSet(String ownerId, String fieldId, boolean activeOnly);
  
  /**
   * @param ownerId
   * @param memberId
   * @param fieldId
   * @return pit group set
   */
  public PITGroupSet findActiveImmediateByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId);
  
  /**
   * @param groupId
   * @param field
   * @return pit group sets
   */
  public Set<PITGroupSet> findAllActiveByPITGroupOwnerAndPITField(String groupId, PITField field);
  
  /**
   * @param groupId
   * @return pit group sets
   */
  public Set<PITGroupSet> findAllActiveByMemberPITGroup(String groupId);
  
  /**
   * @param groupId
   * @return pit group sets
   */
  public Set<PITGroupSet> findAllByMemberPITGroup(String groupId);
  
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
  public PITGroupSet findActiveImmediateChildByParentAndMemberPITGroup(PITGroupSet parentPITGroupSet, String memberGroupId);
  
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
  public Set<PITGroupSet> findAllSelfPITGroupSetsByPITOwnerId(String id);
  
  /**
   * @param id
   */
  public void deleteSelfByPITOwnerId(String id);
  
  /**
   * @param options
   * @return active group sets that are missing in point in time
   */
  public Set<GroupSet> findMissingActivePITGroupSets(QueryOptions options);
  
  /**
   * @return active group sets that are missing in point in time (this time looking for effective issues)
   */
  public Set<GroupSet> findMissingActivePITGroupSetsSecondPass();
  
  /**
   * @return active point in time group sets that should be inactive
   */
  public Set<PITGroupSet> findMissingInactivePITGroupSets();
  
  /**
   * @return source ids of records that have duplicate active entries in PIT
   */
  public Set<String> findActiveDuplicates();
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITGroupSet
   */
  public Set<PITGroupSet> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete (won't run pre and post delete methods)
   * @param id
   */
  public void delete(String id);
}
