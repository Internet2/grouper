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
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.SchemaException;

/** 
 * Basic <code>GroupType</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: GroupTypeDAO.java,v 1.10 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.2.0
 */
public interface GroupTypeDAO extends GrouperDAO {

  /**
   * 
   * @param _gt
   * @throws GrouperDAOException 
   */
  public void createOrUpdate(GroupType _gt) throws GrouperDAOException;

  /**
   * @param _f 
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  void createField(Field _f)
    throws  GrouperDAOException;

  /**
   * @param _gt 
   * @param fields 
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  void delete(GroupType _gt, Set fields)
    throws  GrouperDAOException;

  /**
   * @param _f 
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  void deleteField(Field _f) 
    throws  GrouperDAOException;

  /**
   * @param name 
   * @return boolean
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  boolean existsByName(String name)
    throws  GrouperDAOException;

  /**
   * @return set of types
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<GroupType> findAll() 
    throws  GrouperDAOException;

  /**
   * @param uuid 
   * @return uuid
   * @throws GrouperDAOException 
   * @throws SchemaException 
   * @since   1.2.0
   * @deprecated use overload instead
   */
  @Deprecated
  GroupType findByUuid(String uuid)
    throws  GrouperDAOException,
            SchemaException
            ;

  /**
   * @param uuid 
   * @param exceptionIfNull 
   * @return uuid
   * @throws GrouperDAOException 
   * @throws SchemaException 
   * @since   1.2.0
   */
  GroupType findByUuid(String uuid, boolean exceptionIfNull)
    throws  GrouperDAOException,
            SchemaException
            ;

  /**
   * find all groupTypes by creator
   * @param member
   * @return the groupTypes
   */
  Set<GroupType> findAllByCreator(Member member);  
  
  /**
   * @param uuid 
   * @param name 
   * @param exceptionIfNull 
   * @return the stem or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  GroupType findByUuidOrName(String uuid, String name, boolean exceptionIfNull) throws GrouperDAOException;

  /**
   * @param uuid 
   * @param name 
   * @param exceptionIfNull 
   * @param queryOptions
   * @return the stem or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  GroupType findByUuidOrName(String uuid, String name, boolean exceptionIfNull, QueryOptions queryOptions) throws GrouperDAOException;

  /**
   * update in db
   * @param groupType
   * @throws GrouperDAOException
   */
  void update(GroupType groupType) throws  GrouperDAOException;

  /**
   * save the update properties which are auto saved when business method is called
   * @param groupType
   */
  public void saveUpdateProperties(GroupType groupType);

} 

