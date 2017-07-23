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

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.pit.PITStem;

/**
 * 
 */
public interface PITStemDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitStem
   */
  public void saveOrUpdate(PITStem pitStem);
  
  /**
   * insert or update
   * @param pitStems
   */
  public void saveOrUpdate(Set<PITStem> pitStems);
  
  /**
   * delete
   * @param pitStem
   */
  public void delete(PITStem pitStem);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITStem
   */
  public PITStem findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param createIfNotFound 
   * @param exceptionIfNotFound 
   * @return PITStem
   */
  public PITStem findBySourceIdActive(String id, boolean createIfNotFound, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITStem
   */
  public PITStem findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITStem
   */
  public Set<PITStem> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITStem
   */
  public PITStem findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITStem
   */
  public Set<PITStem> findByParentPITStemId(String id);
  
  /**
   * @param stemName
   * @param orderByStartTime
   * @return set of pit stems
   */
  public Set<PITStem> findByName(String stemName, boolean orderByStartTime);
  
  /**
   * @return active stems that are missing in point in time
   */
  public Set<Stem> findMissingActivePITStems();
  
  /**
   * @return active point in time stems that should be inactive
   */
  public Set<PITStem> findMissingInactivePITStems();
  
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
