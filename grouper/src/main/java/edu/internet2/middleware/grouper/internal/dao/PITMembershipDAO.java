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

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.pit.PITMembership;

/**
 * 
 */
public interface PITMembershipDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitMembership
   */
  public void saveOrUpdate(PITMembership pitMembership);
  
  /**
   * insert or update
   * @param pitMemberships
   */
  public void saveOrUpdate(Set<PITMembership> pitMemberships);
  
  /**
   * delete
   * @param pitMembership
   */
  public void delete(PITMembership pitMembership);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMembership
   */
  public PITMembership findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param ids
   * @return PITMembership
   */
  public Set<PITMembership> findBySourceIdsActive(Collection<String> ids);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMembership
   */
  public PITMembership findById(String id, boolean exceptionIfNotFound);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITMembership
   */
  public Set<PITMembership> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param ids
   * @return set of PITMembership
   */
  public Set<PITMembership> findBySourceIds(Collection<String> ids);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMembership
   */
  public PITMembership findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMembership
   */
  public PITMembership findBySourceIdMostRecent(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   * @return the number of records deleted
   */
  public long deleteInactiveRecords(Timestamp time);
  
  /**
   * Get memberships by owner.
   * @param ownerId
   * @return set of pit memberships
   */
  public Set<PITMembership> findAllByPITOwner(String ownerId);
  
  /**
   * Get memberships by member.
   * @param memberId
   * @return set of pit memberships
   */
  public Set<PITMembership> findAllByPITMember(String memberId);
  
  /**
   * Get memberships by owner, member, field
   * @param ownerId
   * @param memberId
   * @param fieldId
   * @return set of pit memberships
   */
  public Set<PITMembership> findAllByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId);
  
  /**
   * @return active memberships that are missing in point in time
   */
  public Set<Membership> findMissingActivePITMemberships();
  
  /**
   * @return active point in time memberships that should be inactive
   */
  public Set<PITMembership> findMissingInactivePITMemberships();
  
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
