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

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.pit.PITMember;

/**
 * 
 */
public interface PITConfigDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitGrouperConfigHibernate
   */
  public void saveOrUpdate(PITGrouperConfigHibernate pitGrouperConfigHibernate);
  
  /**
   * insert or update
   * @param pitGrouperConfigHibernates
   */
  public void saveOrUpdate(Set<PITGrouperConfigHibernate> pitGrouperConfigHibernates);
  
  /**
   * delete
   * @param pitGrouperConfigHibernate
   */
  public void delete(PITGrouperConfigHibernate pitGrouperConfigHibernate);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGrouperConfigHibernate
   */
  public PITGrouperConfigHibernate findBySourceIdActive(String id, boolean exceptionIfNotFound);
  
  /**
   * @param ids
   * @return
   */
  public Set<PITGrouperConfigHibernate> findBySourceIdsActive(Collection<String> ids);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGrouperConfigHibernate
   */
  public PITGrouperConfigHibernate findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param ids
   * @return PITGrouperConfigHibernate
   */
  public Set<PITGrouperConfigHibernate> findByIds(Collection<String> ids);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITGrouperConfigHibernate
   */
  public PITGrouperConfigHibernate findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITGrouperConfigHibernate
   */
  public Set<PITGrouperConfigHibernate> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   * @return the number of records deleted
   */
  public long deleteInactiveRecords(Timestamp time);
  
  /**
   * @param id
   */
  public void delete(String id);
  
  /**
   * @return active configs that are missing in point in time
   */
  public Set<GrouperConfigHibernate> findMissingActivePITConfigs();
  
  /**
   * @return active point in time configs that should be inactive
   */
  public Set<PITGrouperConfigHibernate> findMissingInactivePITConfigs();
}
