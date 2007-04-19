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
import  edu.internet2.middleware.grouper.Field;
import  edu.internet2.middleware.grouper.MemberOf;
import  edu.internet2.middleware.grouper.MembershipNotFoundException;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  java.util.Date;
import  java.util.Set;

/** 
 * Basic <code>Membership</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: MembershipDAO.java,v 1.2 2007-04-19 14:31:20 blair Exp $
 * @since   1.2.0
 */
public interface MembershipDAO extends GrouperDAO {

  /**
   * TODO 20070404 expect this to change
   * <p/>
   * @since   1.2.0 
   */
  public boolean exists(String ownerUUID, String memberUUID, String listName, String msType)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedBefore(Date d, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByMember(String memberUUID) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByMemberAndVia(String memberUUID, String viaUUID) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByOwnerAndField(String ownerUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByOwnerAndFieldAndType(String ownerUUID, Field f, String type) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public MembershipDTO findByOwnerAndMemberAndFieldAndType(String ownerUUID, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException
            ;            

  /**
   * @since   1.2.0
   */
  public Set findAllChildMemberships(MembershipDTO _ms) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllEffective(String ownerUUID, String memberUUID, Field f, String viaUUID, int depth) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllEffectiveByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllEffectiveByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public Set findAllImmediateByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  public MembershipDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
            ;

  /**
   * @since   1.2.0
   */
  public Set findMembershipsByMemberAndField(String memberUUID, Field f)
    throws  GrouperDAOException;

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
  public int getDepth();

  /**
   * @since   1.2.0
   */
  public String getId();

  /**
   * @since   1.2.0
   */
  public String getListName();

  /**
   * @since   1.2.0
   */
  public String getListType();

  /**
   * @since   1.2.0
   */
  public String getMemberUuid();

  /**
   * @since   1.2.0
   */
  public String getOwnerUuid();

  /**
   * @since   1.2.0
   */
  public String getParentUuid();

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
  public String getViaUuid();

  /**
   * @since   1.2.0
   */
  public MembershipDAO setCreateTime(long createTime);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setDepth(int depth);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setId(String id);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setListName(String listName);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setListType(String listType);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setMemberUuid(String memberUUID);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setOwnerUuid(String ownerUUID);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setParentUuid(String parentUUID);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setType(String type);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  public MembershipDAO setViaUuid(String viaUUID);

  /**
   * TODO 20070404 expect this to change
   * <p/>
   * @since   1.2.0
   */
  public void update(MemberOf mof) 
    throws  GrouperDAOException;

} 

