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
import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;

/** 
 * Basic <code>Membership</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: MembershipDAO.java,v 1.26 2009-08-18 23:11:38 shilen Exp $
 * @since   1.2.0
 */
public interface MembershipDAO extends GrouperDAO {

  /**
   * get all memberships
   * @param enabledOnly 
   * @return set
   */
  public Set<Membership> findAll(boolean enabledOnly);
  
  
  /**
   * @param d 
   * @param f 
   * @param enabledOnly 
   * @return set of membership
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByCreatedAfter(Date d, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param d 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByCreatedBefore(Date d, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByMember(String memberUUID, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param viaGroupId 
   * @param enabledOnly 
   * @return  set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByMemberAndViaGroup(String memberUUID, String viaGroupId, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByGroupOwnerAndField(String ownerGroupId, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;
  
  /**
   * @param ownerStemId 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByStemOwnerAndField(String ownerStemId, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param f 
   * @param type 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByGroupOwnerAndFieldAndType(String ownerGroupId, Field f, String type, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerStemId 
   * @param f 
   * @param type 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByStemOwnerAndFieldAndType(String ownerStemId, Field f, String type, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByGroupOwnerAndMemberAndField(String ownerGroupId, String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;
  
  /**
TODO update for 1.5
   * @param ownerUUID 
   * @param f 
   * @param members 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  Set<Membership> findAllByGroupOwnerAndFieldAndMembers(String ownerUUID, Field f, 
      Collection<Member> members, boolean enabledOnly) 
    throws  GrouperDAOException;
  
  /**
   * @param ownerUUID 
   * @param f 
   * @param members 
   * @param type 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   */
  Set<Membership> findAllByGroupOwnerAndFieldAndMembersAndType(String ownerUUID, Field f, 
      Collection<Member> members, String type, boolean enabledOnly) 
    throws  GrouperDAOException;
  
  /**
   * @param ownerUUID 
   * @param members 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   */
  Set<Membership> findAllByGroupOwnerAndCompositeAndMembers(String ownerUUID, 
      Collection<Member> members, boolean enabledOnly) 
    throws  GrouperDAOException;
  
  /**
   * @param groupOwnerId 
   * @param memberUUID 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  Set<Membership> findAllByGroupOwnerAndMember(String groupOwnerId, String memberUUID, boolean enabledOnly) 
    throws  GrouperDAOException;


  /**
   * @param ownerGroupId
   * @param f
   * @param type
   * @param queryOptions 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByGroupOwnerAndFieldAndType(String ownerGroupId,
      Field f, String type, QueryOptions queryOptions, boolean enabledOnly) throws GrouperDAOException;
  
  /**
   * @param ownerStemId
   * @param f
   * @param type
   * @param queryOptions 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByStemOwnerAndFieldAndType(String ownerStemId,
      Field f, String type, QueryOptions queryOptions, boolean enabledOnly) throws GrouperDAOException;


  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByStemOwnerAndMemberAndField(String ownerStemId, String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param f 
   * @param enabledOnly 
   * @return  Members from memberships.
   * @throws  GrouperDAOException if any DAO errors occur.
   * @see     MembershipDAO#findAllMembersByGroupOwnerAndField(String, Field)
   * @since   1.2.1
   */
  Set<Member> findAllMembersByGroupOwnerAndField(String ownerGroupId, Field f, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * 
   * @param groupOwnerId
   * @param f
   * @param queryOptions
   * @param enabledOnly 
   * @return the members
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByGroupOwnerAndField(String groupOwnerId, Field f, QueryOptions queryOptions, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param type 
   * @param exceptionIfNull
   * @param enabledOnly 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   */
  Membership findByGroupOwnerAndMemberAndFieldAndType(String ownerGroupId, String memberUUID, Field f, String type, boolean exceptionIfNull, boolean enabledOnly)
    throws  GrouperDAOException,
            MembershipNotFoundException
            ;            

  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @param type 
   * @param exceptionIfNull
   * @param enabledOnly 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   */
  Membership findByStemOwnerAndMemberAndFieldAndType(String ownerStemId, String memberUUID, Field f, String type, boolean exceptionIfNull, boolean enabledOnly)
    throws  GrouperDAOException,
            MembershipNotFoundException
            ;            

  /**
   * @param _ms 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllChildMemberships(Membership _ms, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param _ms 
   * @return membership
   * @throws GrouperDAOException 
   * @since   1.5.0
   */
  Membership findParentMembership(Membership _ms) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param viaGroupId 
   * @param depth 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByGroupOwner(String ownerGroupId, String memberUUID, Field f, String viaGroupId, int depth, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @param viaGroupId 
   * @param depth 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByStemOwner(String ownerStemId, String memberUUID, Field f, String viaGroupId, int depth, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByMemberAndField(String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByGroupOwnerAndMemberAndField(String ownerGroupId, String memberUUID, Field f, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllImmediateByMemberAndField(String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param fieldType
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllImmediateByMemberAndFieldType(String memberUUID, String fieldType, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  Set findAllImmediateByMember(String memberUUID, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerUUID 
   * @param enabledOnly 
   * @return list
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  List<Membership> findAllByGroupOwnerAsList(String ownerUUID, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * @param ownerUUID 
   * @param enabledOnly 
   * @return list
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  List<Membership> findAllByStemOwnerAsList(String ownerUUID, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * @param uuid 
   * @param exceptionIfNull 
   * @param enabledOnly 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   */
  Membership findByUuid(String uuid, boolean exceptionIfNull, boolean enabledOnly) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
            ;

  /**
   * @param memberUUID 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findMembershipsByMemberAndField(String memberUUID, Field f, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * find memberships that the user is allowed to see
   * @param grouperSession
   * @param memberUUID
   * @param f
   * @param enabledOnly 
   * @return the memberships
   * @throws GrouperDAOException
   */
  Set<Membership> findMembershipsByMemberAndFieldSecure(GrouperSession grouperSession, String memberUUID, Field f, boolean enabledOnly)
    throws  GrouperDAOException;
  
  /**
   * Save a membership
   * @param ms
   */
  public void save(Membership ms);

  /**
   * Save a set of memberships
   * @param mships
   */
  public void save(Set<Membership> mships);
  
  /**
   * Delete a membership
   * @param ms
   */
  public void delete(Membership ms);

  /**
   * Update a membership
   * @param ms
   */
  public void update(Membership ms);
  
  /**
   * Delete a set of memberships
   * @param mships
   */
  public void delete(Set<Membership> mships);
  
  /**
   * find all memberships that have this member or have this creator
   * @param member
   * @param enabledOnly 
   * @return the memberships
   */
  Set<Membership> findAllByCreatorOrMember(Member member, boolean enabledOnly);
  

  /**
   * @param ownerGroupId
   * @param f
   * @param depth
   * @param enabledOnly
   * @return set
   */
  public Set<Membership> findAllByGroupOwnerAndFieldAndDepth(String ownerGroupId, Field f, int depth, boolean enabledOnly);
} 

