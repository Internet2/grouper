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
import  edu.internet2.middleware.grouper.Field;
import  edu.internet2.middleware.grouper.FieldType;
import  edu.internet2.middleware.grouper.GrouperRuntimeException;
import  edu.internet2.middleware.grouper.SchemaException;
import  java.util.Set;

/** 
 * Basic <code>Field</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: FieldDAO.java,v 1.3 2007-04-19 19:23:21 blair Exp $
 * @since   1.2.0
 */
public interface FieldDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  boolean existsByName(String name) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAll() 
    throws  GrouperRuntimeException;

  /**
   * @since   1.2.0
   */
  Set findAllFieldsByGroupType(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByType(FieldType type) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  String getGroupTypeUuid();

  /**
   * @since   1.2.0
   */
  String getId();

  /**
   * @since   1.2.0
   */
  boolean getIsNullable();

  /**
   * @since   1.2.0
   */
  String getName();

  /**
   * @since   1.2.0
   */
  String getReadPrivilege();

  /**
   * @since   1.2.0
   */
  String getType();
 
  /**
   * @since   1.2.0
   */
  String getUuid();

  /**
   * @since   1.2.0
   */
  String getWritePrivilege();

  /**
   * @since   1.2.0
   */
  boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
            ;

  /**
   * @since   1.2.0
   */
  FieldDAO setGroupTypeUuid(String groupTypeUUID);

  /**
   * @since   1.2.0
   */
  FieldDAO setId(String id);

  /**
   * @since   1.2.0
   */
  FieldDAO setIsNullable(boolean isNullable);

  /**
   * @since   1.2.0
   */
  FieldDAO setName(String name);

  /**
   * @since   1.2.0
   */
  FieldDAO setReadPrivilege(String readPrivilege);

  /**
   * @since   1.2.0
   */
  FieldDAO setType(String type);

  /**
   * @since   1.2.0
   */
  FieldDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  FieldDAO setWritePrivilege(String writePrivilege);

} 

