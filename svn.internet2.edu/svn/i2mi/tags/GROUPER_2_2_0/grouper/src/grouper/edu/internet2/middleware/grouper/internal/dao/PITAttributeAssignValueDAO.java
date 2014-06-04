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

import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;

/**
 * 
 */
public interface PITAttributeAssignValueDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeAssignValue
   */
  public void saveOrUpdate(PITAttributeAssignValue pitAttributeAssignValue);
  
  /**
   * insert or update
   * @param pitAttributeAssignValues
   */
  public void saveOrUpdate(Set<PITAttributeAssignValue> pitAttributeAssignValues);
  
  /**
   * delete
   * @param pitAttributeAssignValue
   */
  public void delete(PITAttributeAssignValue pitAttributeAssignValue);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignValue
   */
  public PITAttributeAssignValue findBySourceIdActive(String id, boolean exceptionIfNotFound);

  /**  
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignValue
   */
  public PITAttributeAssignValue findById(String id, boolean exceptionIfNotFound);

  /**  
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeAssignValue
   */
  public PITAttributeAssignValue findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updatePITAttributeAssignId(String oldId, String newId);
  
  /**
   * @param id
   * @return set of PITAttributeAssignValue
   */
  public Set<PITAttributeAssignValue> findActiveByPITAttributeAssignId(String id);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * Find values by point in time attribute assign id
   * @param attributeAssignId
   * @param queryOptions
   * @return set of values
   */
  public Set<PITAttributeAssignValue> findByPITAttributeAssignId(String attributeAssignId, QueryOptions queryOptions);
  
  /**
   * @return active attribute assign values that are missing in point in time
   */
  public Set<AttributeAssignValue> findMissingActivePITAttributeAssignValues();
  
  /**
   * @return active point in time attribute assign values that should be inactive
   */
  public Set<PITAttributeAssignValue> findMissingInactivePITAttributeAssignValues();
  
  /**
   * @return source ids of records that have duplicate active entries in PIT
   */
  public Set<String> findActiveDuplicates();
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITAttributeAssignValue
   */
  public Set<PITAttributeAssignValue> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete (won't run pre and post delete methods)
   * @param id
   */
  public void delete(String id);
}
