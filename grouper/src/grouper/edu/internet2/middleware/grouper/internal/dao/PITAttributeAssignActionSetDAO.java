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

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;

/**
 * 
 */
public interface PITAttributeAssignActionSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssignActionSet
   */
  public void saveOrUpdate(PITAttributeAssignActionSet pitAttributeAssignActionSet);
  
  /**
   * insert or update
   * @param pitAttributeAssignActionSets
   */
  public void saveOrUpdate(Set<PITAttributeAssignActionSet> pitAttributeAssignActionSets);
  
  /**
   * delete
   * @param pitAttributeAssignActionSet
   */
  public void delete(PITAttributeAssignActionSet pitAttributeAssignActionSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignActionSet
   */
  public PITAttributeAssignActionSet findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignActionSet
   */
  public PITAttributeAssignActionSet findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignActionSet
   */
  public PITAttributeAssignActionSet findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param pitAttributeAssignActionSet
   * @return pit action sets
   */
  public Set<PITAttributeAssignActionSet> findImmediateChildren(PITAttributeAssignActionSet pitAttributeAssignActionSet);
  
  /**
   * @param id
   * @return pit action sets
   */
  public Set<PITAttributeAssignActionSet> findAllSelfPITAttributeAssignActionSetsByPITAttributeAssignActionId(String id);
  
  /**
   * @param id
   */
  public void deleteSelfByPITAttributeAssignActionId(String id);
  
  /**
   * @param id
   * @return pit action sets
   */
  public Set<PITAttributeAssignActionSet> findByThenHasPITAttributeAssignActionId(String id);
  
  /**
   * @return active action sets that are missing in point in time
   */
  public Set<AttributeAssignActionSet> findMissingActivePITAttributeAssignActionSets();
  
  /**
   * @return active point in time action sets that should be inactive
   */
  public Set<PITAttributeAssignActionSet> findMissingInactivePITAttributeAssignActionSets();
  
  /**
   * @return source ids of records that have duplicate active entries in PIT
   */
  public Set<String> findActiveDuplicates();
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeAssignActionSet
   */
  public Set<PITAttributeAssignActionSet> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete (won't run pre and post delete methods)
   * @param id
   */
  public void delete(String id);
}
