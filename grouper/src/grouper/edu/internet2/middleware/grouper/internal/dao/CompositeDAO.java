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

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;

/** 
 * Basic <code>Composite</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: CompositeDAO.java,v 1.12 2009-08-12 12:44:45 shilen Exp $
 * @since   1.2.0
 */
public interface CompositeDAO extends GrouperDAO {

  /**
   * @param groupId
   * @return Set of composites
   * @since   1.5.0
   */
  Set<Composite> findAsFactorOrHasMemberOfFactor(String groupId);
  
  /**
   * @since   1.2.0
   */
  Set<Composite> findAsFactor(Group _g)
    throws  GrouperDAOException;

  /**
   * @param groupId 
   * @return set of composites
   * @throws GrouperDAOException 
   * @since   1.5.0
   */
  Set<Composite> findAsFactor(String groupId)
    throws  GrouperDAOException;
  
  /**
   * @since   1.2.0
   */
  Composite findAsOwner(Group _g, boolean exceptionIfNotFound) 
    throws  CompositeNotFoundException,
            GrouperDAOException
            ;

  /**
   * @since   1.2.0
   */
  Composite findByUuid(String uuid, boolean exceptionIfNotFound) 
    throws  CompositeNotFoundException,
            GrouperDAOException
            ;

  /**
   * @since   1.3.1
   */
  Set<Composite> getAllComposites()
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void update(Set toAdd, Set toDelete, Set modGroups, Set modStems) 
    throws  GrouperDAOException;

  /**
   * find all composites by creator
   * @param member
   * @return the composites
   */
  Set<Composite> findByCreator(Member member);  
  
  /**
   * Save a composite
   * @param c The composite to save.
   */
  public void save(Composite c);

  /**
   * update a composite
   * @param c The composite to update.
   */
  public void update(Composite c);

  /**
   * Delete a composite
   * @param c The composite to delete.
   */
  public void delete(Composite c);
  
  /**
   * find a composite by name or uuid
   * @param uuid
   * @param factorOwnerUUID
   * @param leftFactorUUID
   * @param rightFactorUUID
   * @param type
   * @param exceptionIfNull
   * @return the composite or null
   */
  public Composite findByUuidOrName(String uuid, String factorOwnerUUID, String leftFactorUUID, 
      String rightFactorUUID, String type, boolean exceptionIfNull);

  /**
   * find a composite by name or uuid
   * @param uuid
   * @param factorOwnerUUID
   * @param leftFactorUUID
   * @param rightFactorUUID
   * @param type
   * @param exceptionIfNull
   * @param queryOptions
   * @return the composite or null
   */
  public Composite findByUuidOrName(String uuid, String factorOwnerUUID, String leftFactorUUID, 
      String rightFactorUUID, String type, boolean exceptionIfNull, QueryOptions queryOptions);

  /**
   * save the update properties which are auto saved when business method is called
   * @param composite
   */
  public void saveUpdateProperties(Composite composite);

} 

