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

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.pit.PITMember;

/**
 * 
 */
public interface PITMemberDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitMember
   */
  public void saveOrUpdate(PITMember pitMember);
  
  /**
   * insert or update
   * @param pitMembers
   */
  public void saveOrUpdate(Set<PITMember> pitMembers);
  
  /**
   * delete
   * @param pitMember
   */
  public void delete(PITMember pitMember);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMember
   */
  public PITMember findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param createIfNotFound
   * @param exceptionIfNotFound 
   * @return PITMember
   */
  public PITMember findBySourceIdActive(String id, boolean createIfNotFound, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMember
   */
  public PITMember findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMember
   */
  public PITMember findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITMember
   */
  public Set<PITMember> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   * @return the number of records deleted
   */
  public long deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @param source
   * @param type
   * @return pit members
   */
  public Set<PITMember> findPITMembersBySubjectIdSourceAndType(String id, String source, String type);
  
  /**
   * @return active members that are missing in point in time
   */
  public Set<Member> findMissingActivePITMembers();
  
  /**
   * @return active point in time members that should be inactive
   */
  public Set<PITMember> findMissingInactivePITMembers();
  
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
