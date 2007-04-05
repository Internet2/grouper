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

package edu.internet2.middleware.grouper;
import  java.util.Set;

/** 
 * <i>Field</i> DAO interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: FieldDAO.java,v 1.1 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
interface FieldDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  public boolean existsByName(String name) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAll() 
    throws  GrouperRuntimeException;

  /**
   * @since   1.2.0
   */
  public Set findAllFieldsByGroupType(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByType(FieldType type) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public String getGroupTypeUuid();

  /**
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public boolean getIsNullable();

  /**
   * @since   1.2.0
   */
  public String getName();

  /**
   * @since   1.2.0
   */
  public String getReadPrivilege();

  /**
   * @since   1.2.0
   */
  public String getType();
 
  /**
   * @since   1.2.0
   */
  public String getUuid();

  /**
   * @since   1.2.0
   */
  public String getWritePrivilege();

  /**
   * @since   1.2.0
   */
  public boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
            ;

  /**
   * @since   1.2.0
   */
  public FieldDAO setGroupTypeUuid(String groupTypeUUID);

  /**
   * @since   1.2.0
   */
  public FieldDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public FieldDAO setIsNullable(boolean isNullable);

  /**
   * @since   1.2.0
   */
  public FieldDAO setName(String name);

  /**
   * @since   1.2.0
   */
  public FieldDAO setReadPrivilege(String readPrivilege);

  /**
   * @since   1.2.0
   */
  public FieldDAO setType(String type);

  /**
   * @since   1.2.0
   */
  public FieldDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  public FieldDAO setWritePrivilege(String writePrivilege);

} // interface FieldDAO extends GrouperDAO

