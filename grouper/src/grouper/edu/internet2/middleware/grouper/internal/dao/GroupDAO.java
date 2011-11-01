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
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;

/** 
 * Basic <code>Group</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: GroupDAO.java,v 1.30 2009-12-10 08:54:15 mchyzer Exp $
 * @since   1.2.0
 */
public interface GroupDAO extends GrouperDAO {

  /**
   * find al types for a group
   * @param uuid
   * @return the types
   */
  public Set<GroupType> _findAllTypesByGroup(final String uuid);
  
  /**
   * put in cache
   * @param uuid
   * @param exists
   */
  public void putInExistsCache(String uuid, boolean exists);

  /**
   * @since   1.2.0
   */
  GroupTypeTuple addType(Group _g, GroupType _gt) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(Group _g)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  GroupTypeTuple deleteType(Group _g, GroupType _gt) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean exists(String uuid)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByAnyApproximateAttr(String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByAnyApproximateAttr(String val, String scope) 
    throws  GrouperDAOException,
            IllegalStateException
            ;


  /**
   * @since   1.2.0
   */
  Set<Group> findAllByApproximateAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByApproximateAttr(String attr, String val, String scope) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @param attr attribute name
   * @param val value
   * @param scope some folder or null for all
   * @return  the grops
   * @throws GrouperDAOException 
   * @throws IllegalStateException 
   * @since   2.0.2
   * 
   */
  Set<Group> findAllByApproximateAttrSecure(String attr, String val, String scope) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.3
   */
  Set<Group> findAllByAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
            ;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByAttr(String attr, String val, String scope) 
    throws  GrouperDAOException,
            IllegalStateException
            ;


  /**
   * @since   1.2.0
   */
  Set<Group> findAllByApproximateName(String name) 
    throws  GrouperDAOException
            ;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByApproximateName(String name, String scope) 
    throws  GrouperDAOException
            ;


  /**
   * @since   1.2.0
   */
  Set<Group> findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByCreatedAfter(Date d, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByCreatedBefore(Date d, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByModifiedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByModifiedAfter(Date d, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Group> findAllByModifiedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByModifiedBefore(Date d, String scope) 
    throws  GrouperDAOException;
  
  /**
   * @since   1.5.0
   */
  Set<Group> findAllByLastMembershipAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.5.0
   */
  Set<Group> findAllByLastMembershipAfter(Date d, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.5.0
   */
  Set<Group> findAllByLastMembershipBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.5.0
   */
  Set<Group> findAllByLastMembershipBefore(Date d, String scope) 
    throws  GrouperDAOException;

  /**
   * note, this doesnt cache
   * @since   1.2.0
   */
  Set<Group> findAllByType(GroupType _gt)
    throws  GrouperDAOException;

  /**
   * this caches
   * @since   1.2.0
   */
  Set<Group> findAllByType(GroupType _gt, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Group> findAllByType(GroupType _gt, String scope)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   * @deprecated use overload
   */
  @Deprecated
  Group findByAttribute(String attr, String val) 
    throws  GrouperDAOException, GroupNotFoundException;

  /**
   * @since   1.2.0
   */
  Group findByAttribute(String attr, String val, boolean exceptionIfNotFound) 
    throws  GrouperDAOException, GroupNotFoundException;

  /**
   * @since   1.2.0
   * @deprecated use overload
   */
  @Deprecated
  Group findByName(String name) 
    throws GrouperDAOException, GroupNotFoundException;

  /**
   * @since   1.2.0
   *
   */
  Group findByName(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, GroupNotFoundException;

  /**
   * @since   1.2.0
   */
  Group findByName(String name, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws  GrouperDAOException,
            GroupNotFoundException
            ;
  /**
   * @since   1.2.0
   * @deprecated use overload
   */
  @Deprecated
  Group findByUuid(String uuid) 
    throws  GrouperDAOException, GroupNotFoundException;

  /**
   * 
   */
  Group findByUuid(String uuid, boolean exceptionIfNotFound) 
    throws  GrouperDAOException, GroupNotFoundException;

  /**
   * 
   * @param uuid
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return the group or null or exception
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   */
  Group findByUuid(String uuid, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws  GrouperDAOException, GroupNotFoundException;

  /**
   * find all groups which have these uuids
   * @param uuids
   * @param exceptionOnNotFound if exception should be thrown when a uuid doesnt match up
   * @return the groups
   * @throws GroupNotFoundException 
   */
  Set<Group> findByUuids(Collection<String> uuids, boolean exceptionOnNotFound) throws GroupNotFoundException;
  
  /**
   * @since   1.3.1
   */
  Set<Group> getAllGroups()
    throws  GrouperDAOException;

  /**
   * 
   * @param grouperSession
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @return groups
   * @throws GrouperDAOException
   */
  Set<Group> getAllGroupsSecure(GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * 
   * @param scope
   * @param grouperSession
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @return the groups
   * @throws GrouperDAOException
   */
  Set<Group> getAllGroupsSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;


  /**
   * get immediate children secure
   * @param grouperSession
   * @param stem
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @return the set of groups
   * @throws GrouperDAOException
   */
  Set<Group> getImmediateChildrenSecure(GrouperSession grouperSession, 
      Stem stem, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * get immediate children secure
   * @param grouperSession
   * @param stem
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @param enabledOnly 
   * @return the set of groups
   * @throws GrouperDAOException
   */
  Set<Group> getImmediateChildrenMembershipSecure(GrouperSession grouperSession, 
      Stem stem, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions, boolean enabledOnly)
    throws  GrouperDAOException;


  /**
   * @since   1.4.0
   */
  Set<Group> getAllGroups(String scope)
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Group> getImmediateChildren(Stem stem)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void update(Group _g)
    throws  GrouperDAOException;

  /**
   * find groups by creator or modifier
   * @param member
   * @return the groups
   */
  Set<Group> findByCreatorOrModifier(Member member);
  
  /**
   * @param name 
   * @param scope 
   * @param queryOptions 
   * @param typeOfGroups 
   * @return the set of groups
   * @throws GrouperDAOException 
   * @since   2.1.0
   */
  Set<Group> findAllByApproximateNameSecure(String name, String scope, 
      QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) 
    throws  GrouperDAOException;

  /**
   * Find a group by its alternate name only.
   * @param name
   * @param exceptionIfNotFound
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   */
  Group findByAlternateName(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, GroupNotFoundException;
  
  /**
   * Find a group by its current name only.
   * @param name
   * @param exceptionIfNotFound
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   */
  Group findByCurrentName(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, GroupNotFoundException;
  

  /**
   * Find groups using an approximate string for the current name,
   * display name, extension, display extension.
   * @param name
   * @return set
   * @throws GrouperDAOException
   */
  Set<Group> findAllByApproximateCurrentName(String name) throws GrouperDAOException;

  /**
   * Find groups using an approximate string for the current name,
   * display name, extension, display extension.
   * @param name
   * @param scope
   * @return set
   * @throws GrouperDAOException
   */
  Set<Group> findAllByApproximateCurrentName(String name, String scope)
      throws GrouperDAOException;
  
  /**
   * Find groups using an approximate string for the alternate name.
   * @param name
   * @return set
   * @throws GrouperDAOException
   */
  Set<Group> findAllByApproximateAlternateName(String name) throws GrouperDAOException;

  /**
   * Find groups using an approximate string for the alternate name.
   * @param name
   * @param scope
   * @return set
   * @throws GrouperDAOException
   */
  Set<Group> findAllByApproximateAlternateName(String name, String scope)
      throws GrouperDAOException;

  /**
   * 
   * @param grouperSession
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @param enabledOnly 
   * @return groups
   * @throws GrouperDAOException
   */
  Set<Group> getAllGroupsMembershipSecure(GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions, boolean enabledOnly)
    throws  GrouperDAOException;

  /**
   * 
   * @param scope
   * @param grouperSession
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @param enabledOnly 
   * @return the groups
   * @throws GrouperDAOException
   */
  Set<Group> getAllGroupsMembershipSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions, boolean enabledOnly)
    throws  GrouperDAOException;
  
  /**
   * 
   * @param scope
   * @param stemScope
   * @param grouperSession
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @param enabledOnly 
   * @param stem 
   * @return the groups
   * @throws GrouperDAOException
   */
  Set<Group> getAllGroupsMembershipSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions, boolean enabledOnly, Stem stem, Scope stemScope)
    throws  GrouperDAOException;
  
  /**
   * @param field
   * @param scope
   * @param grouperSession
   * @param subject
   * @param queryOptions if sorting on name, displayName, extension, displayExtension
   * @param enabled null for all, True for enabledOnly, False for disabledOnly
   * @param membershipType immediate, effective, etc
   * @param stem if searching in a specific stem
   * @param stemScope sub or one
   * @return the groups
   * @throws GrouperDAOException
   */
  Set<Group> getAllGroupsMembershipSecure(Field field, String scope, GrouperSession grouperSession, 
      Subject subject, QueryOptions queryOptions, Boolean enabled, 
      MembershipType membershipType, Stem stem, Scope stemScope)
    throws  GrouperDAOException;
  
  /**
   * @param groupId
   */
  public void updateLastMembershipChange(String groupId);
  
  /**
   * @param groupId
   */
  public void updateLastImmediateMembershipChange(String groupId);
  
  /**
   * This will update last_membership_change for group owners where group member in groupSet is the given groupId.
   * @param groupId
   */
  public void updateLastMembershipChangeIncludeAncestorGroups(String groupId);
  
  /**
   * @param uuid 
   * @param name 
   * @param exceptionIfNull 
   * @return the stem or null
   * @throws GrouperDAOException 
   * @throws GroupNotFoundException 
   * @since   1.6.0
   */
  Group findByUuidOrName(String uuid, String name, boolean exceptionIfNull) throws GrouperDAOException, GroupNotFoundException;

  /**
   * save the update properties which are auto saved when business method is called
   * @param group
   */
  public void saveUpdateProperties(Group group);

  /**
   * see which groups do not have this privilege
   * @param grouperSession
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param queryOptions
   * @param considerAllSubject
   * @param sqlLikeString
   * @return the groups
   */
  public Set<Group> findGroupsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, 
      QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString);

  /**
   * get all groups secure, split the scope by whitespace
   * @param scope
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @param typeOfGroup or null for all
   * @return set of group
   */
  public Set<Group> getAllGroupsSplitScopeSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions, TypeOfGroup typeOfGroup);
  

} 

