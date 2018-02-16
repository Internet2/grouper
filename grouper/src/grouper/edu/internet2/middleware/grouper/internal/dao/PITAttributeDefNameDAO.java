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

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;

/**
 * 
 */
public interface PITAttributeDefNameDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeDefName
   */
  public void saveOrUpdate(PITAttributeDefName pitAttributeDefName);

  /**
   * insert or update
   * @param pitAttributeDefNames
   */
  public void saveOrUpdate(Set<PITAttributeDefName> pitAttributeDefNames);
  
  /**
   * delete
   * @param pitAttributeDefName
   */
  public void delete(PITAttributeDefName pitAttributeDefName);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefName
   */
  public PITAttributeDefName findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefName
   */
  public PITAttributeDefName findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeDefName
   */
  public Set<PITAttributeDefName> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefName
   */
  public PITAttributeDefName findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param name
   * @param orderByStartTime
   * @return set of pit attribute def names
   */
  public Set<PITAttributeDefName> findByName(String name, boolean orderByStartTime);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   * @return the number of records deleted
   */
  public long deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITAttributeDefName
   */
  public Set<PITAttributeDefName> findByPITAttributeDefId(String id);
  
  /**
   * @param id
   * @return set of PITAttributeDefName
   */
  public Set<PITAttributeDefName> findByPITStemId(String id);
  
  /**
   * @return active attribute def names that are missing in point in time
   */
  public Set<AttributeDefName> findMissingActivePITAttributeDefNames();
  
  /**
   * @return active point in time attribute def names that should be inactive
   */
  public Set<PITAttributeDefName> findMissingInactivePITAttributeDefNames();
  
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
