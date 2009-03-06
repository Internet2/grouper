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
import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;

/** 
 * Basic <code>Membership</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: MembershipDAO.java,v 1.18 2009-03-06 17:48:56 shilen Exp $
 * @since   1.2.0
 */
public interface MembershipDAO extends GrouperDAO {

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param listName 
   * @param msType 
   * @return if exists
   * @throws GrouperDAOException 
   * @since   1.2.0 
   */
  boolean existsByGroupOwner(String ownerGroupId, String memberUUID, String listName, String msType)
    throws  GrouperDAOException;

  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param listName 
   * @param msType 
   * @return if exists
   * @throws GrouperDAOException 
   * @since   1.2.0 
   */
  boolean existsByStemOwner(String ownerStemId, String memberUUID, String listName, String msType)
    throws  GrouperDAOException;

  /**
   * @param d 
   * @param f 
   * @return set of membership
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException;

  /**
   * @param d 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByCreatedBefore(Date d, Field f) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByMember(String memberUUID) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param viaGroupId 
   * @return  set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByMemberAndViaGroup(String memberUUID, String viaGroupId) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByGroupOwnerAndField(String ownerGroupId, Field f) 
    throws  GrouperDAOException;
  
  /**
   * @param ownerStemId 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByStemOwnerAndField(String ownerStemId, Field f) 
    throws  GrouperDAOException;
  
  /**
   * @param groupOwnerId 
   * @param memberUUID 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  Set<Membership> findAllByGroupOwnerAndMember(String groupOwnerId, String memberUUID) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param f 
   * @param type 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByGroupOwnerAndFieldAndType(String ownerGroupId, Field f, String type) 
    throws  GrouperDAOException;

  /**
   * @param ownerStemId 
   * @param f 
   * @param type 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByStemOwnerAndFieldAndType(String ownerStemId, Field f, String type) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByGroupOwnerAndMemberAndField(String ownerGroupId, String memberUUID, Field f) 
    throws  GrouperDAOException;
  
  /**
   * @param ownerGroupId
   * @param f
   * @param type
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByGroupOwnerAndFieldAndType(String ownerGroupId,
      Field f, String type) throws GrouperDAOException;
  
  /**
   * @param ownerStemId
   * @param f
   * @param type
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByStemOwnerAndFieldAndType(String ownerStemId,
      Field f, String type) throws GrouperDAOException;


  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByStemOwnerAndMemberAndField(String ownerStemId, String memberUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param f 
   * @return  Members from memberships.
   * @throws  GrouperDAOException if any DAO errors occur.
   * @see     MembershipDAO#findAllMembersByGroupOwnerAndField(String, Field)
   * @since   1.2.1
   */
  Set<Member> findAllMembersByGroupOwnerAndField(String ownerGroupId, Field f)
    throws  GrouperDAOException;
    
  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param type 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   * @since   1.2.0
   */
  Membership findByGroupOwnerAndMemberAndFieldAndType(String ownerGroupId, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException
            ;            

  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @param type 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   * @since   1.2.0
   */
  Membership findByStemOwnerAndMemberAndFieldAndType(String ownerStemId, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException
            ;            

  /**
   * @param _ms 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllChildMemberships(Membership _ms) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param viaGroupId 
   * @param depth 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByGroupOwner(String ownerGroupId, String memberUUID, Field f, String viaGroupId, int depth) 
    throws  GrouperDAOException;

  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @param viaGroupId 
   * @param depth 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByStemOwner(String ownerStemId, String memberUUID, Field f, String viaGroupId, int depth) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByGroupOwnerAndMemberAndField(String ownerGroupId, String memberUUID, Field f)
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllImmediateByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException;
  
  /**
   * @param memberUUID 
   * @param fieldType
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllImmediateByMemberAndFieldType(String memberUUID, String fieldType) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  Set findAllImmediateByMember(String memberUUID) 
    throws  GrouperDAOException;

  /**
   * @param ownerUUID 
   * @return list
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  List<Membership> findAllByGroupOwnerAsList(String ownerUUID)
    throws  GrouperDAOException;

  /**
   * @param ownerUUID 
   * @return list
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  List<Membership> findAllByStemOwnerAsList(String ownerUUID)
    throws  GrouperDAOException;

  /**
   * @return list
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  List<Membership> findAllMembershipsWithInvalidOwners()
    throws  GrouperDAOException;

  /**
   * @param uuid 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   * @since   1.2.0
   */
  Membership findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
            ;

  /**
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set findMembershipsByMemberAndField(String memberUUID, Field f)
    throws  GrouperDAOException;

  /**
   * @param mof 
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  void update(DefaultMemberOf mof) 
    throws  GrouperDAOException;

  /**
   * find all memberships that have this member or have this creator
   * @param member
   * @return the memberships
   */
  Set<Membership> findAllByCreatorOrMember(Member member);
} 

