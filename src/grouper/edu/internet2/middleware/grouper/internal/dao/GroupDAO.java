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
 * @version $Id: GroupDAO.java,v 1.6 2007-04-19 16:48:43 blair Exp $
 * @since   1.2.0
 */
public interface GroupDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  public void addType(GroupDTO _g, GroupTypeDTO _gt) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public void delete(GroupDTO _g, Set mships)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public void deleteType(GroupDTO _g, GroupTypeDTO _gt) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public boolean exists(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Map findAllAttributesByGroup(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByAnyApproximateAttr(String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateName(String name) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByModifiedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByModifiedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByType(GroupTypeDTO _gt)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public GroupDTO findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  public GroupDTO findByName(String name) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  public GroupDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  public Map getAttributes();
  
  /**
   * @since   1.2.0
   */
  public String getCreateSource();

  /**
   * @since   1.2.0
   */
  public long getCreateTime();

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid();

  /**
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public String getModifierUuid();

  /**
   * @since   1.2.0
   */
  public String getModifySource();

  /**
   * @since   1.2.0
   */
  public long getModifyTime();

  /**
   * @since   1.2.0
   */
  public String getParentUuid();

  /**
   * @since   1.2.0
   */
  public Set getTypes();
  
  /**
   * @since   1.2.0
   */
  public String getUuid();

  /**
   * @since   1.2.0
   */
  public void revokePriv(GroupDTO _g, DefaultMemberOf mof)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public void revokePriv(GroupDTO _g, Set toDelete)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public GroupDAO setAttributes(Map attributes);

  /**
   * @since   1.2.0
   */
  public GroupDAO setCreateSource(String createSource);

  /**
   * @since   1.2.0
   */
  public GroupDAO setCreateTime(long createTime);

  /**
   * @since   1.2.0
   */
  public GroupDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  public GroupDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public GroupDAO setModifierUuid(String modifierUUID);

  /**
   * @since   1.2.0
   */
  public GroupDAO setModifySource(String modifySource);

  /**
   * @since   1.2.0
   */
  public GroupDAO setModifyTime(long modifyTime);

  /**
   * @since   1.2.0
   */
  public GroupDAO setParentUuid(String parentUUID);

  /**
   * @since   1.2.0
   */
  public GroupDAO setTypes(Set types);

  /**
   * @since   1.2.0
   */
  public GroupDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  public void update(GroupDTO _g)
    throws  GrouperDAOException;

} 

