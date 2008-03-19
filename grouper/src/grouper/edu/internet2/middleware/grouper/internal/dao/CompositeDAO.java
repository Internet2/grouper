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
import  edu.internet2.middleware.grouper.CompositeNotFoundException;
import  edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  java.util.Set;

/** 
 * Basic <code>Composite</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: CompositeDAO.java,v 1.4.4.1 2008-03-19 18:46:11 mchyzer Exp $
 * @since   1.2.0
 */
public interface CompositeDAO extends GrouperDAO {

  /**
   * hibernate version, int for each insert/update, negative is new
   * @return hibernate version
   */
  long getHibernateVersion();
  
  /**
   * hibernate version, int for each insert/update, negative is new
   * @param theHibernateVersion
   */
  CompositeDAO setHibernateVersion(long theHibernateVersion);
  
  /**
   * @since   1.2.0
   */
  Set findAsFactor(GroupDTO _g)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  CompositeDTO findAsOwner(GroupDTO _g) 
    throws  CompositeNotFoundException,
            GrouperDAOException
            ;

  /**
   * @since   1.2.0
   */
  CompositeDTO findByUuid(String uuid) 
    throws  CompositeNotFoundException,
            GrouperDAOException
            ;

  /**
   * @since   1.2.0
   */
  long getCreateTime();

  /**
   * @since   1.2.0
   */
  String getCreatorUuid();

  /**
   * @since   1.2.0
   */
  String getFactorOwnerUuid();

  /**
   * @since   1.2.0
   */
  String getLeftFactorUuid();

  /**
   * @since   1.2.0
   */
  String getRightFactorUuid();

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
  CompositeDAO setCreateTime(long createTime);

  /**
   * @since   1.2.0
   */
  CompositeDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  CompositeDAO setFactorOwnerUuid(String factorOwnerUUID);

  /**
   * @since   1.2.0
   */
  CompositeDAO setLeftFactorUuid(String leftFactorUUID);

  /**
   * @since   1.2.0
   */
  CompositeDAO setRightFactorUuid(String rightFactorUUID);

  /**
   * @since   1.2.0
   */
  CompositeDAO setType(String type);

  /**
   * @since   1.2.0
   */
  CompositeDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  void update(Set toAdd, Set toDelete, Set modGroups, Set modStems) 
    throws  GrouperDAOException;

} 

