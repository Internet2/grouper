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
 * @author  blair christensen.
 * @version $Id: GroupTypeDAO.java,v 1.3.6.1 2008-06-18 09:22:21 mchyzer Exp $
 * @since   1.2.0
 */
public interface GroupTypeDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  void create(GroupTypeDTO _gt)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void createField(FieldDTO _f)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(GroupTypeDTO _gt, Set fields)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void deleteField(FieldDTO _f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean existsByName(String name)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<GroupTypeDTO> findAll() 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  GroupTypeDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException
            ;

} 

