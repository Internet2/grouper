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
 * @version $Id: CompositeDAO.java,v 1.11 2009-06-09 22:55:39 shilen Exp $
 * @since   1.2.0
 */
public interface CompositeDAO extends GrouperDAO {

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
} 

