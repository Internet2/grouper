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

import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;

/**
 * 
 */
public interface PITAttributeDefNameSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeDefNameSet
   */
  public void saveOrUpdate(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * insert or update
   * @param pitAttributeDefNameSets
   */
  public void saveOrUpdate(Set<PITAttributeDefNameSet> pitAttributeDefNameSets);
  
  /**
   * delete
   * @param pitAttributeDefNameSet
   */
  public void delete(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefNameSet
   */
  public PITAttributeDefNameSet findBySourceIdActive(String id, boolean exceptionIfNotFound);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefNameSet
   */
  public PITAttributeDefNameSet findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefNameSet
   */
  public PITAttributeDefNameSet findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   * @return the number of records deleted
   */
  public long deleteInactiveRecords(Timestamp time);
  
  /**
   * @param pitAttributeDefNameSet
   * @return pit attribute def name sets
   */
  public Set<PITAttributeDefNameSet> findImmediateChildren(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * @param id
   * @return pit attribute def name sets
   */
  public Set<PITAttributeDefNameSet> findAllSelfPITAttributeDefNameSetsByPITAttributeDefNameId(String id);
  
  /**
   * @param id
   */
  public void deleteSelfByPITAttributeDefNameId(String id);
  
  /**
   * @param id
   * @return pit attribute def name sets
   */
  public Set<PITAttributeDefNameSet> findByThenHasPITAttributeDefNameId(String id);
  
  
  /**
   * @return active attribute def name sets that are missing in point in time
   */
  public Set<AttributeDefNameSet> findMissingActivePITAttributeDefNameSets();
  
  /**
   * @return active point in time attribute def name sets that should be inactive
   */
  public Set<PITAttributeDefNameSet> findMissingInactivePITAttributeDefNameSets();
  
  /**
   * @return source ids of records that have duplicate active entries in PIT
   */
  public Set<String> findActiveDuplicates();
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeDefNameSet
   */
  public Set<PITAttributeDefNameSet> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete (won't run pre and post delete methods)
   * @param id
   */
  public void delete(String id);
}
