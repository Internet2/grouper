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
import  edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.Member;
import  edu.internet2.middleware.grouper.MembershipNotFoundException;
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  java.util.Date;
import  java.util.List;
import  java.util.Set;

/** 
 * Basic <code>Membership</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: MembershipDAO.java,v 1.9.6.2 2008-08-23 18:48:46 shilen Exp $
 * @since   1.2.0
 */
public interface MembershipDAO extends GrouperDAO {

  /**
   * @since   1.2.0 
   */
  boolean exists(String ownerUUID, String memberUUID, String listName, String msType)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByCreatedBefore(Date d, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByMember(String memberUUID) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByMemberAndVia(String memberUUID, String viaUUID) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByOwnerAndField(String ownerUUID, Field f) 
    throws  GrouperDAOException;
  
  /**
   * @since   1.2.1
   */
  Set findAllByOwnerAndMember(String ownerUUID, String memberUUID) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByOwnerAndFieldAndType(String ownerUUID, Field f, String type) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @return  Members from memberships.
   * @throws  GrouperDAOException if any DAO errors occur.
   * @see     MembershipDAO#findAllMembersByOwnerAndField(String, Field)
   * @since   1.2.1
   */
  Set<MemberDTO> findAllMembersByOwnerAndField(String ownerUUID, Field f)
    throws  GrouperDAOException;
    
  /**
   * @since   1.2.0
   */
  MembershipDTO findByOwnerAndMemberAndFieldAndType(String ownerUUID, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException
            ;            

  /**
   * @since   1.2.0
   */
  Set findAllChildMemberships(MembershipDTO _ms) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllEffective(String ownerUUID, String memberUUID, Field f, String viaUUID, int depth) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllEffectiveByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllEffectiveByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set findAllImmediateByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @since   1.3.1
   */
  Set findAllImmediateByMember(String memberUUID) 
    throws  GrouperDAOException;

  /**
   * @since   1.3.1
   */
  List<MembershipDTO> findAllByOwner(String ownerUUID)
    throws  GrouperDAOException;

  /**
   * @since   1.3.1
   */
  List<MembershipDTO> findAllMembershipsWithInvalidOwners()
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  MembershipDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
            ;

  /**
   * @since   1.2.0
   */
  Set findMembershipsByMemberAndField(String memberUUID, Field f)
    throws  GrouperDAOException;

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
  int getDepth();

  /**
   * @since   1.2.0
   */
  String getId();

  /**
   * @since   1.2.0
   */
  String getListName();

  /**
   * @since   1.2.0
   */
  String getListType();

  /**
   * @since   1.2.0
   */
  String getMemberUuid();
  
  /**
   * @since   1.3.0
   */
  MemberDAO getMemberDAO();

  /**
   * @since   1.2.0
   */
  String getOwnerUuid();

  /**
   * @since   1.2.0
   */
  String getParentUuid();

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
  String getViaUuid();

  /**
   * @since   1.2.0
   */
  MembershipDAO setCreateTime(long createTime);

  /**
   * @since   1.2.0
   */
  MembershipDAO setCreatorUuid(String creatorUUID);

  /**
   * @since   1.2.0
   */
  MembershipDAO setDepth(int depth);

  /**
   * @since   1.2.0
   */
  MembershipDAO setId(String id);

  /**
   * @since   1.2.0
   */
  MembershipDAO setListName(String listName);

  /**
   * @since   1.2.0
   */
  MembershipDAO setListType(String listType);

  /**
   * @since   1.2.0
   */
  MembershipDAO setMemberUuid(String memberUUID);
  
  /**
   * @since   1.3.0
   */
  MembershipDAO setMemberDAO(MemberDAO memberDAO);

  /**
   * @since   1.2.0
   */
  MembershipDAO setOwnerUuid(String ownerUUID);

  /**
   * @since   1.2.0
   */
  MembershipDAO setParentUuid(String parentUUID);

  /**
   * @since   1.2.0
   */
  MembershipDAO setType(String type);

  /**
   * @since   1.2.0
   */
  MembershipDAO setUuid(String uuid);

  /**
   * @since   1.2.0
   */
  MembershipDAO setViaUuid(String viaUUID);

  /**
   * @since   1.2.0
   */
  void update(DefaultMemberOf mof) 
    throws  GrouperDAOException;

} 

