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
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.SchemaException;

/** 
 * Basic <code>Field</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: FieldDAO.java,v 1.7.2.1 2009-03-26 06:26:17 mchyzer Exp $
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
   * @throws GrouperRuntimeException 
   * @since   1.2.0
   */
  Set<Field> findAll() 
    throws  GrouperRuntimeException;

  /**
   * @param uuid 
   * @return set of fields
   * @throws GrouperDAOException 
   * @since   1.2.0
   * @deprecated use the FieldFinder method instead
   */
  @Deprecated
  Set<Field> findAllFieldsByGroupType(String uuid)
    throws  GrouperDAOException;

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

} 

