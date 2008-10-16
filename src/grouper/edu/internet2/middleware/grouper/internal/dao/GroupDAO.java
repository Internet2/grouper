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
import java.util.Date;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;

/** 
 * Basic <code>Group</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: GroupDAO.java,v 1.13 2008-10-16 05:45:47 mchyzer Exp $
 * @since   1.2.0
 */
public interface GroupDAO extends GrouperDAO {

  /**
   * find al types for a group
   * @param uuid
   * @return the types
   */
  public Set<GroupType> _findAllTypesByGroup(final String uuid);
  
  /**
   * update the attributes for a group
   * @param hibernateSession 
   * @param checkExisting true if an update, false if insert
   */
  public void _updateAttributes(HibernateSession hibernateSession, boolean checkExisting, Group group);

  /**
   * put in cache
   * @param uuid
   * @param exists
   */
  public void putInExistsCache(String uuid, boolean exists);

  /**
   * @since   1.2.0
   */
  void addType(Group _g, GroupType _gt) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(Group _g, Set mships)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void deleteType(Group _g, GroupType _gt) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean exists(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Map<String, String> findAllAttributesByGroup(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByAnyApproximateAttr(String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByApproximateAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.3
   */
  Set<Group> findAllByAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByApproximateName(String name) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByModifiedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByModifiedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByType(GroupType _gt)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Group findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  Group findByName(String name) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  Group findByUuid(String uuid) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.3.1
   */
  Set<Group> getAllGroups()
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void revokePriv(Group _g, DefaultMemberOf mof)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void revokePriv(Group _g, Set toDelete)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void update(Group _g)
    throws  GrouperDAOException;

  /**
   * find groups by creator or modifier
   * @param member
   * @return the groups
   */
  Set<Group> findByCreatorOrModifier(Member member);
} 

