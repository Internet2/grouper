/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.pit.PITRoleSet;

/**
 * 
 */
public interface PITRoleSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitRoleSet
   */
  public void saveOrUpdate(PITRoleSet pitRoleSet);
  
  /**
   * insert or update
   * @param pitRoleSets
   */
  public void saveOrUpdate(Set<PITRoleSet> pitRoleSets);
  
  /**
   * delete
   * @param pitRoleSet
   */
  public void delete(PITRoleSet pitRoleSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITRoleSet
   */
  public PITRoleSet findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITRoleSet
   */
  public PITRoleSet findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITRoleSet
   */
  public PITRoleSet findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param pitRoleSet
   * @return pit role sets
   */
  public Set<PITRoleSet> findImmediateChildren(PITRoleSet pitRoleSet);
  
  /**
   * @param id
   * @return pit role sets
   */
  public Set<PITRoleSet> findAllSelfPITRoleSetsByPITRoleId(String id);
  
  /**
   * @param id
   */
  public void deleteSelfByPITRoleId(String id);
  
  /**
   * @param id
   * @return pit role sets
   */
  public Set<PITRoleSet> findByThenHasPITRoleId(String id);
  
  /**
   * @return active role sets that are missing in point in time
   */
  public Set<RoleSet> findMissingActivePITRoleSets();
  
  /**
   * @return active point in time role sets that should be inactive
   */
  public Set<PITRoleSet> findMissingInactivePITRoleSets();
  
  /**
   * @return source ids of records that have duplicate active entries in PIT
   */
  public Set<String> findActiveDuplicates();
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITRoleSet
   */
  public Set<PITRoleSet> findBySourceId(String id, boolean exceptionIfNotFound);

  /**
   * Delete (won't run pre and post delete methods)
   * @param id
   */
  public void delete(String id);
}
