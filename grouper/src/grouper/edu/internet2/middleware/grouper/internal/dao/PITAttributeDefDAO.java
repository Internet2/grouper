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

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;

/**
 * 
 */
public interface PITAttributeDefDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeDef
   */
  public void saveOrUpdate(PITAttributeDef pitAttributeDef);
  
  /**
   * insert or update
   * @param pitAttributeDefs
   */
  public void saveOrUpdate(Set<PITAttributeDef> pitAttributeDefs);
  
  /**
   * delete
   * @param pitAttributeDef
   */
  public void delete(PITAttributeDef pitAttributeDef);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDef
   */
  public PITAttributeDef findBySourceIdActive(String id, boolean exceptionIfNotFound);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDef
   */
  public PITAttributeDef findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDef
   */
  public PITAttributeDef findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeDef
   */
  public Set<PITAttributeDef> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param name
   * @param orderByStartTime
   * @return set of pit attribute defs
   */
  public Set<PITAttributeDef> findByName(String name, boolean orderByStartTime);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITAttributeDef
   */
  public Set<PITAttributeDef> findByPITStemId(String id);
  
  /**
   * @return active attribute defs that are missing in point in time
   */
  public Set<AttributeDef> findMissingActivePITAttributeDefs();
  
  /**
   * @return active point in time attribute defs that should be inactive
   */
  public Set<PITAttributeDef> findMissingInactivePITAttributeDefs();
  
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
