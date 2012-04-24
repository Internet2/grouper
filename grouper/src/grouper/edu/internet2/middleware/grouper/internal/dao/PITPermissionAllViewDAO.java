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
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;

/**
 * 
 */
public interface PITPermissionAllViewDAO extends GrouperDAO {

  /**
   * @param attributeDefIds
   * @param attributeDefNameIds
   * @param roleIds
   * @param actions
   * @param memberIds
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @return set
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefIds, Collection<String> attributeDefNameIds, 
      Collection<String> roleIds, Collection<String> actions, Collection<String> memberIds, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo);
}
