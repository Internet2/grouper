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
import java.util.Collection;
import java.util.Map;

import edu.internet2.middleware.grouper.Attribute;

/** 
 * Basic <code>Attribute</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: AttributeDAO.java,v 1.1 2009-03-24 17:12:08 mchyzer Exp $
 * @since   1.2.0
 */
public interface AttributeDAO extends GrouperDAO {

  /**
   * @param attribute
   */
  public void createOrUpdate(Attribute attribute);

  /**
   * 
   * @param uuid
   * @return map of attribute names to attribute
   * @throws GrouperDAOException
   */
  Map<String, Attribute> findAllAttributesByGroup(String uuid)
    throws  GrouperDAOException;

  /**
   * 
   * @param uuids
   * @return map of grouper uuid to map of attribute names to attribute
   * note, if there is no attributes, return an empty map anyways
   * @throws GrouperDAOException
   */
  Map<String,Map<String, Attribute>> findAllAttributesByGroups(Collection<String> uuids)
    throws  GrouperDAOException;

  /**
   * delete an attribute from the database
   * @param attribute
   */
  public void delete(Attribute attribute);

  /**
   * 
   * @param id
   * @param groupUUID
   * @param fieldId
   * @param exceptionIfNotFound
   * @return the attribute or null
   */
  public Attribute findByUuidOrName(String id, String groupUUID, String fieldId, boolean exceptionIfNotFound);
  
  /**
   * save the update properties which are auto saved when business method is called
   * @param attribute
   */
  public void saveUpdateProperties(Attribute attribute);

} 
