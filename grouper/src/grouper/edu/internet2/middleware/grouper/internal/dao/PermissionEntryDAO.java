/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;

/** 
 * Basic <code>PermissionEntry</code> DAO interface.
 * @author  mchyzer
 * @version $Id: PermissionEntryDAO.java,v 1.2 2009-10-12 09:46:34 mchyzer Exp $
 */
public interface PermissionEntryDAO extends GrouperDAO {

  /**
   * find all permissions that a subject has
   * @param memberId
   * @return the permissions
   */
  public Set<PermissionEntry> findByMemberId(String memberId);
  
  /**
   * get attribute assigns by member and attribute def name id
   * @param memberId
   * @param attributeDefNameId
   * @return set of assigns or empty if none there
   */
  public Set<PermissionEntry> findByMemberIdAndAttributeDefNameId(String memberId, String attributeDefNameId);

} 

