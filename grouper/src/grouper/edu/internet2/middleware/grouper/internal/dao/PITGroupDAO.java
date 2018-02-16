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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITRoleSet;
import edu.internet2.middleware.grouper.pit.PITStem;

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
   * insert or update
   * @param pitGroups
   */
  public void saveOrUpdate(Set<PITGroup> pitGroups);
  
  /**
   * delete
   * @param pitGroup
   */
  public void delete(PITGroup pitGroup);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGroup
   */
  public PITGroup findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param createIfNotFound 
   * @param exceptionIfNotFound 
   * @return PITGroup
   */
  public PITGroup findBySourceIdActive(String id, boolean createIfNotFound, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGroup
   */
  public PITGroup findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGroup
   */
  public PITGroup findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of pit groups
   */
  public Set<PITGroup> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param groupName
   * @param orderByStartTime
   * @return set of pit groups
   */
  public Set<PITGroup> findByName(String groupName, boolean orderByStartTime);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   * @return the number of records deleted
   */
  public long deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITGroup
   */
  public Set<PITGroup> findByPITStemId(String id);
  
  /**
   * Get all the groups that a member is a member of.
   * @param pitMemberId 
   * @param pitFieldId 
   * @param scope 
   * @param pitStem
   * @param stemScope
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param queryOptions 
   * @return set of pit groups
   */
  public Set<PITGroup> getAllGroupsMembershipSecure(String pitMemberId, String pitFieldId, String scope,
      PITStem pitStem, Scope stemScope, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions);
  
  /**
   * @return active groups that are missing in point in time
   */
  public Set<Group> findMissingActivePITGroups();
  
  /**
   * @return active point in time groups that should be inactive
   */
  public Set<PITGroup> findMissingInactivePITGroups();
  
  /**
   * Find the roles that have permissions containing the specified object
   * @param assign
   * @return set of pit groups
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITAttributeAssign assign);
  
  /**
   * Find the roles that have permissions containing the specified object
   * @param actionSet
   * @return set of pit groups
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITAttributeAssignActionSet actionSet);
  
  /**
   * Find the roles that have permissions containing the specified object
   * @param attributeDefNameSet
   * @return set of pit groups
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITAttributeDefNameSet attributeDefNameSet);
  
  /**
   * Find the roles that have permissions containing the specified object
   * @param roleSet
   * @return set of pit groups
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITRoleSet roleSet);
  
  /**
   * Find the roles that have permissions containing the specified object
   * @param membership
   * @return set of pit groups
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITMembership membership);
  
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
