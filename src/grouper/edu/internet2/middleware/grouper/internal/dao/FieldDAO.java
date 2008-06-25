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
import edu.internet2.middleware.grouper.GrouperRuntimeException;
import edu.internet2.middleware.grouper.SchemaException;

/** 
 * Basic <code>Field</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: FieldDAO.java,v 1.5 2008-06-25 05:46:05 mchyzer Exp $
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
  Set<Field> findAll() 
    throws  GrouperRuntimeException;

  /**
   * @since   1.2.0
   */
  Set<Field> findAllFieldsByGroupType(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Field> findAllByType(FieldType type) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
            ;

} 

