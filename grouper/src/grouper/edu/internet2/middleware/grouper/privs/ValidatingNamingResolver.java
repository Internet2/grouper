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

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * Decorator that provides parameter validation for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: ValidatingNamingResolver.java,v 1.11 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public class ValidatingNamingResolver extends NamingResolverDecorator {

  /**
   * @see    NamingResolver#getStemsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Stem> getStemsWhereSubjectDoesntHavePrivilege(String stemId, Scope scope, 
      Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString)
      throws IllegalArgumentException {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getStemsWhereSubjectDoesntHavePrivilege(stemId, scope, 
        subject, privilege, considerAllSubject, sqlLikeString);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#flushCache()
   */
  public void flushCache() {
    super.getDecoratedResolver().flushCache();
  }

  // TODO 20070820 DRY w/ access resolution

  /** */
  private ParameterHelper param;

  /**
   * @param resolver 
   * @since   1.2.1
   */
  public ValidatingNamingResolver(NamingResolver resolver) {
    super(resolver);
    this.param = new ParameterHelper();
  }

  /**
   * @see     NamingResolver#getStemsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   1.2.1
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(subject,
        privilege);
  }

  /**
   * @see     NamingResolver#getPrivileges(Stem, Subject)
   * @since   1.2.1
   */
  public Set<NamingPrivilege> getPrivileges(Stem stem, Subject subject)
      throws IllegalArgumentException {
    this.param.notNullStem(stem).notNullSubject(subject);
    return super.getDecoratedResolver().getPrivileges(stem, subject);
  }

  /**
   * @see     NamingResolver#getSubjectsWithPrivilege(Stem, Privilege)
   * @since   1.2.1
   */
  public Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullStem(stem).notNullPrivilege(privilege);
    return super.getDecoratedResolver().getSubjectsWithPrivilege(stem, privilege);
  }

  /**
   * @see     NamingResolver#grantPrivilege(Stem, Subject, Privilege, String)
   * @since   1.2.1
   */
  public void grantPrivilege(Stem stem, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullStem(stem).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().grantPrivilege(stem, subject, privilege, uuid);
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    this.param.notNullStem(stem).notNullSubject(subject).notNullPrivilege(privilege);
    return super.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullStem(stem).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(stem, privilege);
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Subject, Privilege)
   * @since   1.2.1
   */
  public void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException,
      UnableToPerformException {
    this.param.notNullStem(stem).notNullSubject(subject).notNullPrivilege(privilege);
    super.getDecoratedResolver().revokePrivilege(stem, subject, privilege);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#privilegeCopy(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Stem stem1, Stem stem2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.param.notNullStem(stem1).notNullStem(stem2).notNullPrivilege(priv);
    super.getDecoratedResolver().privilegeCopy(stem1, stem2, priv);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.param.notNullSubject(subj1).notNullSubject(subj2).notNullPrivilege(priv);
    super.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterStemsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Set<Privilege> privInSet) {

    this.param.notNullSubject(subject).notNullHqlQuery(hqlQuery);

    NamingResolver decoratedResolver = super.getDecoratedResolver();
    //CachingNamingResolver
    return decoratedResolver.hqlFilterStemsWhereClause(subject, hqlQuery, hql,
        stemColumn, privInSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#postHqlFilterStems(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStems(Set<Stem> stems, Subject subject,
      Set<Privilege> privInSet) {

    this.param.notNullSubject(subject);

    Set<Stem> filteredStems = super.getDecoratedResolver().postHqlFilterStems(stems,
        subject, privInSet);

    //return filtered groups
    return filteredStems;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    this.param.notNullSubject(subject);
    super.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, String, Privilege, boolean)
   */
  public boolean hqlFilterStemsNotWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject) {

    this.param.notNullSubject(subject).notNullHqlQuery(hqlQuery);

    NamingResolver decoratedResolver = super.getDecoratedResolver();
    //System.out.println(decoratedResolver.getClass().getName());
    //CachingAccessResolver
    return decoratedResolver.hqlFilterStemsNotWithPrivWhereClause(subject, hqlQuery, hql,
        stemColumn, privilege, considerAllSubject);
  }


}
