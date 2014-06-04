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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.privs.Privilege;

/** 
 * Basic <code>Entity</code> DAO interface.
 * @author  chris hyzer.
 * @version $Id: GroupDAO.java,v 1.30 2009-12-10 08:54:15 mchyzer Exp $
 * @since   2.1.0
 */
public interface EntityDAO extends GrouperDAO {

  /**
   * find entities
   * @param grouperSession 
   * @param ancestorFolderIds 
   * @param ancestorFolderNames 
   * @param ids 
   * @param names 
   * @param parentFolderIds 
   * @param parentFolderNames 
   * @param terms 
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @param queryOptions 
   * @return the entities
   * @throws GrouperDAOException
   */
  Set<Entity> findEntitiesSecure(GrouperSession grouperSession, 
      List<String> ancestorFolderIds, List<String> ancestorFolderNames, 
      List<String> ids, List<String> names, List<String> parentFolderIds,
      List<String> parentFolderNames, String terms, Set<Privilege> inPrivSet, QueryOptions queryOptions);

  /**
   * find entities secure by group id
   * @param grouperSession 
   * @param groupIds  (note, can be any amount of group ids, will batch)
   * @return the group, and attribute value tuple
   * @throws GrouperDAOException
   */
  List<Object[]> findEntitiesByGroupIds(Collection<String> groupIds);

} 

