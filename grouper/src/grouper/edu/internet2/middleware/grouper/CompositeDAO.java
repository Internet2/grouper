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
import  java.util.Date;
import  java.util.Set;

/** 
 * <i>Composite</i> DAO interface.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CompositeDAO.java,v 1.1 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
interface CompositeDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  public Set findAsFactor(GroupDTO _g)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public CompositeDTO findAsOwner(GroupDTO _g) 
    throws  CompositeNotFoundException,
            GrouperDAOException
            ;

  /**
   * @since   1.2.0
   */
  public CompositeDTO findByUuid(String uuid) 
    throws  CompositeNotFoundException,
            GrouperDAOException
            ;

  /**
   * @since   1.2.0
   */
  public long getCreateTime();

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid();

  /**
   * @since   1.2.0
   */
  public String getFactorOwnerUuid();

  /**
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public String getLeftFactorUuid();

  /**
   * @since   1.2.0
   */
  public String getRightFactorUuid();

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
  public CompositeDAO setCreateTime(long createTime);

  /**
   * @since   1.2.0
   */
  public CompositeDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  public CompositeDAO setFactorOwnerUuid(String factorOwnerUUID);

  /**
   * @since   1.2.0
   */
  public CompositeDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public CompositeDAO setLeftFactorUuid(String leftFactorUUID);

  /**
   * @since   1.2.0
   */
  public CompositeDAO setRightFactorUuid(String rightFactorUUID);

  /**
   * @since   1.2.0
   */
  public CompositeDAO setType(String type);

  /**
   * @since   1.2.0
   */
  public CompositeDAO setUuid(String uuid);

  /**
   * TODO 20070403 expect this to change.
   * <p/>
   * @since   1.2.0
   */
  public void update(Set toAdd, Set toDelete, Set modGroups, Set modStems) 
    throws  GrouperDAOException;

} // interface CompositeDAO extends GrouperDAO

