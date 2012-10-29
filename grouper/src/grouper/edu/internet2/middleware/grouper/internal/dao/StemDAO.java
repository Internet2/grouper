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
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.stem.StemSet;
import edu.internet2.middleware.subject.Subject;

/** 
 * Basic <code>Stem</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: StemDAO.java,v 1.24 2009-11-17 02:52:29 mchyzer Exp $
 * @since   1.2.0
 */
public interface StemDAO extends GrouperDAO {

  /**
   * find all stems by name
   * @param names 
   * @param exceptionOnNotFound 
   * @return the stems
   * @throws StemNotFoundException 
   */
  public Set<Stem> findByNames(Collection<String> names, boolean exceptionOnNotFound)
      throws StemNotFoundException;

  /**
   * find all parent stems of groups (and grandparents, etc)
   * @param groups
   * @return the stems 
   */
  public Set<Stem> findParentsByGroups(Collection<Group> groups);

  
  /**
   */
  void createChildAttributeDef(Stem _parent, AttributeDef _child)
    throws  GrouperDAOException;

  /**
   */
  void createChildAttributeDefName(Stem _parent, AttributeDefName _child)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void createChildGroup(Stem _parent, Group _child, Member _m, Map<String, String> attributes)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void createChildStem(Stem _child)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void createRootStem(Stem _root)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void delete(Stem _ns)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean exists(String uuid) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Stem> findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Stem> findAllByApproximateDisplayExtension(String val, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Stem> findAllByApproximateDisplayName(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Stem> findAllByApproximateDisplayName(String val, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Stem> findAllByApproximateExtension(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Stem> findAllByApproximateExtension(String val, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Stem> findAllByApproximateName(String val) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Stem> findAllByApproximateName(String val, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Stem> findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Stem> findAllByApproximateNameAny(String name, String scope) 
    throws  GrouperDAOException;

  /**
   * @param name 
   * @param scope 
   * @param queryOptions 
   * @return set stems
   * @throws GrouperDAOException 
   * @since   2.1.0
   */
  public Set<Stem> findAllByApproximateNameAny(String name, String scope, QueryOptions queryOptions);

  /**
   * @since   1.2.0
   */
  Set<Stem> findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Stem> findAllByCreatedAfter(Date d, String scope) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  Set<Stem> findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException;

  /**
   * @since   1.4.0
   */
  Set<Stem> findAllByCreatedBefore(Date d, String scope) 
    throws  GrouperDAOException;

  /**
   * Find all child groups within specified scope.
   * @since   1.2.1
   */
  Set<Group> findAllChildGroups(Stem ns, Stem.Scope scope)
    throws  GrouperDAOException;

  /**
   * Find all child groups within specified scope, and make sure the
   * grouper session can see them
   * @param ns 
   * @param scope 
   * @param grouperSession 
   * @param subject 
   * @param queryOptions 
   * @param inPrivSet is a set of privs that the subject must have one of to display a row.  AccessPrivilege
   * has some pre-baked set constants available
   * @return the groups
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  Set<Group> findAllChildGroupsSecure(
      Stem ns, Stem.Scope scope, GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * Find all child groups within specified scope, and make sure the
   * grouper session can see them
   * @param ns 
   * @param scope 
   * @param grouperSession 
   * @param subject 
   * @param queryOptions 
   * @param inPrivSet is a set of privs that the subject must have one of to display a row.  AccessPrivilege
   * has some pre-baked set constants available
   * @param typeOfGroups type of groups to return or null for all
   * @return the groups
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  Set<Group> findAllChildGroupsSecure(
      Stem ns, Stem.Scope scope, GrouperSession grouperSession, Subject subject, 
      Set<Privilege> inPrivSet, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups)
    throws  GrouperDAOException;

  /**
   * Find all child groups within specified scope, and make sure the
   * grouper session can see them
   * @param ns 
   * @param scope 
   * @param grouperSession 
   * @param subject 
   * @param queryOptions 
   * @param inPrivSet is a set of privs that the subject must have one of to display a row.  AccessPrivilege
   * has some pre-baked set constants available
   * @return the groups
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  Set<Group> findAllChildMembershipGroupsSecure(
      Stem ns, Stem.Scope scope, GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * Find all child stems within specified scope, and make sure the
   * grouper session can see them
   * @param ns 
   * @param scope 
   * @param grouperSession 
   * @param subject 
   * @param queryOptions 
   * @param inPrivSet is a set of privs that the subject must have one of to display a row.  NamingPrivilege
   * has some pre-baked set constants available
   * @return the groups
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  Set<Stem> findAllChildStemsSecure(
      Stem ns, Stem.Scope scope, GrouperSession grouperSession, Subject subject, 
      Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * @since   2.2
   *
   */
  Stem findByIdIndex(Long idIndex, boolean exceptionIfNotFound) 
    throws StemNotFoundException;

  /**
   * Find all child stems within specified scope.
   * @since   1.2.1
   */
  Set<Stem> findAllChildStems(Stem ns, Stem.Scope scope)
    throws  GrouperDAOException;
  
  /**
   * Find all child stems within specified scope.
   * @since   2.1
   */
  Set<Stem> findAllChildStems(Stem ns, Stem.Scope scope, QueryOptions queryOptions)
    throws  GrouperDAOException;
  
  /**
   * Find all child stems within specified scope.
   * @param ns 
   * @param scope 
   * @param orderByName 
   * @return set of stems
   * @throws GrouperDAOException 
   */
  Set<Stem> findAllChildStems(Stem ns, Stem.Scope scope, boolean orderByName)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   * @deprecated
   */
  @Deprecated
  Stem findByName(String name) throws GrouperDAOException, StemNotFoundException;

  /**
   * @since   1.2.0
   * @deprecated
   */
  @Deprecated
  Stem findByUuid(String uuid) throws GrouperDAOException, StemNotFoundException;

  /**
   * @since   1.2.0
   */
  Stem findByName(String name, boolean exceptionIfNull, QueryOptions queryOptions) throws GrouperDAOException, StemNotFoundException;

  /**
   * @since   1.2.0
   */
  Stem findByUuid(String uuid, boolean exceptionIfNull, QueryOptions queryOptions) throws GrouperDAOException, StemNotFoundException;

  /**
   * note, dont pass more than 100 ids
   * @since   2.1
   */
  Set<Stem> findByUuids(Collection<String> uuids, QueryOptions queryOptions);

  /**
   * @since   1.2.0
   */
  Stem findByName(String name, boolean exceptionIfNull) throws GrouperDAOException, StemNotFoundException;

  /**
   * Find a stem by its alternate name only.
   * @param name
   * @param exceptionIfNotFound
   * @param queryOptions 
   * @return stem
   * @throws GrouperDAOException
   * @throws StemNotFoundException
   */
  Stem findByAlternateName(String name, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws GrouperDAOException, StemNotFoundException;
  
  /**
   * Find a stem by its current name only. 
   * @param name
   * @param exceptionIfNotFound
   * @param queryOptions 
   * @return stem
   * @throws GrouperDAOException
   * @throws StemNotFoundException
   */
  Stem findByCurrentName(String name, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws GrouperDAOException, StemNotFoundException;
  
  /**
   * @param uuid 
   * @param name 
   * @param exceptionIfNull 
   * @return the stem or null
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   * @since   1.6.0
   */
  Stem findByUuidOrName(String uuid, String name, boolean exceptionIfNull) throws GrouperDAOException, StemNotFoundException;

  /**
   * @param uuid 
   * @param name 
   * @param exceptionIfNull 
   * @param queryOptions
   * @return the stem or null
   * @throws GrouperDAOException 
   * @throws StemNotFoundException 
   * @since   1.6.0
   */
  Stem findByUuidOrName(String uuid, String name, boolean exceptionIfNull, QueryOptions queryOptions) throws GrouperDAOException, StemNotFoundException;

  /**
   * @since   1.2.0
   */
  Stem findByUuid(String uuid, boolean exceptionIfNull) throws GrouperDAOException, StemNotFoundException;

  /**
   * @since   1.3.1
   */
  Set<Stem> getAllStems()
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void renameStemAndChildren(Set children)
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  void update(Stem _ns)
    throws  GrouperDAOException;

  /**
   * find stems by creator or modifier
   * @param member
   * @return the groups
   */
  Set<Stem> findByCreatorOrModifier(Member member);

  /**
   * get immediate children (stems) secure
   * @param grouperSession
   * @param stem
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @return the set of groups
   * @throws GrouperDAOException
   */
  Set<Stem> getImmediateChildrenSecure(GrouperSession grouperSession, 
      Stem stem, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * 
   * @param grouperSession
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in NamingPrivilege of pre-canned sets
   * @return stems
   * @throws GrouperDAOException
   */
  Set<Stem> getAllStemsSecure(GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * 
   * @param scope is blank for no scope
   * @param grouperSession
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in NamingPrivilege of pre-canned sets
   * @return the stems
   * @throws GrouperDAOException
   */
  Set<Stem> getAllStemsSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;
  
  /**
   * get all stems secure, split the scope by whitespace
   * @param scope
   * @param grouperSession
   * @param subject
   * @param privileges
   * @param queryOptions
   * @return set of attribute defs
   */
  public Set<Stem> getAllStemsSplitScopeSecure(String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions);

  /**
   * @param stemId
   */
  public void updateLastMembershipChange(String stemId);
  

  /**
   * This will update last_membership_change for stem owners where group member in groupSet is the given groupId.
   * @param groupId
   */
  public void updateLastMembershipChangeIncludeAncestorGroups(String groupId);
  
  /**
   * save the udpate properties which are auto saved when business method is called
   * @param stem
   */
  public void saveUpdateProperties(Stem stem);

  /**
   * get stems with groups that have privileges
   * @param grouperSession
   * @param subject
   * @param queryOptions
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @return stems
   * @throws GrouperDAOException
   */
  Set<Stem> getAllStemsWithGroupsSecure(GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException;

  /**
   * see which stems do not have this privilege
   * @param grouperSession
   * @param stemId
   * @param scope
   * @param subject
   * @param privilege
   * @param queryOptions
   * @param considerAllSubject
   * @param sqlLikeString
   * @return the stems
   */
  public Set<Stem> findStemsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, 
      QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString);

  /**
   * @param newParentSets
   * @param oldStemSets
   * @param currentStemId
   */
  public void moveStemSets(Collection<StemSet> newParentSets, Collection<StemSet> oldStemSets, String currentStemId);

} 

