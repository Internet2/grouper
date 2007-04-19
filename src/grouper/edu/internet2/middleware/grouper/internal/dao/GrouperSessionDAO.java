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
import  edu.internet2.middleware.grouper.internal.dto.GrouperSessionDTO;

/** 
 * Basic <code>GrouperSession</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: GrouperSessionDAO.java,v 1.4 2007-04-19 19:33:42 blair Exp $
 * @since   1.2.0
 */
public interface GrouperSessionDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  String create(GrouperSessionDTO _s)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(GrouperSessionDTO _s)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  String getId();

  /**
   * @since   1.2.0
   */
  String getMemberUuid();

  /**
   * @since   1.2.0
   */
  long getStartTime();

  /**
   * @since   1.2.0
   */
  String getUuid();

  /**
   * @since   1.2.0
   */
  GrouperSessionDAO setId(String id);

  /**
   * @since   1.2.0
   */
  GrouperSessionDAO setMemberUuid(String memberUuid);

  /**
   * @since   1.2.0
   */
  GrouperSessionDAO setStartTime(long startTime);

  /**
   * @since   1.2.0
   */
  GrouperSessionDAO setUuid(String uuid);

} 

