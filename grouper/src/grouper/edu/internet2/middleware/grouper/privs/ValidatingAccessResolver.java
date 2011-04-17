/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.privs;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
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
 * Decorator that provides parameter validation for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ValidatingAccessResolver.java,v 1.13 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class ValidatingAccessResolver extends AccessResolverDecorator {

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }

  /** */
  private ParameterHelper param;

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public ValidatingAccessResolver(AccessResolver resolver) {
    super(resolver);
    this.param = new ParameterHelper();
  }

  /**
   * @see     AccessResolver#getGroupsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Group> getGroupsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject,
        privilege);
  }

  /**
   * @see     AccessResolver#getGroupsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Group> getGroupsWhereSubjectDoesntHavePrivilege(String stemId, Scope scope, 
      Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString)
      throws IllegalArgumentException {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getGroupsWhereSubjectDoesntHavePrivilege(
        stemId, scope, subject, privilege, considerAllSubject, sqlLikeString);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolverDecorator#getStemsWhereGroupThatSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  @Override
  public Set<Stem> getStemsWhereGroupThatSubjectHasPrivilege(Subject subject,
      Privilege privilege) throws IllegalArgumentException {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getStemsWhereGroupThatSubjectHasPrivilege(subject,
        privilege);
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
      throws IllegalArgumentException {
    this.param.notNullGroup(group).notNullSubject(subject);
    return super.getDecoratedResolver().getPrivileges(group, subject);
  }

  /**
   * @see     AccessResolver#getSubjectsWithPrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullGroup(group).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getSubjectsWithPrivilege(group, privilege);
  }

  /**
   * @see     AccessResolver#grantPrivilege(Group, Subject, Privilege, String)
   * @since   1.2.1
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullGroup(group).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().grantPrivilege(group, subject, privilege, uuid);
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullGroup(group).notNullSubject(subject).notNullPrivilege(privilege);
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //WheelAccessResolver
    return decoratedResolver.hasPrivilege(group, subject, privilege);
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullGroup(group).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(group, privilege);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject,
      Set<Privilege> privInSet) {
    this.param.notNullSubject(subject);
    return super.getDecoratedResolver().postHqlFilterGroups(groups, subject, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterStemsWithGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStemsWithGroups(Set<Stem> stems, Subject subject,
      Set<Privilege> inPrivSet) {
    this.param.notNullSubject(subject);
    return super.getDecoratedResolver().postHqlFilterStemsWithGroups(stems, subject, inPrivSet);
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullGroup(group).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(group, subject, privilege);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.param.notNullGroup(g1).notNullGroup(g2).notNullPrivilege(priv);
    super.getDecoratedResolver().privilegeCopy(g1, g2, priv);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.param.notNullSubject(subj1).notNullSubject(subj2).notNullPrivilege(priv);
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, String, Set)
   */
  public boolean hqlFilterGroupsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Set<Privilege> privInSet) {

    this.param.notNullSubject(subject).notNullHqlQuery(hqlQuery);

    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterGroupsWhereClause(subject, hqlQuery, hql,
        groupColumn, privInSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#hqlFilterGroupsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, String, Privilege, boolean)
   */
  public boolean hqlFilterGroupsNotWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String groupColumn, Privilege privilege, boolean considerAllSubject) {

    this.param.notNullSubject(subject).notNullHqlQuery(hqlQuery);

    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterGroupsNotWithPrivWhereClause(subject, hqlQuery, hql,
        groupColumn, privilege, considerAllSubject);
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterMemberships(edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Membership> postHqlFilterMemberships(Subject subject,
      Set<Membership> memberships) {

    this.param.notNullSubject(subject);

    AccessResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.postHqlFilterMemberships(subject, memberships);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    this.param.notNullSubject(subject);
    super.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolverDecorator#retrievePrivileges(Group, java.util.Set, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.internal.dao.QueryPaging, java.util.Set)
   */
  @Override
  public Set<PrivilegeSubjectContainer> retrievePrivileges(Group group,
      Set<Privilege> privileges, MembershipType membershipType, QueryPaging queryPaging,
      Set<Member> additionalMembers) {
    
    this.param.notNullGroup(group);

    return super.retrievePrivileges(group, privileges, membershipType, queryPaging,
        additionalMembers);

  }

}
