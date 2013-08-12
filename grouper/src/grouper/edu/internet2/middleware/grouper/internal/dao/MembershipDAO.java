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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.member.SearchStringEnum;
import edu.internet2.middleware.grouper.member.SortStringEnum;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.subject.Source;

/** 
 * Basic <code>Membership</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: MembershipDAO.java,v 1.35 2009-12-17 06:57:57 mchyzer Exp $
 * @since   1.2.0
 */
public interface MembershipDAO extends GrouperDAO {

  /**
   * find records which are disabled which shouldnt be, and enabled which shouldnt be
   * @return the memberships
   */
  public Set<Membership> findAllEnabledDisabledMismatch();
  
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
   * @param stem 
   * @param stemScope 
   * @param field
   * @param type e.g. immediate
   * @param enabledOnly 
   * @param memberId 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByStemParentOfGroupOwnerAndFieldAndType(Stem stem, Stem.Scope stemScope, 
      Field field, MembershipType type, Boolean enabledOnly, String memberId) 
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
   * @param f 
   * @param memberIds 
   * @param type 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   */
  Set<Membership> findAllByGroupOwnerAndFieldAndMemberIdsAndType(String ownerUUID, Field f, 
      Collection<String> memberIds, String type, boolean enabledOnly) 
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
   * @param ownerInGroupId
   * @param ownerNotInGroupId
   * @param typeIn
   * @param typeNotIn
   * @param queryOptions 
   * @param enabled T for enabled, F for disabled, null for all
   * @param disabledOwnerNull if true, the owner must have disabled date of null
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersInOneGroupNotOtherAndType(String ownerInGroupId,
      String ownerNotInGroupId,
      String typeIn, String typeNotIn, QueryOptions queryOptions, Boolean enabled, boolean disabledOwnerNull) throws GrouperDAOException;
  
  /**
   * find members in a group who are not members in any group under a stem
   * @param ownerInGroupId
   * @param ownerNotInStem
   * @param stemScope
   * @param typeIn
   * @param queryOptions 
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersInOneGroupNotStem(String ownerInGroupId,
      Stem ownerNotInStem, Stem.Scope stemScope,
      String typeIn, QueryOptions queryOptions);
  
  
  /**
   * @param ownerGroupId
   * @param f
   * @param type
   * @param sources 
   * @param queryOptions 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByGroupOwnerAndFieldAndType(String ownerGroupId,
      Field f, String type, Set<Source> sources, QueryOptions queryOptions, boolean enabledOnly) throws GrouperDAOException;
  

  /**
   * @param ownerId
   * @param f
   * @param type
   * @param sources
   * @param queryOptions
   * @param enabledOnly
   * @param memberSortStringEnum How to sort results or null for no sorting unless specified by queryOptions
   * @param memberSearchStringEnum Specify search string if searching for members in the group
   * @param memberSearchStringValue Search string value.
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByOwnerAndFieldAndType(String ownerId,
      Field f, String type, Set<Source> sources, QueryOptions queryOptions, boolean enabledOnly,
      SortStringEnum memberSortStringEnum, SearchStringEnum memberSearchStringEnum, String memberSearchStringValue) throws GrouperDAOException;
  
  /**
   * note, dont change this signature, Arnaud is using it
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
   * @see     MembershipDAO#findAllMembersByGroupOwnerAndField(String, Field, boolean)
   * @since   1.2.1
   */
  Set<Member> findAllMembersByGroupOwnerAndField(String ownerGroupId, Field f, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * Find all memberships based on group, field, and a range of disabled dates
   * @param ownerGroupId memberships in this owner 
   * @param field 
   * @param disabledDateFrom null if dont consider
   * @param disabledDateTo null if dont consider
   * @return memberships.
   */
  Set<Membership> findAllMembershipsByGroupOwnerFieldDisabledRange(String ownerGroupId, 
      Field field, Timestamp disabledDateFrom, Timestamp disabledDateTo);

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
   * 
   * @param groupOwnerId
   * @param f
   * @param sources
   * @param queryOptions
   * @param enabledOnly 
   * @return the members
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByGroupOwnerAndField(String groupOwnerId, Field f, Set<Source> sources, QueryOptions queryOptions, boolean enabledOnly)
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
   * @param ownerAttrDefId 
   * @param memberUUID 
   * @param f 
   * @param type 
   * @param exceptionIfNull
   * @param enabledOnly 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   */
  Membership findByAttrDefOwnerAndMemberAndFieldAndType(String ownerAttrDefId, String memberUUID, 
      Field f, String type, boolean exceptionIfNull, boolean enabledOnly)
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
   * find membershpis by group owner and other options.  
   * @param groupIds to limit memberships to
   * @param memberIds to limit memberships to
   * @param membershipIds to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @return a set of membership, group, and member objects
   */
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> groupIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled);

