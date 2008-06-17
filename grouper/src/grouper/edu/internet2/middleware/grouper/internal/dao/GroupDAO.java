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
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  java.util.Date;
import  java.util.Map;
import  java.util.Set;

/** 
 * Basic <code>Group</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: GroupDAO.java,v 1.8.2.1 2008-06-17 17:00:23 mchyzer Exp $
 * @since   1.2.0
 */
public interface GroupDAO extends GrouperDAO {

  /**
   * set the dto so data can be passed back
   * @param groupDTO
   */
  public void setGroupDTO(GroupDTO groupDTO);
  
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
  Map findAllAttributesByGroup(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByAnyApproximateAttr(String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  Set findAllByApproximateAttr(String attr, String val) 
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
  Set findAllByApproximateName(String name) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByModifiedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByModifiedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByType(GroupTypeDTO _gt)
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
  Map getAttributes();
  
  /**
   * @since   1.2.0
   */
  String getCreateSource();

  /**
   * @since   1.2.0
   */
  long getCreateTime();

  /**
   * @since   1.2.0
   */
  String getCreatorUuid();

  /**
   * @since   1.2.0
   */
  String getId();

  /**
   * @since   1.2.0
   */
  String getModifierUuid();

  /**
   * @since   1.2.0
   */
  String getModifySource();

  /**
   * @since   1.2.0
   */
  long getModifyTime();

  /**
   * @since   1.2.0
   */
  String getParentUuid();

  /**
   * @since   1.2.0
   */
  Set getTypes();
  
  /**
   * @since   1.2.0
   */
  String getUuid();

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
  GroupDAO setAttributes(Map attributes);

  /**
   * @since   1.2.0
   */
  GroupDAO setCreateSource(String createSource);

  /**
   * @since   1.2.0
   */
  GroupDAO setCreateTime(long createTime);

  /**
   * @since   1.2.0
   */
  GroupDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  GroupDAO setId(String id);

  /**
   * @since   1.2.0
   */
  GroupDAO setModifierUuid(String modifierUUID);

  /**
   * @since   1.2.0
   */
  GroupDAO setModifySource(String modifySource);

  /**
   * @since   1.2.0
   */
  GroupDAO setModifyTime(long modifyTime);

  /**
   * @since   1.2.0
   */
  GroupDAO setParentUuid(String parentUUID);

  /**
   * @since   1.2.0
   */
  GroupDAO setTypes(Set types);

  /**
   * @since   1.2.0
   */
  GroupDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  void update(GroupDTO _g)
    throws  GrouperDAOException;

} 

