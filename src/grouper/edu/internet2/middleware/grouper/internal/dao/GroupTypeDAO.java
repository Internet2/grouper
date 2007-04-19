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
import  edu.internet2.middleware.grouper.SchemaException;
import  edu.internet2.middleware.grouper.internal.dto.FieldDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  java.util.Set;

/** 
 * Basic <code>GroupType</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: GroupTypeDAO.java,v 1.2 2007-04-19 14:31:20 blair Exp $
 * @since   1.2.0
 */
public interface GroupTypeDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  public String create(GroupTypeDTO _gt)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public String createField(FieldDTO _f)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public void delete(GroupTypeDTO _gt, Set fields)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public void deleteField(FieldDTO _f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public boolean existsByName(String name)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAll() 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public GroupTypeDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException
            ;

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid();

  /**
   * @since   1.2.0
   */
  public long getCreateTime();

  /**
   * @since   1.2.0
   */
  public Set getFields();

  /**
   * @since   1.2.0
   */
  public boolean getIsAssignable();

  /** 
   * @since   1.2.0
   */
  public boolean getIsInternal();

  /**
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public String getName();

  /**
   * @since   1.2.0
   */
  public String getUuid();

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setCreateTime(long createTime);
  
  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setFields(Set fields);

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setIsAssignable(boolean isAssignable);

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setIsInternal(boolean isInternal);

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setName(String name);

  /**
   * @since   1.2.0
   */
  public GroupTypeDAO setUuid(String uuid);

} 

