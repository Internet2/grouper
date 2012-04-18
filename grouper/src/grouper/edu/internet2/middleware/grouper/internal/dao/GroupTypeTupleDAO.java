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
import edu.internet2.middleware.grouper.GroupTypeTuple;

/** 
 * Basic <code>GroupType</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: GroupTypeDAO.java,v 1.10 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.2.0
 */
public interface GroupTypeTupleDAO extends GrouperDAO {

  /**
   * @param uuid 
   * @param groupUuid 
   * @param typeUuid
   * @param exceptionIfNull 
   * @return the stem or null
   * @throws GrouperDAOException 
   * @since   1.6.0
   */
  GroupTypeTuple findByUuidOrKey(String uuid, String groupUuid, String typeUuid, boolean exceptionIfNull) throws GrouperDAOException;

  /**
   * update in db
   * @param groupTypeTuple
   * @throws GrouperDAOException
   */
  void update(GroupTypeTuple groupTypeTuple) throws  GrouperDAOException;

  /**
   * save in db
   * @param groupTypeTuple
   * @throws GrouperDAOException
   */
  void save(GroupTypeTuple groupTypeTuple) throws  GrouperDAOException;

  /**
   * delete in db
   * @param groupTypeTuple
   * @throws GrouperDAOException
   */
  void delete(GroupTypeTuple groupTypeTuple) throws  GrouperDAOException;

  /**
   * save the update properties which are auto saved when business method is called
   * @param groupTypeTuple
   */
  public void saveUpdateProperties(GroupTypeTuple groupTypeTuple);

} 