  /**
   * find membershpis by group owner and other options.  
   * @param groupIds to limit memberships to
   * @param memberIds to limit memberships to
   * @param membershipIds to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @return a set of membership, group, and member objects
   */
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> groupIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean shouldCheckSecurity);

  /**
   * find memberships by attribute def owner and other options.  
   * @param attributeDefId to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param fields if finding by fields, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param queryOptions for the query
   * @return a set of members and member objects
   */
  public List<Member> findAllMembersByAttributeDefOwnerOptions(String attributeDefId, 
      MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, Boolean enabled, QueryOptions queryOptions);

  /**
   * find memberships by attribute def owner and other options.  
   * @param attributeDefId to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param fields if finding by fields, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param queryOptions for the query
   * @return a set of membership and member objects
   */
  public Set<Object[]> findAllByAttributeDefOwnerOptions(String attributeDefId, 
      MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, Boolean enabled, QueryOptions queryOptions);

  /**
   * find membershpis by attribute def owner and other options.  
   * @param attributeDefId to limit memberships to
   * @param memberIds memberids to get, or blank for all
   * @param membershipType Immediate, NonImmediate, etc
   * @param fields if finding by fields, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param queryOptions for the query
   * @return a set of membership and member objects
   */
  public Set<Object[]> findAllByAttributeDefOwnerOptions(String attributeDefId, 
      Collection<String> memberIds, MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, Boolean enabled, QueryOptions queryOptions);

  /**
   * find membershpis by group owner and other options.  
   * @param groupId to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise members list is returned
   * @param enabled null for all, true for enabled only, false for disabled only
   * @return a set of sourceIds
   */
  public Set<String> findSourceIdsByGroupOwnerOptions(String groupId,
      MembershipType membershipType,
      Field field, Boolean enabled);

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
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllNonImmediateByMemberAndField(String memberUUID, Field f, boolean enabledOnly) 
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
   * @param uuid 
   * @param exceptionIfNull 
   * @param enabledOnly 
   * @param queryOptions 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   */
  Membership findByUuid(String uuid, boolean exceptionIfNull, boolean enabledOnly, QueryOptions queryOptions) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
            ;

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
   * Update a set of memberships
   * @param mships
   */
  public void update(Set<Membership> mships);
  
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
  

  /**
   * Find all missing group sets for immediate memberships where the owner is a group.
   * @return set of memberships for which a groupSet is missing
   */
  public Set<Membership> findMissingImmediateGroupSetsForGroupOwners();
  
  /**
   * Find all missing group sets for immediate memberships where the owner is a stem.
   * @return set of memberships for which a groupSet is missing
   */
  public Set<Membership> findMissingImmediateGroupSetsForStemOwners();


  /**
   * Find all missing group sets for immediate memberships where the owner is an attr def.
   * @return set of memberships for which a groupSet is missing
   */
  public Set<Membership> findMissingImmediateGroupSetsForAttrDefOwners();


  /**
   * @param ownerAttrDefId
   * @param f
   * @param type
   * @param queryOptions 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Member> findAllMembersByAttrDefOwnerAndFieldAndType(String ownerAttrDefId,
      Field f, String type, QueryOptions queryOptions, boolean enabledOnly) throws GrouperDAOException;


  /**
   * @param ownerAttrDefId 
   * @param memberUUID 
   * @param f 
   * @param viaGroupId 
   * @param depth 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllEffectiveByAttrDefOwner(String ownerAttrDefId, String memberUUID, Field f, String viaGroupId, int depth, boolean enabledOnly) 
    throws  GrouperDAOException;
  
  /**
   * @param ownerAttrDefId 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByAttrDefOwnerAndField(String ownerAttrDefId, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerAttrDefId 
   * @param memberUUID 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  Set<Membership> findAllByAttrDefOwnerAndMember(String ownerAttrDefId, String memberUUID, boolean enabledOnly) 
    throws  GrouperDAOException;


  /**
   * @param ownerAttrDefId 
   * @param f 
   * @param type 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByAttrDefOwnerAndFieldAndType(String ownerAttrDefId, Field f, String type, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param ownerAttrDefId 
   * @param memberUUID 
   * @param f 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllByAttrDefOwnerAndMemberAndField(String ownerAttrDefId, String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param attrDefId 
   * @param enabledOnly 
   * @return list
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  List<Membership> findAllByAttrDefOwnerAsList(String attrDefId, boolean enabledOnly)
    throws  GrouperDAOException;
  
  /**
   * @param attrDefId 
   * @param enabledOnly 
   * @return list
   * @throws GrouperDAOException 
   */
  List<Membership> findAllImmediateByAttrDefOwnerAsList(String attrDefId, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.3.1
   */
  Set findAllNonImmediateByMember(String memberUUID, boolean enabledOnly) 
    throws  GrouperDAOException;

  /**
   * @param memberUUID 
   * @param fieldType
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.0
   */
  Set<Membership> findAllNonImmediateByMemberAndFieldType(String memberUUID, String fieldType, boolean enabledOnly) 
    throws  GrouperDAOException;
  
  /**
   * @param uuid 
   * @param memberUUID 
   * @param fieldId 
   * @param ownerAttrDefId 
   * @param ownerGroupId 
   * @param ownerStemId 
   * @param exceptionIfNull 
   * @return the stem or null
   * @throws GrouperDAOException 
   * @throws GroupNotFoundException 
   * @since   1.6.0
   */
  Membership findByImmediateUuidOrKey(String uuid, String memberUUID, String fieldId, 
      String ownerAttrDefId, String ownerGroupId, String ownerStemId, boolean exceptionIfNull) throws GrouperDAOException;

  /**
   * @param uuid 
   * @param exceptionIfNull 
   * @return membership
   */
  public Membership findByImmediateUuid(String uuid, boolean exceptionIfNull);
  
  /**
   * @param uuid 
   * @param exceptionIfNull 
   * @param queryOptions 
   * @return membership
   */
  public Membership findByImmediateUuid(String uuid, boolean exceptionIfNull, QueryOptions queryOptions);
  
  /**
   * save the update properties which are auto saved when business method is called
   * @param membership
   */
  public void saveUpdateProperties(Membership membership);

  /**
   * find memberships by attribute def owner and other options.  
   * @param groupId to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param fields if finding by fields, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param queryOptions for the query
   * @return a set of members and member objects
   */
  public List<Member> findAllMembersByGroupOwnerOptions(String groupId, 
      MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, Boolean enabled, QueryOptions queryOptions);

  /**
   * find membershpis by group owner and other options.  
   * @param groupId to limit memberships to
   * @param memberIds memberids to get, or blank for all
   * @param membershipType Immediate, NonImmediate, etc
   * @param fields if finding by fields, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param queryOptions for the query
   * @return a set of membership and member objects
   */
  public Set<Object[]> findAllByGroupOwnerOptions(String groupId, 
      Collection<String> memberIds, MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, Boolean enabled, QueryOptions queryOptions);

  /**
   * find memberships by group owner and other options.  
   * @param groupId to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param fields if finding by fields, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param queryOptions for the query
   * @return a set of membership and member objects
   */
  public Set<Object[]> findAllByGroupOwnerOptions(String groupId, 
      MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, Boolean enabled, QueryOptions queryOptions);

  /**
   * In this case, membership objects are not joined with groupSets like most queries.  This queries the memberships table only.
   * @param ownerGroupId
   * @param f
   * @param type
   * @param enabledOnly
   * @return set of memberships
   */
  public Set<Membership> findAllMembershipEntriesByGroupOwnerAndFieldAndType(String ownerGroupId, Field f, String type, boolean enabledOnly);
  
  /**
   * @return set of arrays containing owner ids, composite ids, and member ids that should be added as composite memberships 
   */
  public Set<Object[]> findMissingComplementMemberships();
  
  /**
   * @return set of arrays containing owner ids, composite ids, and member ids that should be added as composite memberships 
   */
  public Set<Object[]> findMissingUnionMemberships();
  
  /**
   * @return set of arrays containing owner ids, composite ids, and member ids that should be added as composite memberships 
   */
  public Set<Object[]> findMissingIntersectionMemberships();
  
  /**
   * @return set of immediate memberships that are bad
   */
  public Set<Membership> findBadComplementMemberships();
  
  /**
   * @return set of immediate memberships that are bad
   */
  public Set<Membership> findBadUnionMemberships();
  
  /**
   * @return set of immediate memberships that are bad
   */
  public Set<Membership> findBadIntersectionMemberships();
  
  /**
   * This will find "immediate" memberships on composite groups 
   * along with memberships that don't have the right viaCompositeId.
   * @return set of immediate memberships that are bad
   */
  public Set<Membership> findBadMembershipsOnCompositeGroup();
  
  /**
   * @return set of immediate memberships that are bad
   */
  public Set<Membership> findBadCompositeMembershipsOnNonCompositeGroup();

  /**
   * find memberships by stem owner and other options.  
   * @param stemIds to limit memberships to
   * @param memberIds to limit memberships to
   * @param membershipIds to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @return a set of membership, stem, and member objects
   */
  public Set<Object[]> findAllByStemOwnerOptions(Collection<String> stemIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean shouldCheckSecurity);

  /**
   * find memberships by stem owner and other options.  
   * @param attributeDefIds to limit memberships to
   * @param memberIds to limit memberships to
   * @param membershipIds to limit memberships to
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @return a set of membership, stem, and member objects
   */
  public Set<Object[]> findAllByAttributeDefOwnerOptions(Collection<String> attributeDefIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, 
      Boolean shouldCheckSecurity);
} 

