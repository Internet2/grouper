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
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MemberNotUniqueException;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/** 
 * Basic <code>Member</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: MemberDAO.java,v 1.11 2009-12-28 06:08:37 mchyzer Exp $
 * @since   1.2.0
 */
public interface MemberDAO extends GrouperDAO {

  /**
   * @since   1.2.0
   */
  void create(Member _m) 
    throws  GrouperDAOException;

  /**
   * @since   1.2.0
   */
  boolean exists(String uuid) 
    throws  GrouperDAOException;

  /**
   * @since   1.3.0
   */
  Set<Member> findAll() 
    throws  GrouperDAOException;
  
  /**
   * @since   1.3.0
   */
  Set<Member> findAll(Source source) 
    throws  GrouperDAOException;
  
  /**
   * find all members that are used somewhere (e.g. memberships or attributes)
   * @param source 
   * @return the members
   * @throws GrouperDAOException 
   * @since   1.6.1
   */
  Set<Member> findAllUsed(Source source) 
    throws  GrouperDAOException;
  
  /**
   * @since   1.2.0
   * @deprecated use overload
   */
  @Deprecated
  Member findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  Member findBySubject(Subject subj, boolean exceptionIfNull)
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   * @deprecated use overload
   */
  @Deprecated
  Member findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  Member findBySubject(String id, String src, String type, boolean exceptionIfNull) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * find by subject id only (cant be duplicates)
   * @param subjectId
   * @return the member
   * @throws GrouperDAOException
   * @throws MemberNotFoundException
   * @throws MemberNotUniqueException 
   * @deprecated use overload
   */
  @Deprecated
  Member findBySubject(String subjectId) 
    throws  GrouperDAOException,
            MemberNotFoundException,
            MemberNotUniqueException;
  
  /**
   * find by subject id only (cant be duplicates)
   * @param subjectId
   * @return the member
   * @throws GrouperDAOException
   * @throws MemberNotFoundException
   * @throws MemberNotUniqueException 
   */
  Member findBySubject(String subjectId, boolean exceptionIfNull) 
    throws  GrouperDAOException,
            MemberNotFoundException,
            MemberNotUniqueException;
  
  /**
   * find by subject id and source id
   * @param subjectId
   * @param src
   * @return the member
   * @throws GrouperDAOException
   * @throws MemberNotFoundException
   */
  Member findBySubject(String subjectId, String src, boolean exceptionIfNull) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * find by subject id and source id
   * @param subjectId
   * @param src
   * @return the member
   * @throws GrouperDAOException
   * @throws MemberNotFoundException
   * @deprecated use overload
   */
  @Deprecated
  Member findBySubject(String subjectId, String src) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   * @deprecated
   */
  @Deprecated
  Member findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  Member findByUuid(String uuid, boolean exceptionIfNull) 
    throws  GrouperDAOException,
            MemberNotFoundException
            ;

  /**
   * @since   1.2.0
   */
  void update(Member _m) 
    throws  GrouperDAOException;

  /**
   * update the exists cache
   * @param uuid
   * @param exists
   */
  public void existsCachePut(String uuid, boolean exists);

  /**
   * remove from cache
   * @param uuid
   */
  public void uuid2dtoCacheRemove(String uuid);

  /**
   * find the set of member uuids of the intersection of two groups
   * @param groupUuid1
   * @param groupUuid2
   * @return the set of member uuids (non null)
   */
  public Set<String> _internal_membersIntersection(String groupUuid1, String groupUuid2);
  
  /**
   * find the set of member uuids of the union of two groups
   * @param groupUuid1
   * @param groupUuid2
   * @return the set of member uuids (non null)
   */
  public Set<String> _internal_membersUnion(String groupUuid1, String groupUuid2);
  
  /**
   * find the set of member uuids of the complement of two groups
   * @param groupUuid1
   * @param groupUuid2
   * @return the set of member uuids (non null)
   */
  public Set<String> _internal_membersComplement(String groupUuid1, String groupUuid2);

  /**
   * convert a set of subjects to a set of members
   * @param grouperSession 
   * @param subjects to convert to members
   * @param group that subjects must be in
   * @param field that they must be in in the group (null will default to eh members list
   * @param membershipType that they must be in in the group or null for any
   * @return the members in the group
   */
  public abstract Set<Member> findBySubjectsInGroup(GrouperSession grouperSession,
      Set<Subject> subjects, Group group, Field field, MembershipType membershipType);

  /**
   * find members by subjects and create if not exist possibly
   * @param subjects
   * @param createIfNotExists
   * @return the members
   */
  public Set<Member> findBySubjects(
      Collection<Subject> subjects, boolean createIfNotExists);
  
  /**
   * find a member by uuid or subject id
   * @param uuid
   * @param subjectId
   * @param source
   * @param exceptionIfNull
   * @return the member
   */
  public abstract Member findByUuidOrSubject(String uuid, String subjectId, String source, boolean exceptionIfNull);
  
  /**
   * save the udpate properties which are auto saved when business method is called
   * @param member
   */
  public void saveUpdateProperties(Member member);

} 

