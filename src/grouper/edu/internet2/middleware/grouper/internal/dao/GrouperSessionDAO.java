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
import  java.util.Date;

/** 
 * <i>GrouperSession</i> DAO interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperSessionDAO.java,v 1.1 2007-04-17 14:17:29 blair Exp $
 * @since   1.2.0
 */
public interface GrouperSessionDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  public String create(GrouperSessionDTO _s)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public void delete(GrouperSessionDTO _s)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public String getMemberUuid();

  /**
   * @since   1.2.0
   */
  public Date getStartTime();

  /**
   * @since   1.2.0
   */
  public String getUuid();

  /**
   * @since   1.2.0
   */
  public GrouperSessionDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public GrouperSessionDAO setMemberUuid(String memberUuid);

  /**
   * @since   1.2.0
   */
  public GrouperSessionDAO setStartTime(Date startTime);

  /**
   * @since   1.2.0
   */
  public GrouperSessionDAO setUuid(String uuid);

} 

