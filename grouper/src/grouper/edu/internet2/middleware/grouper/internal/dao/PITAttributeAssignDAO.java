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
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
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
   * insert or update
   * @param pitAttributeAssigns
   */
  public void saveOrUpdate(Set<PITAttributeAssign> pitAttributeAssigns);
  
  /**
   * delete
   * @param pitAttributeAssign
   */
  public void delete(PITAttributeAssign pitAttributeAssign);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssign
   */
  public PITAttributeAssign findBySourceIdActive(String id, boolean exceptionIfNotFound);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssign
   */
  public PITAttributeAssign findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssign
   */
  public PITAttributeAssign findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssign
   */
  public PITAttributeAssign findBySourceIdMostRecent(String id, boolean exceptionIfNotFound);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateOwnerPITAttributeAssignId(String oldId, String newId);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findActiveByOwnerPITAttributeAssignId(String id);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateOwnerPITMembershipId(String oldId, String newId);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findActiveByOwnerPITMembershipId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerPITMembershipId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerPITGroupId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerPITStemId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerPITAttributeDefId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerPITAttributeAssignId(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param attributeAssigns
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @return pit assignments
   */
  public Set<PITAttributeAssign> findAssignmentsOnAssignments(Collection<PITAttributeAssign> attributeAssigns, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByPITAttributeDefNameId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByPITAttributeAssignActionId(String id);
  
  /**
   * @return active attribute assigns that are missing in point in time
   */
  public Set<AttributeAssign> findMissingActivePITAttributeAssigns();
  
  /**
   * @return active point in time attribute assigns that should be inactive
   */
  public Set<PITAttributeAssign> findMissingInactivePITAttributeAssigns();
  
  /**
   * @param pitGroupId
   * @param pitAttributeDefNameId
   * @return set of PITAttributeAssign
   */
  public Set<PITAttributeAssign> findByOwnerPITGroupIdAndPITAttributeDefNameId(String pitGroupId, String pitAttributeDefNameId);
  
  /**
   * @return source ids of records that have duplicate active entries in PIT
   */
  public Set<String> findActiveDuplicates();
  
  /**
   * Delete (won't run pre and post delete methods)
   * @param id
   */
  public void delete(String id);
}
