/**
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
 */
/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;

/**
 * 
 */
public interface PITAttributeAssignActionDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssignAction
   */
  public void saveOrUpdate(PITAttributeAssignAction pitAttributeAssignAction);
  
  /**
   * insert or update
   * @param pitAttributeAssignActions
   */
  public void saveOrUpdate(Set<PITAttributeAssignAction> pitAttributeAssignActions);
  
  /**
   * delete
   * @param pitAttributeAssignAction
   */
  public void delete(PITAttributeAssignAction pitAttributeAssignAction);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignAction
   */
  public PITAttributeAssignAction findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignAction
   */
  public PITAttributeAssignAction findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeAssignAction
   */
  public Set<PITAttributeAssignAction> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignAction
   */
  public PITAttributeAssignAction findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   * @return set of PITAttributeAssignAction
   */
  public Set<PITAttributeAssignAction> findByPITAttributeDefId(String id);
  
  /**
   * @return active actions that are missing in point in time
   */
  public Set<AttributeAssignAction> findMissingActivePITAttributeAssignActions();
  
  /**
   * @return active point in time actions that should be inactive
   */
  public Set<PITAttributeAssignAction> findMissingInactivePITAttributeAssignActions();
  
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
