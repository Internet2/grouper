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

import  edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import  edu.internet2.middleware.subject.Subject;


/**
 * Decorator that provides parameter validation for {@link AccessResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ValidatingAccessResolver.java,v 1.12 2009-08-29 15:57:59 shilen Exp $
 * @since   1.2.1
 */
public class ValidatingAccessResolver extends AccessResolverDecorator {

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
    throws  IllegalArgumentException
  {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getGroupsWhereSubjectHasPrivilege(subject, privilege);
  }

  /**
   * @see     AccessResolver#getPrivileges(Group, Subject)
   * @since   1.2.1
   */
  public Set<AccessPrivilege> getPrivileges(Group group, Subject subject)
    throws  IllegalArgumentException
  {
    this.param.notNullGroup(group).notNullSubject(subject);
    return super.getDecoratedResolver().getPrivileges(group, subject);
  }

  /**
   * @see     AccessResolver#getSubjectsWithPrivilege(Group, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Group group, Privilege privilege)
    throws  IllegalArgumentException
  {
    this.param.notNullGroup(group).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getSubjectsWithPrivilege(group, privilege);
  }

  /**
   * @see     AccessResolver#grantPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void grantPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    this.param.notNullGroup(group).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().grantPrivilege(group, subject, privilege);
  }

  /**
   * @see     AccessResolver#hasPrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
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
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    this.param.notNullGroup(group).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(group, privilege);
  }
            
  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#postHqlFilterGroups(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Group> postHqlFilterGroups(Set<Group> groups, Subject subject, Set<Privilege> privInSet) {
    this.param.notNullSubject(subject);
    return super.getDecoratedResolver().postHqlFilterGroups(groups, subject, privInSet);
  }

  /**
   * @see     AccessResolver#revokePrivilege(Group, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Group group, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    this.param.notNullGroup(group).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(group, subject, privilege);
  }




  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#flushCache()
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }



  /*
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Group g1, Group g2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.param.notNullGroup(g1).notNullGroup(g2).notNullPrivilege(priv);
    super.getDecoratedResolver().privilegeCopy(g1, g2, priv);
  }



  /*
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.param.notNullSubject(subj1).notNullSubject(subj2).notNullPrivilege(priv);
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }            

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#stop()
   */
  public void stop() {
    super.getDecoratedResolver().stop();
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
    return decoratedResolver.hqlFilterGroupsWhereClause(subject, hqlQuery, hql, groupColumn, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    AccessResolver decoratedResolver = super.getDecoratedResolver();
    return decoratedResolver.getGrouperSession();
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

}

