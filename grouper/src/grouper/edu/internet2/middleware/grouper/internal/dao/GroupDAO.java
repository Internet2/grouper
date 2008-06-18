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
import  edu.internet2.middleware.grouper.GroupNotFoundException;
import  edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  java.util.Date;
import  java.util.Map;
import  java.util.Set;

/** 
 * Basic <code>Group</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: GroupDAO.java,v 1.8.2.2 2008-06-18 09:22:21 mchyzer Exp $
 * @since   1.2.0
 */
public interface GroupDAO extends GrouperDAO {

  /**
   * find al types for a group
   * @param uuid
   * @return the types
   */
  public Set<GroupTypeDTO> _findAllTypesByGroup(final String uuid);
  
  /**
   * update the attributes for a group
   * @param hibernateSession 
   * @param checkExisting true if an update, false if insert
   */
  public void _updateAttributes(HibernateSession hibernateSession, boolean checkExisting, GroupDTO groupDTO);

  /**
   * put in cache
   * @param uuid
   * @param exists
   */
  public void putInExistsCache(String uuid, boolean exists);

  /**
   * @since   1.2.0
   */
  void addType(GroupDTO _g, GroupTypeDTO _gt) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(GroupDTO _g, Set mships)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void deleteType(GroupDTO _g, GroupTypeDTO _gt) 
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
  Set<GroupDTO> findAllByAnyApproximateAttr(String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  Set<GroupDTO> findAllByApproximateAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.3
   */
  Set<GroupDTO> findAllByAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  Set<GroupDTO> findAllByApproximateName(String name) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  Set<GroupDTO> findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<GroupDTO> findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<GroupDTO> findAllByModifiedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<GroupDTO> findAllByModifiedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<GroupDTO> findAllByType(GroupTypeDTO _gt)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  GroupDTO findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  GroupDTO findByName(String name) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  GroupDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  void revokePriv(GroupDTO _g, DefaultMemberOf mof)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void revokePriv(GroupDTO _g, Set toDelete)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void update(GroupDTO _g)
    throws  GrouperDAOException;

} 

