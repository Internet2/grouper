/**
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
 */
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.helper;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer;
import edu.internet2.middleware.subject.Subject;


/**
 * Mock {@link AccessResolver}.
 * @author  blair christensen.
 * @version $Id: MockAccessResolver.java,v 1.5 2009-08-29 15:57:59 shilen Exp $
 * @since   1.2.1
 */
public class MockAccessResolver implements AccessResolver {


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
    throw E;
  }

  /** */
  private static final GrouperException E = new GrouperException("not implemented");

  /**
   * @return  New <code>MockAccessResolver</code>.
   * @since   1.2.1
   */
  public MockAccessResolver() {
    super();
  }



  /**
   * Not implemented.
   * @param property 
   * @return  config
   * @throws IllegalArgumentException 
   * @throws  GrouperException
   * @since   1.2.1
   */
  public String getConfig(String property) 
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege, String uuid)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    throw E;
  }

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }
            

  /**
   * Not implemented.
   * @throws  GrouperException
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    throw E;
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    throw E;
  }


  /**
   * Not implemented.
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    throw E;
  }


  /**
   * Not implemented.
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    throw E;
  }            

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterGroupsWhereClause( 
      Subject subject, HqlQuery hqlQuery, StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {
    throw E;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    throw E;
  }


  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterMemberships(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Membership> postHqlFilterMemberships(Subject subject,
      Set<Membership> memberships) {
    throw E;
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject,
      Set<Privilege> privInSet) {
    throw E;
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterStemsWithGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStemsWithGroups(Set<Stem> stems, Subject subject,
      Set<Privilege> inPrivSet) {
    throw E;
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    throw E;
  }



  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getStemsWhereGroupThatSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Stem> getStemsWhereGroupThatSubjectHasPrivilege(Subject subject,
      Privilege privilege) throws IllegalArgumentException {
    throw E;
  }


  /**
   * @see AccessResolver#getGroupsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Group> getGroupsWhereSubjectDoesntHavePrivilege(String stemId, Scope scope,
      Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    throw E;
  }

  /**
   * @see AccessResolver#hqlFilterGroupsNotWithPrivWhereClause(Subject, HqlQuery, StringBuilder, String, Privilege, boolean)
   */
  public boolean hqlFilterGroupsNotWithPrivWhereClause(Subject subject, HqlQuery byHqlStatic, 
      StringBuilder sql, String groupColumn, Privilege privilege, boolean considerAllSubject) {
    throw E;
  }

  /**
   * @see AccessResolver#retrievePrivileges(Group, Set, MembershipType, QueryPaging, Set)
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(Group group,
      Set<Privilege> privileges, MembershipType membershipType, QueryPaging queryPaging,
      Set<Member> additionalMembers) {
    throw E;
  }
}

