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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import  edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.subject.Subject;


/**
 * Decorator for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: NamingResolverDecorator.java,v 1.3 2009-09-21 06:14:26 mchyzer Exp $
 * @since   1.2.1
 */
public abstract class NamingResolverDecorator implements NamingResolver {
  // TODO 20070820 DRY w/ access resolution

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#flushCache()
   */
  public void flushCache() {
    this.getDecoratedResolver().flushCache();
  }

  /** */
  private NamingResolver  decorated;

  /** */
  private ParameterHelper param;


 
  /**
   * @param   resolver  <i>NamingResolver</i> to decorate.
   * @throws  IllegalArgumentException if <i>resolver</i> is null.
   * @since   1.2.1
   */
  public NamingResolverDecorator(NamingResolver resolver) 
    throws  IllegalArgumentException
  {
    this.param      = new ParameterHelper();
    this.param.notNullNamingResolver(resolver);
    this.decorated  = resolver;
  }


  /**
   * @return  Decorated <i>NamingResolver</i>.
   * @throws  IllegalStateException if no decorated <i>NamingResolver</i>.
   * @since   1.2.1
   */
  public NamingResolver getDecoratedResolver() 
    throws  IllegalStateException
  {
    if (this.decorated == null) { 
      throw new IllegalStateException("null decorated NamingResolver");
    }
    return this.decorated;
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#getGrouperSession()
   */
  public GrouperSession getGrouperSession() {
    return this.getDecoratedResolver().getGrouperSession();
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#getPrivileges(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject)
   */
  public Set<NamingPrivilege> getPrivileges(Stem stem, Subject subject)
      throws IllegalArgumentException {
    return this.getDecoratedResolver().getPrivileges(stem, subject);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#getStemsWhereSubjectHasPrivilege(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    return this.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#getSubjectsWithPrivilege(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
      throws IllegalArgumentException {
    return this.getDecoratedResolver().getSubjectsWithPrivilege(stem, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#grantPrivilege(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege, String)
   */
  public void grantPrivilege(Stem stem, Subject subject, Privilege privilege, String uuid)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().grantPrivilege(stem, subject, privilege, uuid);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hasPrivilege(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException {
    return this.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  public boolean hqlFilterStemsWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Set<Privilege> privInSet) {
    return this.getDecoratedResolver().hqlFilterStemsWhereClause(subject, hqlQuery, hql, stemColumn, privInSet);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#postHqlFilterStems(java.util.Set, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  public Set<Stem> postHqlFilterStems(Set<Stem> stems, Subject subject,
      Set<Privilege> privInSet) {
    return this.getDecoratedResolver().postHqlFilterStems(stems, subject, privInSet);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#privilegeCopy(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Stem stem1, Stem stem2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().privilegeCopy(stem1, stem2, priv);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#privilegeCopy(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(Subject subj1, Subject subj2, Privilege priv)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().privilegeCopy(subj1, subj2, priv);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#revokeAllPrivilegesForSubject(edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(Subject subject) {
    this.getDecoratedResolver().revokeAllPrivilegesForSubject(subject);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#revokePrivilege(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(Stem stem, Privilege privilege)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().revokePrivilege(stem, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#revokePrivilege(edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
      throws IllegalArgumentException, UnableToPerformException {
    this.getDecoratedResolver().revokePrivilege(stem, subject, privilege);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#stop()
   */
  public void stop() {
    this.getDecoratedResolver().stop();
  }

  /**
   * @see NamingResolver#getStemsWhereSubjectDoesntHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Stem> getStemsWhereSubjectDoesntHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    return this.getDecoratedResolver().getStemsWhereSubjectDoesntHavePrivilege(stemId, scope, subject, privilege, considerAllSubject, sqlLikeString);
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsWhereClause(edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.String, Privilege)
   */
  public boolean hqlFilterStemsNotWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject) {
    return this.getDecoratedResolver().hqlFilterStemsNotWithPrivWhereClause(subject, hqlQuery, hql, stemColumn, privilege, considerAllSubject);
  }


  /**
   * @see NamingResolver#getStemsWhereSubjectDoesHavePrivilege(String, Scope, Subject, Privilege, boolean, String)
   */
  public Set<Stem> getStemsWhereSubjectDoesHavePrivilege(
      String stemId, Scope scope, Subject subject, Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {
    return this.getDecoratedResolver().getStemsWhereSubjectDoesHavePrivilege(stemId, scope, subject, privilege, considerAllSubject, sqlLikeString);
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.NamingResolver#hqlFilterStemsWithPrivWhereClause(Subject, HqlQuery, StringBuilder, String, Privilege, boolean)
   */
  public boolean hqlFilterStemsWithPrivWhereClause(Subject subject, HqlQuery hqlQuery,
      StringBuilder hql, String stemColumn, Privilege privilege, boolean considerAllSubject) {
    return this.getDecoratedResolver().hqlFilterStemsWithPrivWhereClause(subject, hqlQuery, hql, stemColumn, privilege, considerAllSubject);
  }


}

