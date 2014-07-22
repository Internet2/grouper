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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SchemaException;

/** 
 * Basic <code>Field</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: FieldDAO.java,v 1.9 2009-04-13 16:53:08 mchyzer Exp $
 * @since   1.2.0
 */
public interface FieldDAO extends GrouperDAO {

  /**
   * @param field
   */
  public void createOrUpdate(Field field);

  /**
   * @param name 
   * @return if exists
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  boolean existsByName(String name) 
    throws  GrouperDAOException;

  /**
   * @return all fields
   * @throws GrouperException 
   * @since   1.2.0
   */
  Set<Field> findAll() 
    throws  GrouperException;

  /**
   * @param type 
   * @return set of fields
   * @throws GrouperDAOException 
   * @since   1.2.0
   * @deprecated use the FieldFinder instead
   */
  @Deprecated
  Set<Field> findAllByType(FieldType type) 
    throws  GrouperDAOException;

  /**
   * update in db
   * @param field
   * @throws GrouperDAOException
   */
  void update(Field field) throws  GrouperDAOException;

  /**
   * @param f 
   * @return if in use
   * @throws GrouperDAOException 
   * @throws SchemaException 
   * @since   1.2.0
   */
  boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
            ;

  /**
   * @param uuid 
   * @param name 
   * @param exceptionIfNull 
   * @return the field or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  Field findByUuidOrName(String uuid, String name, boolean exceptionIfNull) throws GrouperDAOException;

  /**
   * @param uuid 
   * @param name 
   * @param exceptionIfNull 
   * @param queryOptions
   * @return the field or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  Field findByUuidOrName(String uuid, String name, boolean exceptionIfNull,
      QueryOptions queryOptions) throws GrouperDAOException;

  /**
   * save the update properties which are auto saved when business method is called
   * @param field
   */
  public void saveUpdateProperties(Field field);


  /**
   * Delete field
   * @param field 
   */
  public void delete(Field field);

  /**
   * Delete fields
   * @param fields 
   */
  public void delete(Set<Field> fields);
} 

