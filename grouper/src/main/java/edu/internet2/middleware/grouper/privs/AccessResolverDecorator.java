/**
 * Copyright 2014 Internet2
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

package edu.internet2.middleware.grouper.privs;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.subject.Subject;


/**
 * Decorator for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: AccessResolverDecorator.java,v 1.3 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public abstract class AccessResolverDecorator implements AccessResolver {

  /** */
  private AccessResolver  decorated;
  
  /** */
  private ParameterHelper param;


 
  /**
   * @param   resolver  <i>AccessResolver</i> to decorate.
   * @throws  IllegalArgumentException if <i>resolver</i> is null.
   * @since   1.2.1
   */
  public AccessResolverDecorator(AccessResolver resolver) 
    throws  IllegalArgumentException
  {
    this.param      = new ParameterHelper();
    this.param.notNullAccessResolver(resolver);
    this.decorated  = resolver;
  }


  /**
   * @return  Decorated <i>AccessResolver</i>.
   * @throws  IllegalStateException if no decorated <i>AccessResolver</i>.
   * @since   1.2.1
   */
  public AccessResolver getDecoratedResolver() 
    throws  IllegalStateException
  {
    if (this.decorated == null) { 
      throw new IllegalStateException("null decorated AccessResolver");
    }
    return this.decorated;
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    this.getDecoratedResolver().flushCache();
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    return this.getDecoratedResolver().getGrouperSession();
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getGroupsWhereSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    return this.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see AccessResolver#getGroupsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Group> getGroupsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    return this.getDecoratedResolver().getGroupsWhereSubjectDoesntHavePrivilege(stemId, 
        scope, subject, privilege, considerAllSubject, 
        sqlLikeString);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getStemsWhereGroupThatSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Stem> getStemsWhereGroupThatSubjectHasPrivilege(Subject subject,
      Privilege privilege) throws IllegalArgumentException {
    return this.getDecoratedResolver().getStemsWhereGroupThatSubjectHasPrivilege(subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getPrivileges(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.subject.Subject)
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
      throws IllegalArgumentException {
    return this.getDecoratedResolver().getPrivileges(group, subject);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getSubjectsWithPrivilege(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
      throws IllegalArgumentException {
    return this.getDecoratedResolver().getSubjectsWithPrivilege(group, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#grantPrivilege(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege, String)
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().grantPrivilege(group, subject, privilege, uuid);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hasPrivilege(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    return this.getDecoratedResolver().hasPrivilege(group, subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterGroupsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {
    return this.getDecoratedResolver().hqlFilterGroupsWhereClause(subject, hqlQuery, hql, groupColumn, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege)
   */
  public boolean hqlFilterGroupsNotWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Privilege privilege, boolean considerAllSubject) {
    return this.getDecoratedResolver().hqlFilterGroupsNotWithPrivWhereClause(subject, hqlQuery, hql, groupColumn, privilege, considerAllSubject);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject,
      Set<Privilege> privInSet) {
    return this.getDecoratedResolver().postHqlFilterGroups(groups, subject, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterStemsWithGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStemsWithGroups(Set<Stem> stems, Subject subject,
      Set<Privilege> inPrivSet) {
    return this.getDecoratedResolver().postHqlFilterStemsWithGroups(stems, subject, inPrivSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterMemberships(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Membership> postHqlFilterMemberships(Subject subject,
      Set<Membership> memberships) {
    return this.getDecoratedResolver().postHqlFilterMemberships(subject, memberships);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().privilegeCopy(g1, g2, priv);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    this.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#revokePrivilege(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(Group group, Privilege privilege)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().revokePrivilege(group, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#revokePrivilege(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().revokePrivilege(group, subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
    this.getDecoratedResolver().stop();
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#retrievePrivileges(Group, java.util.Set, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.internal.dao.QueryPaging, java.util.Set)
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(Group group,
      Set<Privilege> privileges, MembershipType membershipType, QueryPaging queryPaging,
      Set<Member> additionalMembers) {
    return this.getDecoratedResolver().retrievePrivileges(group, privileges, 
        membershipType, queryPaging, additionalMembers);
  }


  /**
   * @see AccessResolver#getGroupsWhereSubjectDoesHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Group> getGroupsWhereSubjectDoesHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    return this.getDecoratedResolver().getGroupsWhereSubjectDoesHavePrivilege(stemId, 
        scope, subject, privilege, considerAllSubject, 
        sqlLikeString);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWithPrivWhereClause(Subject, HqlQuery, StringBuilder, String, Privilege, boolean)
   */
  public boolean hqlFilterGroupsWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Privilege privilege, boolean considerAllSubject) {
    return this.getDecoratedResolver().hqlFilterGroupsWithPrivWhereClause(subject, hqlQuery, hql, groupColumn, privilege, considerAllSubject);
  }

}

